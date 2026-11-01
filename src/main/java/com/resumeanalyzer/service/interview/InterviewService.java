package com.resumeanalyzer.service.interview;

import com.resumeanalyzer.dto.interview.*;
import com.resumeanalyzer.entity.*;
import com.resumeanalyzer.exception.BadRequestException;
import com.resumeanalyzer.exception.ResourceNotFoundException;
import com.resumeanalyzer.exception.UnauthorizedAccessException;
import com.resumeanalyzer.repository.*;
import com.resumeanalyzer.service.ai.AIService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class InterviewService {

    private final InterviewSessionRepository sessionRepository;
    private final InterviewQuestionRepository questionRepository;
    private final InterviewAnswerRepository answerRepository;
    private final AnswerEvaluationRepository evaluationRepository;
    private final ResumeRepository resumeRepository;
    private final UserRepository userRepository;
    private final AIService aiService;

    public InterviewService(InterviewSessionRepository sessionRepository,
                            InterviewQuestionRepository questionRepository,
                            InterviewAnswerRepository answerRepository,
                            AnswerEvaluationRepository evaluationRepository,
                            ResumeRepository resumeRepository,
                            UserRepository userRepository,
                            AIService aiService) {
        this.sessionRepository = sessionRepository;
        this.questionRepository = questionRepository;
        this.answerRepository = answerRepository;
        this.evaluationRepository = evaluationRepository;
        this.resumeRepository = resumeRepository;
        this.userRepository = userRepository;
        this.aiService = aiService;
    }

    @Transactional
    public InterviewSessionDetailDto createSession(Long userId, CreateInterviewRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        Resume resume = resumeRepository.findById(request.getResumeId())
                .orElseThrow(() -> new ResourceNotFoundException("Resume not found with id: " + request.getResumeId()));

        if (!resume.getUser().getId().equals(userId)) {
            throw new UnauthorizedAccessException("You are not authorized to use this resume for an interview.");
        }

        InterviewSession session = new InterviewSession();
        session.setUser(user);
        session.setResume(resume);
        session.setTargetRole(request.getTargetRole().trim());
        session.setInterviewType(request.getInterviewType());
        session.setDifficulty(request.getDifficulty());
        session.setFocusArea(request.getFocusArea());
        session.setQuestionCount(request.getQuestionCount());
        session.setStatus(InterviewSessionStatus.CREATED);

        InterviewSession savedSession = sessionRepository.save(session);

        // Generate grounded questions
        List<AIService.GeneratedQuestionData> generatedQuestions = aiService.generateQuestions(
                resume,
                request.getTargetRole(),
                request.getInterviewType(),
                request.getDifficulty(),
                request.getFocusArea(),
                request.getQuestionCount()
        );

        List<InterviewQuestion> questionEntities = new ArrayList<>();
        int order = 1;
        for (AIService.GeneratedQuestionData gq : generatedQuestions) {
            InterviewQuestion q = new InterviewQuestion(
                    savedSession,
                    order++,
                    gq.getQuestionText(),
                    gq.getCategory(),
                    gq.getDifficulty(),
                    gq.getContextHint()
            );
            questionEntities.add(questionRepository.save(q));
        }

        savedSession.setQuestions(questionEntities);
        savedSession.setStatus(InterviewSessionStatus.IN_PROGRESS);
        sessionRepository.save(savedSession);

        return getSessionDetail(userId, savedSession.getId());
    }

    @Transactional(readOnly = true)
    public InterviewSessionDetailDto getSessionDetail(Long userId, Long sessionId) {
        InterviewSession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("Interview session not found with id: " + sessionId));

        if (!session.getUser().getId().equals(userId)) {
            throw new UnauthorizedAccessException("You are not authorized to access this interview session.");
        }

        return toDetailDto(session);
    }

    @Transactional
    public InterviewAnswerDto saveAnswer(Long userId, Long sessionId, SaveAnswerRequest request) {
        InterviewSession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("Interview session not found with id: " + sessionId));

        if (!session.getUser().getId().equals(userId)) {
            throw new UnauthorizedAccessException("You are not authorized to answer for this session.");
        }

        if (session.getStatus() != InterviewSessionStatus.IN_PROGRESS) {
            throw new BadRequestException("Cannot submit answers to an interview in status: " + session.getStatus());
        }

        InterviewQuestion question = questionRepository.findById(request.getQuestionId())
                .orElseThrow(() -> new ResourceNotFoundException("Question not found with id: " + request.getQuestionId()));

        if (!question.getSession().getId().equals(sessionId)) {
            throw new BadRequestException("Question does not belong to this interview session.");
        }

        String text = request.getAnswerText() != null ? request.getAnswerText().trim() : "";

        InterviewAnswer answer = answerRepository.findBySessionIdAndQuestionId(sessionId, question.getId())
                .orElseGet(() -> {
                    InterviewAnswer newAns = new InterviewAnswer();
                    newAns.setSession(session);
                    newAns.setQuestion(question);
                    newAns.setUser(session.getUser());
                    return newAns;
                });

        answer.setAnswerText(text);
        answer.setSubmittedAt(LocalDateTime.now());

        InterviewAnswer saved = answerRepository.saveAndFlush(answer);

        return new InterviewAnswerDto(saved.getId(), question.getId(), saved.getAnswerText(), saved.getSubmittedAt(), null);
    }

    @Transactional
    public InterviewSessionDetailDto submitInterview(Long userId, Long sessionId) {
        InterviewSession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("Interview session not found with id: " + sessionId));

        if (!session.getUser().getId().equals(userId)) {
            throw new UnauthorizedAccessException("You are not authorized to submit this session.");
        }

        if (session.getStatus() == InterviewSessionStatus.COMPLETED) {
            return toDetailDto(session);
        }

        session.setStatus(InterviewSessionStatus.EVALUATING);
        sessionRepository.save(session);

        List<InterviewQuestion> questions = questionRepository.findBySessionIdOrderByQuestionOrderAsc(sessionId);
        List<InterviewAnswer> answers = answerRepository.findBySessionId(sessionId);

        int totalScoreAccumulator = 0;
        int evaluatedCount = 0;

        for (InterviewQuestion q : questions) {
            InterviewAnswer ans = answers.stream()
                    .filter(a -> a.getQuestion().getId().equals(q.getId()))
                    .findFirst()
                    .orElseGet(() -> {
                        InterviewAnswer emptyAns = new InterviewAnswer();
                        emptyAns.setSession(session);
                        emptyAns.setQuestion(q);
                        emptyAns.setUser(session.getUser());
                        emptyAns.setAnswerText("");
                        emptyAns.setSubmittedAt(LocalDateTime.now());
                        return answerRepository.saveAndFlush(emptyAns);
                    });

            AIService.EvaluatedAnswerData evalData = aiService.evaluateAnswer(
                    q.getQuestionText(),
                    q.getQuestionCategory(),
                    ans.getAnswerText()
            );

            AnswerEvaluation eval = evaluationRepository.findByAnswerId(ans.getId())
                    .orElse(new AnswerEvaluation());

            eval.setAnswer(ans);
            eval.setCorrectnessScore(evalData.getCorrectness());
            eval.setUnderstandingScore(evalData.getUnderstanding());
            eval.setRelevanceScore(evalData.getRelevance());
            eval.setClarityScore(evalData.getClarity());
            eval.setTotalScore(evalData.getTotalScore());
            eval.setFeedback(evalData.getFeedback());
            eval.setMissingPoints(evalData.getMissingPoints());
            eval.setSampleImprovedAnswer(evalData.getSampleAnswer());

            AnswerEvaluation savedEval = evaluationRepository.save(eval);
            ans.setEvaluation(savedEval);

            totalScoreAccumulator += evalData.getTotalScore();
            evaluatedCount++;
        }

        int aggregateOverallScore = evaluatedCount > 0 ? (totalScoreAccumulator / evaluatedCount) : 0;
        session.setOverallScore(aggregateOverallScore);

        // Overall summary analysis
        if (aggregateOverallScore >= 80) {
            session.setStrengths("Strong technical communication, consistent architectural grounding, and clear articulation of problem-solving methods.");
            session.setImprovements("Fine-tune explanations with specific latency/resource metrics and edge-case handling.");
            session.setTopicsToPrepare("Deep dive into distributed system edge cases, cache-invalidation strategies, and high-availability patterns.");
        } else if (aggregateOverallScore >= 60) {
            session.setStrengths("Good foundational knowledge across primary frameworks and clear understanding of core concepts.");
            session.setImprovements("Incorporate more industry terminology (e.g. ACID, CAP theorem, indexing structures) and structure answers using the STAR method.");
            session.setTopicsToPrepare("Concurrency mechanisms, database indexing execution plans, and production incident root-cause analysis.");
        } else {
            session.setStrengths("Demonstrated awareness of core development tasks and basic workflows.");
            session.setImprovements("Provide more comprehensive responses; avoid brief one-sentence answers; elaborate on internal mechanisms and why specific decisions were made.");
            session.setTopicsToPrepare("Core language mechanics, relational query optimization, RESTful API conventions, and systematic debugging steps.");
        }

        session.setStatus(InterviewSessionStatus.COMPLETED);
        InterviewSession completedSession = sessionRepository.save(session);

        return toDetailDto(completedSession);
    }

    @Transactional(readOnly = true)
    public List<InterviewSessionSummaryDto> getUserSessions(Long userId, String role, InterviewSessionStatus status) {
        List<InterviewSession> sessions = sessionRepository.findByUserIdWithFilters(userId, role, status);
        return sessions.stream().map(this::toSummaryDto).collect(Collectors.toList());
    }

    private InterviewSessionSummaryDto toSummaryDto(InterviewSession session) {
        InterviewSessionSummaryDto dto = new InterviewSessionSummaryDto();
        dto.setId(session.getId());
        dto.setResumeId(session.getResume().getId());
        dto.setResumeTitle(session.getResume().getTitle());
        dto.setTargetRole(session.getTargetRole());
        dto.setInterviewType(session.getInterviewType());
        dto.setDifficulty(session.getDifficulty());
        dto.setFocusArea(session.getFocusArea());
        dto.setQuestionCount(session.getQuestionCount());
        dto.setStatus(session.getStatus());
        dto.setOverallScore(session.getOverallScore());
        dto.setCreatedAt(session.getCreatedAt());
        dto.setUpdatedAt(session.getUpdatedAt());

        long answered = answerRepository.findBySessionId(session.getId()).stream()
                .filter(a -> a.getAnswerText() != null && !a.getAnswerText().isBlank())
                .count();
        dto.setAnsweredCount((int) answered);

        return dto;
    }

    private InterviewSessionDetailDto toDetailDto(InterviewSession session) {
        InterviewSessionDetailDto dto = new InterviewSessionDetailDto();
        dto.setId(session.getId());
        dto.setResumeId(session.getResume().getId());
        dto.setResumeTitle(session.getResume().getTitle());
        dto.setTargetRole(session.getTargetRole());
        dto.setInterviewType(session.getInterviewType());
        dto.setDifficulty(session.getDifficulty());
        dto.setFocusArea(session.getFocusArea());
        dto.setQuestionCount(session.getQuestionCount());
        dto.setStatus(session.getStatus());
        dto.setOverallScore(session.getOverallScore());
        dto.setStrengths(session.getStrengths());
        dto.setImprovements(session.getImprovements());
        dto.setTopicsToPrepare(session.getTopicsToPrepare());
        dto.setDemoMode(aiService.isDemoModeActive());
        dto.setCreatedAt(session.getCreatedAt());
        dto.setUpdatedAt(session.getUpdatedAt());

        List<InterviewQuestion> questions = questionRepository.findBySessionIdOrderByQuestionOrderAsc(session.getId());
        dto.setQuestions(questions.stream().map(q -> new InterviewQuestionDto(
                q.getId(),
                q.getQuestionOrder(),
                q.getQuestionText(),
                q.getQuestionCategory(),
                q.getDifficulty(),
                q.getContextHint()
        )).collect(Collectors.toList()));

        List<InterviewAnswer> answers = answerRepository.findBySessionId(session.getId());
        dto.setAnswers(answers.stream().map(a -> {
            AnswerEvaluationDto evalDto = null;
            AnswerEvaluation ev = a.getEvaluation() != null ? a.getEvaluation() : evaluationRepository.findByAnswerId(a.getId()).orElse(null);
            if (ev != null) {
                evalDto = new AnswerEvaluationDto(
                        ev.getId(),
                        ev.getCorrectnessScore(),
                        ev.getUnderstandingScore(),
                        ev.getRelevanceScore(),
                        ev.getClarityScore(),
                        ev.getTotalScore(),
                        ev.getFeedback(),
                        ev.getMissingPoints(),
                        ev.getSampleImprovedAnswer()
                );
            }
            return new InterviewAnswerDto(a.getId(), a.getQuestion().getId(), a.getAnswerText(), a.getSubmittedAt(), evalDto);
        }).collect(Collectors.toList()));

        return dto;
    }
}
