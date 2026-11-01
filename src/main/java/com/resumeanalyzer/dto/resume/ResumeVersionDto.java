package com.resumeanalyzer.dto.resume;

import java.time.LocalDateTime;

public class ResumeVersionDto {
    private Long id;
    private Integer versionNumber;
    private String versionNote;
    private String contentText;
    private LocalDateTime createdAt;

    public ResumeVersionDto() {}

    public ResumeVersionDto(Long id, Integer versionNumber, String versionNote, String contentText, LocalDateTime createdAt) {
        this.id = id;
        this.versionNumber = versionNumber;
        this.versionNote = versionNote;
        this.contentText = contentText;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getVersionNumber() {
        return versionNumber;
    }

    public void setVersionNumber(Integer versionNumber) {
        this.versionNumber = versionNumber;
    }

    public String getVersionNote() {
        return versionNote;
    }

    public void setVersionNote(String versionNote) {
        this.versionNote = versionNote;
    }

    public String getContentText() {
        return contentText;
    }

    public void setContentText(String contentText) {
        this.contentText = contentText;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
