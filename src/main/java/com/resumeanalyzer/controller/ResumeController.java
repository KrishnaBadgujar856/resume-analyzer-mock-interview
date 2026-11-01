package com.resumeanalyzer.controller;

import com.resumeanalyzer.dto.resume.*;
import com.resumeanalyzer.security.CustomUserPrincipal;
import com.resumeanalyzer.service.resume.ResumeService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/resumes")
public class ResumeController {

    private final ResumeService resumeService;

    public ResumeController(ResumeService resumeService) {
        this.resumeService = resumeService;
    }

    // 1. CREATE - Upload Resume
    @PostMapping
    public ResponseEntity<ResumeDetailDto> uploadResume(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "title", required = false) String title,
            @RequestParam(value = "targetRole", required = false) String targetRole) {
        ResumeDetailDto response = resumeService.uploadResume(principal.getId(), file, title, targetRole);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // 2. READ - List Resumes & Server-side Search
    @GetMapping
    public ResponseEntity<List<ResumeSummaryDto>> getResumes(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @RequestParam(value = "search", required = false) String search) {
        List<ResumeSummaryDto> resumes = resumeService.getResumes(principal.getId(), search);
        return ResponseEntity.ok(resumes);
    }

    // 2. READ - Single Resume Detail
    @GetMapping("/{id}")
    public ResponseEntity<ResumeDetailDto> getResumeDetail(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @PathVariable Long id) {
        ResumeDetailDto resume = resumeService.getResumeDetail(principal.getId(), id);
        return ResponseEntity.ok(resume);
    }

    // 3. UPDATE - Edit Title / Target Role
    @PutMapping("/{id}")
    public ResponseEntity<ResumeDetailDto> updateResume(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @PathVariable Long id,
            @Valid @RequestBody UpdateResumeRequest request) {
        ResumeDetailDto updated = resumeService.updateResume(principal.getId(), id, request);
        return ResponseEntity.ok(updated);
    }

    // 4. DELETE - Delete Resume
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> deleteResume(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @PathVariable Long id) {
        resumeService.deleteResume(principal.getId(), id);
        return ResponseEntity.ok(Map.of("message", "Resume deleted successfully."));
    }

    // 5. VERSIONING - Save Revised Version
    @PostMapping("/{id}/versions")
    public ResponseEntity<ResumeVersionDto> createVersion(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @PathVariable Long id,
            @Valid @RequestBody CreateVersionRequest request) {
        ResumeVersionDto version = resumeService.createVersion(principal.getId(), id, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(version);
    }

    // 6. RE-ANALYZE
    @PostMapping("/{id}/analyze")
    public ResponseEntity<ResumeAnalysisDto> reanalyze(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @PathVariable Long id,
            @RequestParam(value = "targetRole", required = false) String targetRole) {
        ResumeAnalysisDto analysis = resumeService.reanalyze(principal.getId(), id, targetRole);
        return ResponseEntity.ok(analysis);
    }

    // 7. SUGGESTION REVIEW - Accept / Reject Suggestion
    @PatchMapping("/suggestions/{suggestionId}")
    public ResponseEntity<ResumeSuggestionDto> updateSuggestion(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @PathVariable Long suggestionId,
            @Valid @RequestBody UpdateSuggestionStatusRequest request) {
        ResumeSuggestionDto updated = resumeService.updateSuggestionStatus(principal.getId(), suggestionId, request);
        return ResponseEntity.ok(updated);
    }
}
