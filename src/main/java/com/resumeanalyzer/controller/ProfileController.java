package com.resumeanalyzer.controller;

import com.resumeanalyzer.dto.auth.ChangePasswordRequest;
import com.resumeanalyzer.dto.auth.UpdateProfileRequest;
import com.resumeanalyzer.dto.auth.UserProfileDto;
import com.resumeanalyzer.security.CustomUserPrincipal;
import com.resumeanalyzer.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/profile")
public class ProfileController {

    private final AuthService authService;

    public ProfileController(AuthService authService) {
        this.authService = authService;
    }

    @GetMapping
    public ResponseEntity<UserProfileDto> getProfile(@AuthenticationPrincipal CustomUserPrincipal principal) {
        UserProfileDto profile = authService.getProfile(principal.getId());
        return ResponseEntity.ok(profile);
    }

    @PutMapping
    public ResponseEntity<UserProfileDto> updateProfile(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @Valid @RequestBody UpdateProfileRequest request) {
        UserProfileDto updated = authService.updateProfile(principal.getId(), request);
        return ResponseEntity.ok(updated);
    }

    @PostMapping("/password")
    public ResponseEntity<Map<String, String>> changePassword(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @Valid @RequestBody ChangePasswordRequest request) {
        authService.changePassword(principal.getId(), request);
        return ResponseEntity.ok(Map.of("message", "Password changed successfully."));
    }
}
