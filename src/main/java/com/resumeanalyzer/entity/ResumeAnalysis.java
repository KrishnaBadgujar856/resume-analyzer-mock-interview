package com.resumeanalyzer.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "resume_analyses")
public class ResumeAnalysis {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "resume_id", nullable = false)
    private Resume resume;

    @Column(name = "overall_score", nullable = false)
    private Integer overallScore;

    @Column(name = "section_completeness_score", nullable = false)
    private Integer sectionCompletenessScore;

    @Column(name = "readability_score", nullable = false)
    private Integer readabilityScore;

    @Column(name = "relevance_score", nullable = false)
    private Integer relevanceScore;

    @Column(name = "target_role", length = 100)
    private String targetRole;

    @Column(columnDefinition = "TEXT")
    private String summary;

    @Column(name = "strengths_json", columnDefinition = "TEXT")
    private String strengthsJson;

    @Column(name = "weaknesses_json", columnDefinition = "TEXT")
    private String weaknessesJson;

    @Column(name = "missing_sections_json", columnDefinition = "TEXT")
    private String missingSectionsJson;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @OneToMany(mappedBy = "analysis", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ResumeSuggestion> suggestions = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    public ResumeAnalysis() {}

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Resume getResume() {
        return resume;
    }

    public void setResume(Resume resume) {
        this.resume = resume;
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

    public String getStrengthsJson() {
        return strengthsJson;
    }

    public void setStrengthsJson(String strengthsJson) {
        this.strengthsJson = strengthsJson;
    }

    public String getWeaknessesJson() {
        return weaknessesJson;
    }

    public void setWeaknessesJson(String weaknessesJson) {
        this.weaknessesJson = weaknessesJson;
    }

    public String getMissingSectionsJson() {
        return missingSectionsJson;
    }

    public void setMissingSectionsJson(String missingSectionsJson) {
        this.missingSectionsJson = missingSectionsJson;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public List<ResumeSuggestion> getSuggestions() {
        return suggestions;
    }

    public void setSuggestions(List<ResumeSuggestion> suggestions) {
        this.suggestions = suggestions;
    }
}
