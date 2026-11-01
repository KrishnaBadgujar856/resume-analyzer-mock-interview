package com.resumeanalyzer.dto.interview;

import com.resumeanalyzer.entity.DifficultyLevel;
import com.resumeanalyzer.entity.InterviewType;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class CreateInterviewRequest {

    @NotNull(message = "Resume selection is required")
    private Long resumeId;

    @NotBlank(message = "Target job role is required")
    private String targetRole;

    @NotNull(message = "Interview type is required")
    private InterviewType interviewType = InterviewType.TECHNICAL;

    @NotNull(message = "Difficulty level is required")
    private DifficultyLevel difficulty = DifficultyLevel.INTERMEDIATE;

    private String focusArea;

    @Min(value = 3, message = "Question count must be at least 3")
    @Max(value = 10, message = "Question count cannot exceed 10")
    private Integer questionCount = 5;

    public CreateInterviewRequest() {}

    public Long getResumeId() {
        return resumeId;
    }

    public void setResumeId(Long resumeId) {
        this.resumeId = resumeId;
    }

    public String getTargetRole() {
        return targetRole;
    }

    public void setTargetRole(String targetRole) {
        this.targetRole = targetRole;
    }

    public InterviewType getInterviewType() {
        return interviewType;
    }

    public void setInterviewType(InterviewType interviewType) {
        this.interviewType = interviewType;
    }

    public DifficultyLevel getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(DifficultyLevel difficulty) {
        this.difficulty = difficulty;
    }

    public String getFocusArea() {
        return focusArea;
    }

    public void setFocusArea(String focusArea) {
        this.focusArea = focusArea;
    }

    public Integer getQuestionCount() {
        return questionCount;
    }

    public void setQuestionCount(Integer questionCount) {
        this.questionCount = questionCount;
    }
}
