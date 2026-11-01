package com.resumeanalyzer.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "resume_suggestions")
public class ResumeSuggestion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "analysis_id", nullable = false)
    private ResumeAnalysis analysis;

    @Column(nullable = false, length = 100)
    private String category;

    @Column(name = "issue_description", columnDefinition = "TEXT", nullable = false)
    private String issueDescription;

    @Column(name = "suggested_action", columnDefinition = "TEXT", nullable = false)
    private String suggestedAction;

    @Column(name = "original_snippet", columnDefinition = "TEXT")
    private String originalSnippet;

    @Column(name = "suggested_snippet", columnDefinition = "TEXT")
    private String suggestedSnippet;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private SuggestionStatus status = SuggestionStatus.PENDING;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    public ResumeSuggestion() {}

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public ResumeAnalysis getAnalysis() {
        return analysis;
    }

    public void setAnalysis(ResumeAnalysis analysis) {
        this.analysis = analysis;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getIssueDescription() {
        return issueDescription;
    }

    public void setIssueDescription(String issueDescription) {
        this.issueDescription = issueDescription;
    }

    public String getSuggestedAction() {
        return suggestedAction;
    }

    public void setSuggestedAction(String suggestedAction) {
        this.suggestedAction = suggestedAction;
    }

    public String getOriginalSnippet() {
        return originalSnippet;
    }

    public void setOriginalSnippet(String originalSnippet) {
        this.originalSnippet = originalSnippet;
    }

    public String getSuggestedSnippet() {
        return suggestedSnippet;
    }

    public void setSuggestedSnippet(String suggestedSnippet) {
        this.suggestedSnippet = suggestedSnippet;
    }

    public SuggestionStatus getStatus() {
        return status;
    }

    public void setStatus(SuggestionStatus status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
