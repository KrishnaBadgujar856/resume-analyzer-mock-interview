package com.resumeanalyzer.controller;

import com.resumeanalyzer.dto.interview.*;
import com.resumeanalyzer.entity.InterviewSessionStatus;
import com.resumeanalyzer.security.CustomUserPrincipal;
import com.resumeanalyzer.service.interview.InterviewService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/interviews")
public class InterviewController {

    private final InterviewService interviewService;

    public InterviewController(InterviewService interviewService) {
        this.interviewService = interviewService;
    }

    @PostMapping
    public ResponseEntity<InterviewSessionDetailDto> createSession(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @Valid @RequestBody CreateInterviewRequest request) {
        InterviewSessionDetailDto session = interviewService.createSession(principal.getId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(session);
    }

    @GetMapping
    public ResponseEntity<List<InterviewSessionSummaryDto>> getUserSessions(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @RequestParam(value = "role", required = false) String role,
            @RequestParam(value = "status", required = false) InterviewSessionStatus status) {
        List<InterviewSessionSummaryDto> list = interviewService.getUserSessions(principal.getId(), role, status);
        return ResponseEntity.ok(list);
    }

    @GetMapping("/{id}")
    public ResponseEntity<InterviewSessionDetailDto> getSessionDetail(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @PathVariable Long id) {
        InterviewSessionDetailDto session = interviewService.getSessionDetail(principal.getId(), id);
        return ResponseEntity.ok(session);
    }

    @PostMapping("/{id}/answers")
    public ResponseEntity<InterviewAnswerDto> saveAnswer(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @PathVariable Long id,
            @Valid @RequestBody SaveAnswerRequest request) {
        InterviewAnswerDto answer = interviewService.saveAnswer(principal.getId(), id, request);
        return ResponseEntity.ok(answer);
    }

    @PostMapping("/{id}/submit")
    public ResponseEntity<InterviewSessionDetailDto> submitInterview(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @PathVariable Long id) {
        InterviewSessionDetailDto result = interviewService.submitInterview(principal.getId(), id);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/{id}/results")
    public ResponseEntity<InterviewSessionDetailDto> getResults(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @PathVariable Long id) {
        InterviewSessionDetailDto result = interviewService.getSessionDetail(principal.getId(), id);
        return ResponseEntity.ok(result);
    }
}
