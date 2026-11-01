package com.resumeanalyzer.dto.resume;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class ResumeAnalysisDto {

    private Long id;
    private Integer overallScore;
    private Integer sectionCompletenessScore;
    private Integer readabilityScore;
    private Integer relevanceScore;
    private String targetRole;
    private String summary;
    private List<String> strengths = new ArrayList<>();
    private List<String> weaknesses = new ArrayList<>();
    private List<String> missingSections = new ArrayList<>();
    private List<ResumeSuggestionDto> suggestions = new ArrayList<>();
    private LocalDateTime createdAt;

    public ResumeAnalysisDto() {}

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getOverallScore() {
        return overallScore;
    }

    public void setOverallScore(Integer overallScore) {
        this.overallScore = overallScore;
    }

    public Integer getSectionCompletenessScore() {
        return sectionCompletenessScore;
    }

    public void setSectionCompletenessScore(Integer sectionCompletenessScore) {
        this.sectionCompletenessScore = sectionCompletenessScore;
    }

    public Integer getReadabilityScore() {
        return readabilityScore;
    }

    public void setReadabilityScore(Integer readabilityScore) {
        this.readabilityScore = readabilityScore;
    }

    public Integer getRelevanceScore() {
        return relevanceScore;
    }

    public void setRelevanceScore(Integer relevanceScore) {
        this.relevanceScore = relevanceScore;
    }

    public String getTargetRole() {
        return targetRole;
    }

    public void setTargetRole(String targetRole) {
        this.targetRole = targetRole;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public List<String> getStrengths() {
        return strengths;
    }

    public void setStrengths(List<String> strengths) {
        this.strengths = strengths;
    }

    public List<String> getWeaknesses() {
        return weaknesses;
    }

    public void setWeaknesses(List<String> weaknesses) {
        this.weaknesses = weaknesses;
    }

    public List<String> getMissingSections() {
        return missingSections;
    }

    public void setMissingSections(List<String> missingSections) {
        this.missingSections = missingSections;
    }

    public List<ResumeSuggestionDto> getSuggestions() {
        return suggestions;
    }

    public void setSuggestions(List<ResumeSuggestionDto> suggestions) {
        this.suggestions = suggestions;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
