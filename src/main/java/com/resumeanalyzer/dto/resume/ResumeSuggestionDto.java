package com.resumeanalyzer.dto.resume;

import com.resumeanalyzer.entity.SuggestionStatus;

public class ResumeSuggestionDto {
    private Long id;
    private String category;
    private String issueDescription;
    private String suggestedAction;
    private String originalSnippet;
    private String suggestedSnippet;
    private SuggestionStatus status;

    public ResumeSuggestionDto() {}

    public ResumeSuggestionDto(Long id, String category, String issueDescription, String suggestedAction,
                               String originalSnippet, String suggestedSnippet, SuggestionStatus status) {
        this.id = id;
        this.category = category;
        this.issueDescription = issueDescription;
        this.suggestedAction = suggestedAction;
        this.originalSnippet = originalSnippet;
        this.suggestedSnippet = suggestedSnippet;
        this.status = status;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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
}
