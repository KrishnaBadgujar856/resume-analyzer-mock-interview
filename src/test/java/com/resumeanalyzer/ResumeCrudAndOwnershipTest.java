package com.resumeanalyzer;

import com.resumeanalyzer.dto.resume.CreateVersionRequest;
import com.resumeanalyzer.dto.resume.ResumeDetailDto;
import com.resumeanalyzer.dto.resume.ResumeSummaryDto;
import com.resumeanalyzer.dto.resume.UpdateResumeRequest;
import com.resumeanalyzer.entity.FileType;
import com.resumeanalyzer.entity.Resume;
import com.resumeanalyzer.entity.ResumeStatus;
import com.resumeanalyzer.entity.User;
import com.resumeanalyzer.exception.UnauthorizedAccessException;
import com.resumeanalyzer.repository.*;
import com.resumeanalyzer.service.resume.DocumentExtractionService;
import com.resumeanalyzer.service.resume.FileStorageService;
import com.resumeanalyzer.service.resume.ResumeAnalysisService;
import com.resumeanalyzer.service.resume.ResumeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ResumeCrudAndOwnershipTest {

    @Mock private ResumeRepository resumeRepository;
    @Mock private UserRepository userRepository;
    @Mock private ResumeVersionRepository versionRepository;
    @Mock private ResumeSkillRepository skillRepository;
    @Mock private ResumeAnalysisRepository analysisRepository;
    @Mock private ResumeSuggestionRepository suggestionRepository;
    @Mock private FileStorageService fileStorageService;
    @Mock private DocumentExtractionService documentExtractionService;
    @Mock private ResumeAnalysisService resumeAnalysisService;

    private ResumeService resumeService;

    private User userA;
    private User userB;

    @BeforeEach
    void setUp() {
        resumeService = new ResumeService(
                resumeRepository,
                userRepository,
                versionRepository,
                skillRepository,
                analysisRepository,
                suggestionRepository,
                fileStorageService,
                documentExtractionService,
                resumeAnalysisService
        );

        userA = new User("Alice", "alice@example.com", "hashA");
        userA.setId(1L);

        userB = new User("Bob", "bob@example.com", "hashB");
        userB.setId(2L);
    }

    @Test
    @DisplayName("Should successfully upload resume and trigger analysis (CREATE)")
    void testUploadResume() {
        MockMultipartFile file = new MockMultipartFile(
                "file", "resume.pdf", "application/pdf", "Valid resume content text".getBytes()
        );

        FileStorageService.StoredFileResult storedResult = new FileStorageService.StoredFileResult(
                "resume.pdf", "stored_uuid.pdf", Path.of("/uploads/stored_uuid.pdf"), FileType.PDF, 1024L
        );

        when(userRepository.findById(1L)).thenReturn(Optional.of(userA));
        when(fileStorageService.storeFile(file)).thenReturn(storedResult);
        when(documentExtractionService.extractText(any(), eq(FileType.PDF))).thenReturn("Extracted Resume Text Content");

        Resume savedResume = new Resume();
        savedResume.setId(50L);
        savedResume.setUser(userA);
        savedResume.setTitle("My PDF Resume");
        savedResume.setOriginalFilename("resume.pdf");
        savedResume.setStoredFilename("stored_uuid.pdf");
        savedResume.setFileType(FileType.PDF);
        savedResume.setFileSize(1024L);
        savedResume.setStatus(ResumeStatus.ANALYZED);
        savedResume.setExtractedText("Extracted Resume Text Content");

        when(resumeRepository.save(any(Resume.class))).thenReturn(savedResume);
        when(resumeRepository.findById(50L)).thenReturn(Optional.of(savedResume));
        when(skillRepository.findByResumeId(50L)).thenReturn(Collections.emptyList());
        when(versionRepository.findByResumeIdOrderByVersionNumberDesc(50L)).thenReturn(Collections.emptyList());

        ResumeDetailDto result = resumeService.uploadResume(1L, file, "My PDF Resume", "Java Developer");

        assertThat(result).isNotNull();
        assertThat(result.getTitle()).isEqualTo("My PDF Resume");
        verify(resumeAnalysisService, times(1)).analyzeResume(any(Resume.class), any());
    }

    @Test
    @DisplayName("Should search resumes using server-side repository query (SEARCH)")
    void testSearchResumes() {
        Resume r = new Resume();
        r.setId(10L);
        r.setUser(userA);
        r.setTitle("Spring Boot Specialist");
        r.setOriginalFilename("resume.pdf");
        r.setFileType(FileType.PDF);
        r.setFileSize(2048L);

        when(resumeRepository.searchResumes(1L, "Spring")).thenReturn(List.of(r));

        List<ResumeSummaryDto> results = resumeService.getResumes(1L, "Spring");

        assertThat(results).hasSize(1);
        assertThat(results.get(0).getTitle()).isEqualTo("Spring Boot Specialist");
        verify(resumeRepository, times(1)).searchResumes(1L, "Spring");
    }

    @Test
    @DisplayName("Should enforce ownership check: User B cannot view User A's resume")
    void testOwnershipEnforcementOnRead() {
        Resume resumeOwnedByA = new Resume();
        resumeOwnedByA.setId(99L);
        resumeOwnedByA.setUser(userA);

        when(resumeRepository.findById(99L)).thenReturn(Optional.of(resumeOwnedByA));

        assertThatThrownBy(() -> resumeService.getResumeDetail(2L, 99L))
                .isInstanceOf(UnauthorizedAccessException.class)
                .hasMessageContaining("not authorized to view this resume");
    }

    @Test
    @DisplayName("Should enforce ownership check: User B cannot delete User A's resume")
    void testOwnershipEnforcementOnDelete() {
        Resume resumeOwnedByA = new Resume();
        resumeOwnedByA.setId(99L);
        resumeOwnedByA.setUser(userA);

        when(resumeRepository.findById(99L)).thenReturn(Optional.of(resumeOwnedByA));

        assertThatThrownBy(() -> resumeService.deleteResume(2L, 99L))
                .isInstanceOf(UnauthorizedAccessException.class)
                .hasMessageContaining("not authorized to delete this resume");

        verify(resumeRepository, never()).delete(any());
        verify(fileStorageService, never()).deleteFile(any());
    }

    @Test
    @DisplayName("Should update resume title and role for authorized owner (UPDATE)")
    void testUpdateResume() {
        Resume resume = new Resume();
        resume.setId(15L);
        resume.setUser(userA);
        resume.setTitle("Old Title");
        resume.setOriginalFilename("file.pdf");
        resume.setFileType(FileType.PDF);
        resume.setFileSize(1024L);

        when(resumeRepository.findById(15L)).thenReturn(Optional.of(resume));
        when(skillRepository.findByResumeId(15L)).thenReturn(Collections.emptyList());
        when(versionRepository.findByResumeIdOrderByVersionNumberDesc(15L)).thenReturn(Collections.emptyList());

        UpdateResumeRequest req = new UpdateResumeRequest("Updated Modern Title", "Full Stack Engineer");
        ResumeDetailDto updated = resumeService.updateResume(1L, 15L, req);

        assertThat(updated.getTitle()).isEqualTo("Updated Modern Title");
        verify(resumeRepository, times(1)).save(resume);
    }
}
