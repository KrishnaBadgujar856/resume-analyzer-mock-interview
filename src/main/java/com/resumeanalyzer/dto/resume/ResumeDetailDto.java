package com.resumeanalyzer.dto.resume;

import com.resumeanalyzer.entity.FileType;
import com.resumeanalyzer.entity.ResumeStatus;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class ResumeDetailDto {

    private Long id;
    private String title;
    private String originalFilename;
    private FileType fileType;
    private Long fileSize;
    private String targetRole;
    private ResumeStatus status;
    private String extractedText;
    private String errorMessage;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<ResumeSkillDto> skills = new ArrayList<>();
    private List<ResumeVersionDto> versions = new ArrayList<>();
    private ResumeAnalysisDto latestAnalysis;

    public ResumeDetailDto() {}

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getOriginalFilename() {
        return originalFilename;
    }

    public void setOriginalFilename(String originalFilename) {
        this.originalFilename = originalFilename;
    }

    public FileType getFileType() {
        return fileType;
    }

    public void setFileType(FileType fileType) {
        this.fileType = fileType;
    }

    public Long getFileSize() {
        return fileSize;
    }

    public void setFileSize(Long fileSize) {
        this.fileSize = fileSize;
    }

    public String getTargetRole() {
        return targetRole;
    }

    public void setTargetRole(String targetRole) {
        this.targetRole = targetRole;
    }

    public ResumeStatus getStatus() {
        return status;
    }

    public void setStatus(ResumeStatus status) {
        this.status = status;
    }

    public String getExtractedText() {
        return extractedText;
    }

    public void setExtractedText(String extractedText) {
        this.extractedText = extractedText;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
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

    public List<ResumeSkillDto> getSkills() {
        return skills;
    }

    public void setSkills(List<ResumeSkillDto> skills) {
        this.skills = skills;
    }

    public List<ResumeVersionDto> getVersions() {
        return versions;
    }

    public void setVersions(List<ResumeVersionDto> versions) {
        this.versions = versions;
    }

    public ResumeAnalysisDto getLatestAnalysis() {
        return latestAnalysis;
    }

    public void setLatestAnalysis(ResumeAnalysisDto latestAnalysis) {
        this.latestAnalysis = latestAnalysis;
    }
}
