package com.resumeanalyzer.dto.interview;

public class AnswerEvaluationDto {

    private Long id;
    private Integer correctnessScore;
    private Integer understandingScore;
    private Integer relevanceScore;
    private Integer clarityScore;
    private Integer totalScore;
    private String feedback;
    private String missingPoints;
    private String sampleImprovedAnswer;

    public AnswerEvaluationDto() {}

    public AnswerEvaluationDto(Long id, Integer correctnessScore, Integer understandingScore,
                               Integer relevanceScore, Integer clarityScore, Integer totalScore,
                               String feedback, String missingPoints, String sampleImprovedAnswer) {
        this.id = id;
        this.correctnessScore = correctnessScore;
        this.understandingScore = understandingScore;
        this.relevanceScore = relevanceScore;
        this.clarityScore = clarityScore;
        this.totalScore = totalScore;
        this.feedback = feedback;
        this.missingPoints = missingPoints;
        this.sampleImprovedAnswer = sampleImprovedAnswer;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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
}
