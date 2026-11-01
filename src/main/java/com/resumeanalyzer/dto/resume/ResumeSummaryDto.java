package com.resumeanalyzer.dto.resume;

import com.resumeanalyzer.entity.FileType;
import com.resumeanalyzer.entity.ResumeStatus;

import java.time.LocalDateTime;

public class ResumeSummaryDto {

    private Long id;
    private String title;
    private String originalFilename;
    private FileType fileType;
    private Long fileSize;
    private String targetRole;
    private ResumeStatus status;
    private Integer overallScore;
    private Integer versionCount;
    private Integer skillCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public ResumeSummaryDto() {}

    public ResumeSummaryDto(Long id, String title, String originalFilename, FileType fileType, Long fileSize,
                            String targetRole, ResumeStatus status, Integer overallScore,
                            Integer versionCount, Integer skillCount,
                            LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.title = title;
        this.originalFilename = originalFilename;
        this.fileType = fileType;
        this.fileSize = fileSize;
        this.targetRole = targetRole;
        this.status = status;
        this.overallScore = overallScore;
        this.versionCount = versionCount;
        this.skillCount = skillCount;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

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

    public Integer getOverallScore() {
        return overallScore;
    }

    public void setOverallScore(Integer overallScore) {
        this.overallScore = overallScore;
    }

    public Integer getVersionCount() {
        return versionCount;
    }

    public void setVersionCount(Integer versionCount) {
        this.versionCount = versionCount;
    }

    public Integer getSkillCount() {
        return skillCount;
    }

    public void setSkillCount(Integer skillCount) {
        this.skillCount = skillCount;
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
