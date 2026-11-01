package com.resumeanalyzer.dto.interview;

import com.resumeanalyzer.entity.DifficultyLevel;
import com.resumeanalyzer.entity.InterviewSessionStatus;
import com.resumeanalyzer.entity.InterviewType;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class InterviewSessionDetailDto {

    private Long id;
    private Long resumeId;
    private String resumeTitle;
    private String targetRole;
    private InterviewType interviewType;
    private DifficultyLevel difficulty;
    private String focusArea;
    private Integer questionCount;
    private InterviewSessionStatus status;
    private Integer overallScore;
    private String strengths;
    private String improvements;
    private String topicsToPrepare;
    private boolean demoMode;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private List<InterviewQuestionDto> questions = new ArrayList<>();
    private List<InterviewAnswerDto> answers = new ArrayList<>();

    public InterviewSessionDetailDto() {}

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

    public String getStrengths() {
        return strengths;
    }

    public void setStrengths(String strengths) {
        this.strengths = strengths;
    }

    public String getImprovements() {
        return improvements;
    }

    public void setImprovements(String improvements) {
        this.improvements = improvements;
    }

    public String getTopicsToPrepare() {
        return topicsToPrepare;
    }

    public void setTopicsToPrepare(String topicsToPrepare) {
        this.topicsToPrepare = topicsToPrepare;
    }

    public boolean isDemoMode() {
        return demoMode;
    }

    public void setDemoMode(boolean demoMode) {
        this.demoMode = demoMode;
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

    public List<InterviewQuestionDto> getQuestions() {
        return questions;
    }

    public void setQuestions(List<InterviewQuestionDto> questions) {
        this.questions = questions;
    }

    public List<InterviewAnswerDto> getAnswers() {
        return answers;
    }

    public void setAnswers(List<InterviewAnswerDto> answers) {
        this.answers = answers;
    }
}
