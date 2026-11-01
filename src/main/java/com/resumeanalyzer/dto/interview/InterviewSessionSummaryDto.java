package com.resumeanalyzer.dto.interview;

import com.resumeanalyzer.entity.DifficultyLevel;
import com.resumeanalyzer.entity.InterviewSessionStatus;
import com.resumeanalyzer.entity.InterviewType;

import java.time.LocalDateTime;

public class InterviewSessionSummaryDto {

    private Long id;
    private Long resumeId;
    private String resumeTitle;
    private String targetRole;
    private InterviewType interviewType;
    private DifficultyLevel difficulty;
    private String focusArea;
    private Integer questionCount;
    private Integer answeredCount;
    private InterviewSessionStatus status;
    private Integer overallScore;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public InterviewSessionSummaryDto() {}

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getResumeId() {
        return resumeId;
    }

    public void setResumeId(Long resumeId) {
        this.resumeId = resumeId;
    }

    public String getResumeTitle() {
        return resumeTitle;
    }

    public void setResumeTitle(String resumeTitle) {
        this.resumeTitle = resumeTitle;
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

    public Integer getAnsweredCount() {
        return answeredCount;
    }

    public void setAnsweredCount(Integer answeredCount) {
        this.answeredCount = answeredCount;
    }

    public InterviewSessionStatus getStatus() {
        return status;
    }

    public void setStatus(InterviewSessionStatus status) {
        this.status = status;
    }

    public Integer getOverallScore() {
        return overallScore;
    }

    public void setOverallScore(Integer overallScore) {
        this.overallScore = overallScore;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
