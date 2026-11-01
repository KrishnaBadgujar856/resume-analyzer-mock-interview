package com.resumeanalyzer.dto.interview;

import java.time.LocalDateTime;

public class InterviewAnswerDto {

    private Long id;
    private Long questionId;
    private String answerText;
    private LocalDateTime submittedAt;
    private AnswerEvaluationDto evaluation;

    public InterviewAnswerDto() {}

    public InterviewAnswerDto(Long id, Long questionId, String answerText, LocalDateTime submittedAt, AnswerEvaluationDto evaluation) {
        this.id = id;
        this.questionId = questionId;
        this.answerText = answerText;
        this.submittedAt = submittedAt;
        this.evaluation = evaluation;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getQuestionId() {
        return questionId;
    }

    public void setQuestionId(Long questionId) {
        this.questionId = questionId;
    }

    public String getAnswerText() {
        return answerText;
    }

    public void setAnswerText(String answerText) {
        this.answerText = answerText;
    }

    public LocalDateTime getSubmittedAt() {
        return submittedAt;
    }

    public void setSubmittedAt(LocalDateTime submittedAt) {
        this.submittedAt = submittedAt;
    }

    public AnswerEvaluationDto getEvaluation() {
        return evaluation;
    }

    public void setEvaluation(AnswerEvaluationDto evaluation) {
        this.evaluation = evaluation;
    }
}
