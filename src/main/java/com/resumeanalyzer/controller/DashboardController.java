package com.resumeanalyzer.controller;

import com.resumeanalyzer.dto.DashboardStatsDto;
import com.resumeanalyzer.dto.interview.InterviewSessionSummaryDto;
import com.resumeanalyzer.dto.resume.ResumeSummaryDto;
import com.resumeanalyzer.entity.InterviewSessionStatus;
import com.resumeanalyzer.repository.InterviewSessionRepository;
import com.resumeanalyzer.repository.ResumeRepository;
import com.resumeanalyzer.security.CustomUserPrincipal;
import com.resumeanalyzer.service.interview.InterviewService;
import com.resumeanalyzer.service.resume.ResumeService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    private final ResumeRepository resumeRepository;
    private final InterviewSessionRepository interviewSessionRepository;
    private final ResumeService resumeService;
    private final InterviewService interviewService;

    public DashboardController(ResumeRepository resumeRepository,
                               InterviewSessionRepository interviewSessionRepository,
                               ResumeService resumeService,
                               InterviewService interviewService) {
        this.resumeRepository = resumeRepository;
        this.interviewSessionRepository = interviewSessionRepository;
        this.resumeService = resumeService;
        this.interviewService = interviewService;
    }

    @GetMapping({"", "/stats"})
    public ResponseEntity<DashboardStatsDto> getDashboardStats(@AuthenticationPrincipal CustomUserPrincipal principal) {
        Long userId = principal.getId();

        long totalResumes = resumeRepository.countByUserId(userId);
        long totalInterviews = interviewSessionRepository.countByUserId(userId);
        long completedInterviews = interviewSessionRepository.countByUserIdAndStatus(userId, InterviewSessionStatus.COMPLETED);
        Double avgScore = interviewSessionRepository.getAverageScoreByUserId(userId);

        List<ResumeSummaryDto> recentResumes = resumeService.getResumes(userId, null).stream().limit(4).toList();
        List<InterviewSessionSummaryDto> recentInterviews = interviewService.getUserSessions(userId, null, null).stream().limit(4).toList();

        DashboardStatsDto dto = new DashboardStatsDto(
                totalResumes,
                totalInterviews,
                completedInterviews,
                avgScore != null ? Math.round(avgScore * 10.0) / 10.0 : null,
                recentResumes,
                recentInterviews
        );

        return ResponseEntity.ok(dto);
    }
}
