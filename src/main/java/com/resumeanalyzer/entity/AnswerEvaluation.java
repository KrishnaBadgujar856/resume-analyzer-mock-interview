package com.resumeanalyzer.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "answer_evaluations")
public class AnswerEvaluation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "answer_id", nullable = false)
    private InterviewAnswer answer;

    @Column(name = "correctness_score", nullable = false)
    private Integer correctnessScore;

    @Column(name = "understanding_score", nullable = false)
    private Integer understandingScore;

    @Column(name = "relevance_score", nullable = false)
    private Integer relevanceScore;

    @Column(name = "clarity_score", nullable = false)
    private Integer clarityScore;

    @Column(name = "total_score", nullable = false)
    private Integer totalScore;

    @Column(columnDefinition = "TEXT")
    private String feedback;

    @Column(name = "missing_points", columnDefinition = "TEXT")
    private String missingPoints;

    @Column(name = "sample_improved_answer", columnDefinition = "TEXT")
    private String sampleImprovedAnswer;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    public AnswerEvaluation() {}

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public InterviewAnswer getAnswer() {
        return answer;
    }

    public void setAnswer(InterviewAnswer answer) {
        this.answer = answer;
    }

    public Integer getCorrectnessScore() {
        return correctnessScore;
    }

    public void setCorrectnessScore(Integer correctnessScore) {
        this.correctnessScore = correctnessScore;
    }

    public Integer getUnderstandingScore() {
        return understandingScore;
    }

    public void setUnderstandingScore(Integer understandingScore) {
        this.understandingScore = understandingScore;
    }

    public Integer getRelevanceScore() {
        return relevanceScore;
    }

    public void setRelevanceScore(Integer relevanceScore) {
        this.relevanceScore = relevanceScore;
    }

    public Integer getClarityScore() {
        return clarityScore;
    }

    public void setClarityScore(Integer clarityScore) {
        this.clarityScore = clarityScore;
    }

    public Integer getTotalScore() {
        return totalScore;
    }

    public void setTotalScore(Integer totalScore) {
        this.totalScore = totalScore;
    }

    public String getFeedback() {
        return feedback;
    }

    public void setFeedback(String feedback) {
        this.feedback = feedback;
    }

    public String getMissingPoints() {
        return missingPoints;
    }

    public void setMissingPoints(String missingPoints) {
        this.missingPoints = missingPoints;
    }

    public String getSampleImprovedAnswer() {
        return sampleImprovedAnswer;
    }

    public void setSampleImprovedAnswer(String sampleImprovedAnswer) {
        this.sampleImprovedAnswer = sampleImprovedAnswer;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
