package com.resumeanalyzer.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "interview_questions")
public class InterviewQuestion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "session_id", nullable = false)
    private InterviewSession session;

    @Column(name = "question_order", nullable = false)
    private Integer questionOrder;

    @Column(name = "question_text", columnDefinition = "TEXT", nullable = false)
    private String questionText;

    @Column(name = "question_category", length = 100)
    private String questionCategory;

    @Enumerated(EnumType.STRING)
    @Column(length = 30)
    private DifficultyLevel difficulty;

    @Column(name = "context_hint", columnDefinition = "TEXT")
    private String contextHint;

    public InterviewQuestion() {}

    public InterviewQuestion(InterviewSession session, Integer questionOrder, String questionText,
                             String questionCategory, DifficultyLevel difficulty, String contextHint) {
        this.session = session;
        this.questionOrder = questionOrder;
        this.questionText = questionText;
        this.questionCategory = questionCategory;
        this.difficulty = difficulty;
        this.contextHint = contextHint;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public InterviewSession getSession() {
        return session;
    }

    public void setSession(InterviewSession session) {
        this.session = session;
    }

    public Integer getQuestionOrder() {
        return questionOrder;
    }

    public void setQuestionOrder(Integer questionOrder) {
        this.questionOrder = questionOrder;
    }

    public String getQuestionText() {
        return questionText;
    }

    public void setQuestionText(String questionText) {
        this.questionText = questionText;
    }

    public String getQuestionCategory() {
        return questionCategory;
    }

    public void setQuestionCategory(String questionCategory) {
        this.questionCategory = questionCategory;
    }

    public DifficultyLevel getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(DifficultyLevel difficulty) {
        this.difficulty = difficulty;
    }

    public String getContextHint() {
        return contextHint;
    }

    public void setContextHint(String contextHint) {
        this.contextHint = contextHint;
    }
}
