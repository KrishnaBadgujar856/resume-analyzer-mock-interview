package com.resumeanalyzer.dto;

import com.resumeanalyzer.dto.interview.InterviewSessionSummaryDto;
import com.resumeanalyzer.dto.resume.ResumeSummaryDto;

import java.util.List;

public class DashboardStatsDto {

    private long totalResumes;
    private long totalInterviews;
    private long completedInterviews;
    private Double averageScore;
    private List<ResumeSummaryDto> recentResumes;
    private List<InterviewSessionSummaryDto> recentInterviews;

    public DashboardStatsDto() {}

    public DashboardStatsDto(long totalResumes, long totalInterviews, long completedInterviews,
                             Double averageScore, List<ResumeSummaryDto> recentResumes,
                             List<InterviewSessionSummaryDto> recentInterviews) {
        this.totalResumes = totalResumes;
        this.totalInterviews = totalInterviews;
        this.completedInterviews = completedInterviews;
        this.averageScore = averageScore;
        this.recentResumes = recentResumes;
        this.recentInterviews = recentInterviews;
    }

    public long getTotalResumes() {
        return totalResumes;
    }

    public void setTotalResumes(long totalResumes) {
        this.totalResumes = totalResumes;
    }

    public long getTotalInterviews() {
        return totalInterviews;
    }

    public void setTotalInterviews(long totalInterviews) {
        this.totalInterviews = totalInterviews;
    }

    public long getCompletedInterviews() {
        return completedInterviews;
    }

    public void setCompletedInterviews(long completedInterviews) {
        this.completedInterviews = completedInterviews;
    }

    public Double getAverageScore() {
        return averageScore;
    }

    public void setAverageScore(Double averageScore) {
        this.averageScore = averageScore;
    }

    public List<ResumeSummaryDto> getRecentResumes() {
        return recentResumes;
    }

    public void setRecentResumes(List<ResumeSummaryDto> recentResumes) {
        this.recentResumes = recentResumes;
    }

    public List<InterviewSessionSummaryDto> getRecentInterviews() {
        return recentInterviews;
    }

    public void setRecentInterviews(List<InterviewSessionSummaryDto> recentInterviews) {
        this.recentInterviews = recentInterviews;
    }
}
