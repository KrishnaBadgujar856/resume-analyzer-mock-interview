package com.resumeanalyzer.dto.interview;

import com.resumeanalyzer.entity.DifficultyLevel;

public class InterviewQuestionDto {

    private Long id;
    private Integer questionOrder;
    private String questionText;
    private String questionCategory;
    private DifficultyLevel difficulty;
    private String contextHint;

    public InterviewQuestionDto() {}

    public InterviewQuestionDto(Long id, Integer questionOrder, String questionText,
                                String questionCategory, DifficultyLevel difficulty, String contextHint) {
        this.id = id;
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
