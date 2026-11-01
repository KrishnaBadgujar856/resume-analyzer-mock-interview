package com.resumeanalyzer.service.resume;

import com.resumeanalyzer.dto.resume.*;
import com.resumeanalyzer.entity.*;
import com.resumeanalyzer.exception.BadRequestException;
import com.resumeanalyzer.exception.ResourceNotFoundException;
import com.resumeanalyzer.exception.UnauthorizedAccessException;
import com.resumeanalyzer.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ResumeService {

    private final ResumeRepository resumeRepository;
    private final UserRepository userRepository;
    private final ResumeVersionRepository versionRepository;
    private final ResumeSkillRepository skillRepository;
    private final ResumeAnalysisRepository analysisRepository;
    private final ResumeSuggestionRepository suggestionRepository;
    private final FileStorageService fileStorageService;
    private final DocumentExtractionService documentExtractionService;
    private final ResumeAnalysisService resumeAnalysisService;

    public ResumeService(ResumeRepository resumeRepository,
                         UserRepository userRepository,
                         ResumeVersionRepository versionRepository,
                         ResumeSkillRepository skillRepository,
                         ResumeAnalysisRepository analysisRepository,
                         ResumeSuggestionRepository suggestionRepository,
                         FileStorageService fileStorageService,
                         DocumentExtractionService documentExtractionService,
                         ResumeAnalysisService resumeAnalysisService) {
        this.resumeRepository = resumeRepository;
        this.userRepository = userRepository;
        this.versionRepository = versionRepository;
        this.skillRepository = skillRepository;
        this.analysisRepository = analysisRepository;
        this.suggestionRepository = suggestionRepository;
        this.fileStorageService = fileStorageService;
        this.documentExtractionService = documentExtractionService;
        this.resumeAnalysisService = resumeAnalysisService;
    }

    // 1. CREATE (Upload & Register)
    @Transactional
    public ResumeDetailDto uploadResume(Long userId, MultipartFile file, String title, String targetRole) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        // Store private file
        FileStorageService.StoredFileResult stored = fileStorageService.storeFile(file);

        String resumeTitle = (title != null && !title.isBlank()) ? title.trim() : stored.getOriginalFilename();
        String role = (targetRole != null && !targetRole.isBlank()) ? targetRole.trim() : user.getTargetRole();

        Resume resume = new Resume();
        resume.setUser(user);
        resume.setTitle(resumeTitle);
        resume.setOriginalFilename(stored.getOriginalFilename());
        resume.setStoredFilename(stored.getStoredFilename());
        resume.setFileType(stored.getFileType());
        resume.setFileSize(stored.getSize());
        resume.setTargetRole(role);
        resume.setStatus(ResumeStatus.PROCESSING);

        Resume savedResume = resumeRepository.save(resume);

        try {
            // Extract text
            String extractedText = documentExtractionService.extractText(stored.getPath(), stored.getFileType());
            savedResume.setExtractedText(extractedText);

            // Create initial Version 1
            ResumeVersion initialVersion = new ResumeVersion(savedResume, 1, "Initial Upload", extractedText);
            versionRepository.save(initialVersion);

            // Perform automatic analysis
            resumeAnalysisService.analyzeResume(savedResume, role);

        } catch (Exception e) {
            fileStorageService.deleteFile(stored.getStoredFilename());
            savedResume.setStatus(ResumeStatus.FAILED);
            savedResume.setErrorMessage(e.getMessage());
            resumeRepository.save(savedResume);
            throw e;
        }

        return getResumeDetail(userId, savedResume.getId());
    }

    // 2. READ (List & Server-Side Search)
    @Transactional(readOnly = true)
    public List<ResumeSummaryDto> getResumes(Long userId, String searchQuery) {
        List<Resume> resumes;
        if (searchQuery != null && !searchQuery.isBlank()) {
            resumes = resumeRepository.searchResumes(userId, searchQuery.trim());
        } else {
            resumes = resumeRepository.findByUserIdOrderByCreatedAtDesc(userId);
        }

        return resumes.stream().map(this::toSummaryDto).collect(Collectors.toList());
    }

    // 2. READ (Single Resume Detail)
    @Transactional(readOnly = true)
    public ResumeDetailDto getResumeDetail(Long userId, Long resumeId) {
        Resume resume = resumeRepository.findById(resumeId)
                .orElseThrow(() -> new ResourceNotFoundException("Resume not found with id: " + resumeId));

        if (!resume.getUser().getId().equals(userId)) {
            throw new UnauthorizedAccessException("You are not authorized to view this resume.");
        }

        ResumeDetailDto dto = new ResumeDetailDto();
        dto.setId(resume.getId());
        dto.setTitle(resume.getTitle());
        dto.setOriginalFilename(resume.getOriginalFilename());
        dto.setFileType(resume.getFileType());
        dto.setFileSize(resume.getFileSize());
        dto.setTargetRole(resume.getTargetRole());
        dto.setStatus(resume.getStatus());
        dto.setExtractedText(resume.getExtractedText());
        dto.setErrorMessage(resume.getErrorMessage());
        dto.setCreatedAt(resume.getCreatedAt());
        dto.setUpdatedAt(resume.getUpdatedAt());

        // Skills
        List<ResumeSkill> skills = skillRepository.findByResumeId(resumeId);
        dto.setSkills(skills.stream()
                .map(s -> new ResumeSkillDto(s.getId(), s.getName(), s.getCategory()))
                .collect(Collectors.toList()));

        // Versions
        List<ResumeVersion> versions = versionRepository.findByResumeIdOrderByVersionNumberDesc(resumeId);
        dto.setVersions(versions.stream()
                .map(v -> new ResumeVersionDto(v.getId(), v.getVersionNumber(), v.getVersionNote(), v.getContentText(), v.getCreatedAt()))
                .collect(Collectors.toList()));

        // Latest Analysis
        analysisRepository.findTopByResumeIdOrderByCreatedAtDesc(resumeId)
                .ifPresent(analysis -> dto.setLatestAnalysis(resumeAnalysisService.toDto(analysis)));

        return dto;
    }

    // 3. UPDATE (Metadata)
    @Transactional
    public ResumeDetailDto updateResume(Long userId, Long resumeId, UpdateResumeRequest request) {
        Resume resume = resumeRepository.findById(resumeId)
                .orElseThrow(() -> new ResourceNotFoundException("Resume not found with id: " + resumeId));

        if (!resume.getUser().getId().equals(userId)) {
            throw new UnauthorizedAccessException("You are not authorized to update this resume.");
        }

        resume.setTitle(request.getTitle().trim());
        if (request.getTargetRole() != null) {
            resume.setTargetRole(request.getTargetRole().trim());
        }

        resumeRepository.save(resume);
        return getResumeDetail(userId, resumeId);
    }

    // 4. DELETE (Safe Cascading & File Cleanup)
    @Transactional
    public void deleteResume(Long userId, Long resumeId) {
        Resume resume = resumeRepository.findById(resumeId)
                .orElseThrow(() -> new ResourceNotFoundException("Resume not found with id: " + resumeId));

        if (!resume.getUser().getId().equals(userId)) {
            throw new UnauthorizedAccessException("You are not authorized to delete this resume.");
        }

        String storedFilename = resume.getStoredFilename();

        // Delete from database
        resumeRepository.delete(resume);

        // Delete stored private file
        fileStorageService.deleteFile(storedFilename);
    }

    // 5. VERSIONING (Create New Version)
    @Transactional
    public ResumeVersionDto createVersion(Long userId, Long resumeId, CreateVersionRequest request) {
        Resume resume = resumeRepository.findById(resumeId)
                .orElseThrow(() -> new ResourceNotFoundException("Resume not found with id: " + resumeId));

        if (!resume.getUser().getId().equals(userId)) {
            throw new UnauthorizedAccessException("You are not authorized to edit this resume.");
        }

        int nextVersion = versionRepository.findTopByResumeIdOrderByVersionNumberDesc(resumeId)
                .map(v -> v.getVersionNumber() + 1)
                .orElse(1);

        String note = (request.getVersionNote() != null && !request.getVersionNote().isBlank())
                ? request.getVersionNote().trim()
                : "Version " + nextVersion;

        ResumeVersion version = new ResumeVersion(resume, nextVersion, note, request.getContentText());
        ResumeVersion saved = versionRepository.save(version);

        // Update active resume text and re-trigger analysis
        resume.setExtractedText(request.getContentText());
        resumeRepository.save(resume);

        resumeAnalysisService.analyzeResume(resume, resume.getTargetRole());

        return new ResumeVersionDto(saved.getId(), saved.getVersionNumber(), saved.getVersionNote(), saved.getContentText(), saved.getCreatedAt());
    }

    // 6. RE-ANALYZE
    @Transactional
    public ResumeAnalysisDto reanalyze(Long userId, Long resumeId, String targetRole) {
        Resume resume = resumeRepository.findById(resumeId)
                .orElseThrow(() -> new ResourceNotFoundException("Resume not found with id: " + resumeId));

        if (!resume.getUser().getId().equals(userId)) {
            throw new UnauthorizedAccessException("You are not authorized to reanalyze this resume.");
        }

        String role = (targetRole != null && !targetRole.isBlank()) ? targetRole.trim() : resume.getTargetRole();
        ResumeAnalysis analysis = resumeAnalysisService.analyzeResume(resume, role);
        return resumeAnalysisService.toDto(analysis);
    }

    // 7. SUGGESTION REVIEW (Accept / Reject)
    @Transactional
    public ResumeSuggestionDto updateSuggestionStatus(Long userId, Long suggestionId, UpdateSuggestionStatusRequest request) {
        ResumeSuggestion suggestion = suggestionRepository.findById(suggestionId)
                .orElseThrow(() -> new ResourceNotFoundException("Suggestion not found with id: " + suggestionId));

        if (!suggestion.getAnalysis().getResume().getUser().getId().equals(userId)) {
            throw new UnauthorizedAccessException("You are not authorized to modify this suggestion.");
        }

        suggestion.setStatus(request.getStatus());
        ResumeSuggestion updated = suggestionRepository.save(suggestion);

        return new ResumeSuggestionDto(
                updated.getId(),
                updated.getCategory(),
                updated.getIssueDescription(),
                updated.getSuggestedAction(),
                updated.getOriginalSnippet(),
                updated.getSuggestedSnippet(),
                updated.getStatus()
        );
    }

    private ResumeSummaryDto toSummaryDto(Resume resume) {
        Integer overallScore = analysisRepository.findTopByResumeIdOrderByCreatedAtDesc(resume.getId())
                .map(ResumeAnalysis::getOverallScore)
                .orElse(null);

        int versionCount = resume.getVersions() != null ? resume.getVersions().size() : 0;
        int skillCount = resume.getSkills() != null ? resume.getSkills().size() : 0;

        return new ResumeSummaryDto(
                resume.getId(),
                resume.getTitle(),
                resume.getOriginalFilename(),
                resume.getFileType(),
                resume.getFileSize(),
                resume.getTargetRole(),
                resume.getStatus(),
                overallScore,
                versionCount,
                skillCount,
                resume.getCreatedAt(),
                resume.getUpdatedAt()
        );
    }
}
