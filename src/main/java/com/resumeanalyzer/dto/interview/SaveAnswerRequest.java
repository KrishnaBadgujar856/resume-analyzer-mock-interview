package com.resumeanalyzer.dto.interview;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class SaveAnswerRequest {

    @NotNull(message = "Question ID is required")
    private Long questionId;

    @NotNull(message = "Answer text is required")
    @Size(max = 10000, message = "Answer must not exceed 10000 characters")
    private String answerText;

    public SaveAnswerRequest() {}

    public SaveAnswerRequest(Long questionId, String answerText) {
        this.questionId = questionId;
        this.answerText = answerText;
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
}
