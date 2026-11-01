package com.resumeanalyzer;

import com.resumeanalyzer.dto.interview.InterviewAnswerDto;
import com.resumeanalyzer.dto.interview.InterviewSessionDetailDto;
import com.resumeanalyzer.dto.interview.SaveAnswerRequest;
import com.resumeanalyzer.entity.*;
import com.resumeanalyzer.exception.BadRequestException;
import com.resumeanalyzer.repository.*;
import com.resumeanalyzer.service.ai.AIService;
import com.resumeanalyzer.service.interview.InterviewService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class InterviewAnswerPersistenceTest {

    @Mock private InterviewSessionRepository sessionRepository;
    @Mock private InterviewQuestionRepository questionRepository;
    @Mock private InterviewAnswerRepository answerRepository;
    @Mock private AnswerEvaluationRepository evaluationRepository;
    @Mock private ResumeRepository resumeRepository;
    @Mock private UserRepository userRepository;
    @Mock private AIService aiService;

    private InterviewService interviewService;

    private User user;
    private Resume resume;
    private InterviewSession session;
    private InterviewQuestion question1;
    private InterviewQuestion question2;

    @BeforeEach
    void setUp() {
        interviewService = new InterviewService(
                sessionRepository,
                questionRepository,
                answerRepository,
                evaluationRepository,
                resumeRepository,
                userRepository,
                aiService
        );

        user = new User();
        user.setId(10L);
        user.setEmail("candidate@example.com");

        resume = new Resume();
        resume.setId(20L);
        resume.setUser(user);
        resume.setTitle("Full Stack Java Resume");

        session = new InterviewSession();
        session.setId(100L);
        session.setUser(user);
        session.setResume(resume);
        session.setTargetRole("Java Backend Engineer");
        session.setStatus(InterviewSessionStatus.IN_PROGRESS);
        session.setInterviewType(InterviewType.TECHNICAL);
        session.setDifficulty(DifficultyLevel.INTERMEDIATE);
        session.setQuestionCount(2);

        question1 = new InterviewQuestion(session, 1, "How does ConcurrentHashMap achieve thread-safety?", "Concurrency", DifficultyLevel.INTERMEDIATE, "Explain CAS and bucket locking.");
        question1.setId(501L);

        question2 = new InterviewQuestion(session, 2, "Explain database indexing in MySQL.", "Database", DifficultyLevel.INTERMEDIATE, "Discuss B+ Trees and clustered index.");
        question2.setId(502L);
    }

    @Test
    @DisplayName("Should successfully save answer and link to correct session, question, and user")
    void testSaveAnswerLinksSessionAndQuestionAndPersists() {
        when(sessionRepository.findById(100L)).thenReturn(Optional.of(session));
        when(questionRepository.findById(501L)).thenReturn(Optional.of(question1));
        when(answerRepository.findBySessionIdAndQuestionId(100L, 501L)).thenReturn(Optional.empty());

        when(answerRepository.saveAndFlush(any(InterviewAnswer.class))).thenAnswer(invocation -> {
            InterviewAnswer ans = invocation.getArgument(0);
            ans.setId(901L);
            return ans;
        });

        SaveAnswerRequest request = new SaveAnswerRequest(501L, "ConcurrentHashMap uses synchronized blocks on individual bucket nodes rather than locking the entire map.");
        InterviewAnswerDto result = interviewService.saveAnswer(10L, 100L, request);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(901L);
        assertThat(result.getQuestionId()).isEqualTo(501L);
        assertThat(result.getAnswerText()).contains("synchronized blocks on individual bucket nodes");

        ArgumentCaptor<InterviewAnswer> captor = ArgumentCaptor.forClass(InterviewAnswer.class);
        verify(answerRepository).saveAndFlush(captor.capture());
        InterviewAnswer captured = captor.getValue();
        assertThat(captured.getSession().getId()).isEqualTo(100L);
        assertThat(captured.getQuestion().getId()).isEqualTo(501L);
        assertThat(captured.getUser().getId()).isEqualTo(10L);
        assertThat(captured.getSubmittedAt()).isNotNull();
    }

    @Test
    @DisplayName("Should update existing answer idempotently without creating duplicate rows")
    void testUpdateExistingAnswerDoesNotCreateDuplicates() {
        InterviewAnswer existing = new InterviewAnswer();
        existing.setId(901L);
        existing.setSession(session);
        existing.setQuestion(question1);
        existing.setUser(user);
        existing.setAnswerText("Initial draft answer.");
        existing.setSubmittedAt(LocalDateTime.now().minusMinutes(5));

        when(sessionRepository.findById(100L)).thenReturn(Optional.of(session));
        when(questionRepository.findById(501L)).thenReturn(Optional.of(question1));
        when(answerRepository.findBySessionIdAndQuestionId(100L, 501L)).thenReturn(Optional.of(existing));
        when(answerRepository.saveAndFlush(any(InterviewAnswer.class))).thenReturn(existing);

        SaveAnswerRequest updateRequest = new SaveAnswerRequest(501L, "Updated comprehensive explanation with CAS and volatile node references.");
        InterviewAnswerDto result = interviewService.saveAnswer(10L, 100L, updateRequest);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(901L);
        assertThat(result.getAnswerText()).contains("Updated comprehensive explanation");

        verify(answerRepository).saveAndFlush(existing);
    }

    @Test
    @DisplayName("Should allow short keyword answers without throwing validation errors")
    void testSaveShortAnswerSucceeds() {
        when(sessionRepository.findById(100L)).thenReturn(Optional.of(session));
        when(questionRepository.findById(502L)).thenReturn(Optional.of(question2));
        when(answerRepository.findBySessionIdAndQuestionId(100L, 502L)).thenReturn(Optional.empty());
        when(answerRepository.saveAndFlush(any(InterviewAnswer.class))).thenAnswer(invocation -> invocation.getArgument(0));

        SaveAnswerRequest shortRequest = new SaveAnswerRequest(502L, "B+ Tree");
        InterviewAnswerDto result = interviewService.saveAnswer(10L, 100L, shortRequest);

        assertThat(result.getAnswerText()).isEqualTo("B+ Tree");
    }

    @Test
    @DisplayName("Should allow blank or cleared answer text when candidate clears response")
    void testSaveBlankAnswerSucceeds() {
        when(sessionRepository.findById(100L)).thenReturn(Optional.of(session));
        when(questionRepository.findById(502L)).thenReturn(Optional.of(question2));
        when(answerRepository.findBySessionIdAndQuestionId(100L, 502L)).thenReturn(Optional.empty());
        when(answerRepository.saveAndFlush(any(InterviewAnswer.class))).thenAnswer(invocation -> invocation.getArgument(0));

        SaveAnswerRequest blankRequest = new SaveAnswerRequest(502L, "   ");
        InterviewAnswerDto result = interviewService.saveAnswer(10L, 100L, blankRequest);

        assertThat(result.getAnswerText()).isEqualTo("");
    }

    @Test
    @DisplayName("Should reject answer save when interview is already COMPLETED or EVALUATING")
    void testRejectSaveWhenSessionNotActive() {
        session.setStatus(InterviewSessionStatus.COMPLETED);
        when(sessionRepository.findById(100L)).thenReturn(Optional.of(session));

        SaveAnswerRequest request = new SaveAnswerRequest(501L, "Late answer submission.");
        assertThatThrownBy(() -> interviewService.saveAnswer(10L, 100L, request))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Cannot submit answers to an interview in status: COMPLETED");

        verify(answerRepository, never()).saveAndFlush(any());
    }

    @Test
    @DisplayName("Should reject answer save if question does not belong to the session")
    void testRejectSaveWhenQuestionNotBelongingToSession() {
        InterviewSession anotherSession = new InterviewSession();
        anotherSession.setId(999L);
        question1.setSession(anotherSession);

        when(sessionRepository.findById(100L)).thenReturn(Optional.of(session));
        when(questionRepository.findById(501L)).thenReturn(Optional.of(question1));

        SaveAnswerRequest request = new SaveAnswerRequest(501L, "Valid text.");
        assertThatThrownBy(() -> interviewService.saveAnswer(10L, 100L, request))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Question does not belong to this interview session");

        verify(answerRepository, never()).saveAndFlush(any());
    }

    @Test
    @DisplayName("Should return all saved answers when loading interview session detail")
    void testSessionDetailReturnsSavedAnswers() {
        InterviewAnswer ans1 = new InterviewAnswer();
        ans1.setId(1L);
        ans1.setSession(session);
        ans1.setQuestion(question1);
        ans1.setAnswerText("Saved answer for question 1.");
        ans1.setSubmittedAt(LocalDateTime.now());

        when(sessionRepository.findById(100L)).thenReturn(Optional.of(session));
        when(questionRepository.findBySessionIdOrderByQuestionOrderAsc(100L)).thenReturn(List.of(question1, question2));
        when(answerRepository.findBySessionId(100L)).thenReturn(List.of(ans1));

        InterviewSessionDetailDto detail = interviewService.getSessionDetail(10L, 100L);

        assertThat(detail).isNotNull();
        assertThat(detail.getAnswers()).hasSize(1);
        assertThat(detail.getAnswers().get(0).getQuestionId()).isEqualTo(501L);
        assertThat(detail.getAnswers().get(0).getAnswerText()).isEqualTo("Saved answer for question 1.");
    }

    @Test
    @DisplayName("Should evaluate candidate answers and link rubric evaluations upon submission")
    void testSubmitInterviewEvaluatesSavedAnswers() {
        InterviewAnswer ans1 = new InterviewAnswer();
        ans1.setId(1L);
        ans1.setSession(session);
        ans1.setQuestion(question1);
        ans1.setAnswerText("ConcurrentHashMap segments write access via node-level synchronization, allowing concurrent reads without blocking.");
        ans1.setSubmittedAt(LocalDateTime.now());

        when(sessionRepository.findById(100L)).thenReturn(Optional.of(session));
        when(questionRepository.findBySessionIdOrderByQuestionOrderAsc(100L)).thenReturn(List.of(question1, question2));
        when(answerRepository.findBySessionId(100L)).thenReturn(List.of(ans1));
        when(answerRepository.saveAndFlush(any(InterviewAnswer.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(sessionRepository.save(any(InterviewSession.class))).thenReturn(session);

        AIService.EvaluatedAnswerData evalData = new AIService.EvaluatedAnswerData(
                85, 90, 85, 80, 86, "Strong explanation", "None", "Exemplary answer"
        );
        when(aiService.evaluateAnswer(anyString(), anyString(), anyString())).thenReturn(evalData);
        when(evaluationRepository.findByAnswerId(any())).thenReturn(Optional.empty());

        InterviewSessionDetailDto result = interviewService.submitInterview(10L, 100L);

        assertThat(result).isNotNull();
        assertThat(session.getStatus()).isEqualTo(InterviewSessionStatus.COMPLETED);
        verify(evaluationRepository, atLeastOnce()).save(any(AnswerEvaluation.class));
    }
}