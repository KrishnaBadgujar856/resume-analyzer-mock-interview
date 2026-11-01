package com.resumeanalyzer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.resumeanalyzer.entity.*;
import com.resumeanalyzer.exception.BadRequestException;
import com.resumeanalyzer.exception.DocumentProcessingException;
import com.resumeanalyzer.repository.ResumeAnalysisRepository;
import com.resumeanalyzer.repository.ResumeRepository;
import com.resumeanalyzer.repository.ResumeSkillRepository;
import com.resumeanalyzer.service.resume.DocumentExtractionService;
import com.resumeanalyzer.service.resume.FileStorageService;
import com.resumeanalyzer.service.resume.ResumeAnalysisService;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.encryption.AccessPermission;
import org.apache.pdfbox.pdmodel.encryption.StandardProtectionPolicy;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockMultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class DocumentExtractionAndValidationTest {

    @TempDir
    Path tempDir;

    private DocumentExtractionService extractionService;
    private FileStorageService storageService;

    @BeforeEach
    void setUp() {
        extractionService = new DocumentExtractionService();
        storageService = new FileStorageService(tempDir.toString());
    }

    // =========================================================================
    // 1. TEXT EXTRACTION: PDF & DOCX
    // =========================================================================

    @Test
    @DisplayName("Should correctly extract text from a valid text-based PDF")
    void testExtractValidPdf() throws IOException {
        File pdfFile = tempDir.resolve("valid_resume.pdf").toFile();
        try (PDDocument doc = new PDDocument()) {
            PDPage page = new PDPage();
            doc.addPage(page);
            try (PDPageContentStream cs = new PDPageContentStream(doc, page)) {
                cs.beginText();
                cs.setFont(PDType1Font.HELVETICA_BOLD, 12);
                cs.newLineAtOffset(50, 700);
                cs.showText("Alex Sharma - Senior Java Backend Developer");
                cs.newLineAtOffset(0, -20);
                cs.setFont(PDType1Font.HELVETICA, 10);
                cs.showText("Skills: Java, Spring Boot, MySQL, Docker, RESTful APIs");
                cs.endText();
            }
            doc.save(pdfFile);
        }

        String text = extractionService.extractText(pdfFile.toPath(), FileType.PDF);
        assertThat(text).isNotBlank();
        assertThat(text).contains("Alex Sharma");
        assertThat(text).contains("Java, Spring Boot, MySQL");
    }

    @Test
    @DisplayName("Should correctly extract text from a valid Word (.docx) document")
    void testExtractValidDocx() throws IOException {
        File docxFile = tempDir.resolve("valid_resume.docx").toFile();
        try (XWPFDocument doc = new XWPFDocument();
             FileOutputStream fos = new FileOutputStream(docxFile)) {
            XWPFParagraph p1 = doc.createParagraph();
            XWPFRun r1 = p1.createRun();
            r1.setText("Jordan Lee - Full Stack Software Engineer");

            XWPFParagraph p2 = doc.createParagraph();
            XWPFRun r2 = p2.createRun();
            r2.setText("Technical Expertise: React, TypeScript, Node.js, PostgreSQL, Docker, Kubernetes");

            doc.write(fos);
        }

        String text = extractionService.extractText(docxFile.toPath(), FileType.DOCX);
        assertThat(text).isNotBlank();
        assertThat(text).contains("Jordan Lee");
        assertThat(text).contains("React, TypeScript, Node.js");
    }

    // =========================================================================
    // 2. SCANNED PDF & INSUFFICIENT TEXT CHECKS
    // =========================================================================

    @Test
    @DisplayName("Should reject scanned/image-only PDF with clear, user-friendly error message")
    void testScannedPdfInsufficientTextThrowsUserFriendlyMessage() throws IOException {
        File scannedPdf = tempDir.resolve("scanned_image.pdf").toFile();
        try (PDDocument doc = new PDDocument()) {
            // A page with no text content represents an image-only / scanned document
            PDPage page = new PDPage();
            doc.addPage(page);
            doc.save(scannedPdf);
        }

        assertThatThrownBy(() -> extractionService.extractText(scannedPdf.toPath(), FileType.PDF))
                .isInstanceOf(DocumentProcessingException.class)
                .hasMessageContaining("insufficient selectable text")
                .hasMessageContaining("scanned document or image-based PDF")
                .hasMessageContaining("OCR (Optical Character Recognition) is not supported");
    }

    @Test
    @DisplayName("Should reject DOCX with insufficient text (< 50 characters)")
    void testDocxInsufficientTextThrowsFriendlyMessage() throws IOException {
        File sparseDocx = tempDir.resolve("sparse.docx").toFile();
        try (XWPFDocument doc = new XWPFDocument();
             FileOutputStream fos = new FileOutputStream(sparseDocx)) {
            XWPFParagraph p = doc.createParagraph();
            XWPFRun r = p.createRun();
            r.setText("Hi"); // Only 2 characters
            doc.write(fos);
        }

        assertThatThrownBy(() -> extractionService.extractText(sparseDocx.toPath(), FileType.DOCX))
                .isInstanceOf(DocumentProcessingException.class)
                .hasMessageContaining("insufficient readable text")
                .hasMessageContaining("fewer than 50 characters");
    }

    @Test
    @DisplayName("Should reject password-protected encrypted PDF with clear error message")
    void testEncryptedPdfThrowsFriendlyMessage() throws IOException {
        File encPdf = tempDir.resolve("encrypted.pdf").toFile();
        try (PDDocument doc = new PDDocument()) {
            PDPage page = new PDPage();
            doc.addPage(page);
            StandardProtectionPolicy policy = new StandardProtectionPolicy("ownerPass", "userPass", new AccessPermission());
            doc.protect(policy);
            doc.save(encPdf);
        }

        assertThatThrownBy(() -> extractionService.extractText(encPdf.toPath(), FileType.PDF))
                .isInstanceOf(DocumentProcessingException.class)
                .hasMessageContaining("password protected");
    }

    // =========================================================================
    // 3. FILE VALIDATION & SECURITY IN FILE STORAGE SERVICE
    // =========================================================================

    @Test
    @DisplayName("Should accept valid PDF file with genuine %PDF header")
    void testFileStorageAcceptsValidPdf() {
        byte[] pdfBytes = "%PDF-1.4\n1 0 obj\n<<>>\nendobj\ntrailer\n<<>>\n%%EOF".getBytes();
        MockMultipartFile file = new MockMultipartFile("file", "resume.pdf", "application/pdf", pdfBytes);

        FileStorageService.StoredFileResult result = storageService.storeFile(file);
        assertThat(result).isNotNull();
        assertThat(result.getFileType()).isEqualTo(FileType.PDF);
        assertThat(result.getStoredFilename()).endsWith(".pdf");
    }

    @Test
    @DisplayName("Should accept valid DOCX file with genuine PK ZIP header")
    void testFileStorageAcceptsValidDocx() {
        byte[] docxBytes = new byte[]{0x50, 0x4B, 0x03, 0x04, 0x14, 0x00, 0x06, 0x00};
        MockMultipartFile file = new MockMultipartFile("file", "resume.docx", "application/vnd.openxmlformats-officedocument.wordprocessingml.document", docxBytes);

        FileStorageService.StoredFileResult result = storageService.storeFile(file);
        assertThat(result).isNotNull();
        assertThat(result.getFileType()).isEqualTo(FileType.DOCX);
        assertThat(result.getStoredFilename()).endsWith(".docx");
    }

    @Test
    @DisplayName("Should reject spoofed PDF file lacking %PDF signature")
    void testFileStorageRejectsSpoofedPdf() {
        byte[] fakePdfBytes = "This is a plain text file pretending to be a PDF".getBytes();
        MockMultipartFile file = new MockMultipartFile("file", "fake.pdf", "application/pdf", fakePdfBytes);

        assertThatThrownBy(() -> storageService.storeFile(file))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("does not contain a valid PDF signature");
    }

    @Test
    @DisplayName("Should reject spoofed DOCX file lacking PK ZIP signature")
    void testFileStorageRejectsSpoofedDocx() {
        byte[] fakeDocxBytes = "Not a zip archive at all".getBytes();
        MockMultipartFile file = new MockMultipartFile("file", "fake.docx", "application/vnd.openxmlformats-officedocument.wordprocessingml.document", fakeDocxBytes);

        assertThatThrownBy(() -> storageService.storeFile(file))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("does not contain a valid Word (.docx) package signature");
    }

    @Test
    @DisplayName("Should reject empty file (0 bytes)")
    void testFileStorageRejectsEmptyFile() {
        MockMultipartFile file = new MockMultipartFile("file", "empty.pdf", "application/pdf", new byte[0]);

        assertThatThrownBy(() -> storageService.storeFile(file))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Uploaded file is empty");
    }

    @Test
    @DisplayName("Should reject unsupported file extensions (e.g. .png, .txt)")
    void testFileStorageRejectsUnsupportedExtension() {
        MockMultipartFile file = new MockMultipartFile("file", "image.png", "image/png", new byte[]{1, 2, 3, 4});

        assertThatThrownBy(() -> storageService.storeFile(file))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Unsupported file type");
    }

    @Test
    @DisplayName("Should reject files exceeding 10MB limit")
    void testFileStorageRejectsOversizedFile() {
        // Mock a file larger than 10MB (11MB)
        MockMultipartFile oversized = new MockMultipartFile("file", "huge.pdf", "application/pdf", new byte[10]) {
            @Override
            public long getSize() {
                return 11 * 1024 * 1024L; // 11 MB
            }
        };

        assertThatThrownBy(() -> storageService.storeFile(oversized))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("File size exceeds the 10MB limit");
    }

    @Test
    @DisplayName("Should reject path traversal attempts in filename")
    void testFileStorageRejectsPathTraversal() {
        MockMultipartFile file = new MockMultipartFile("file", "../../evil.pdf", "application/pdf", "%PDF-1.4 test".getBytes());

        assertThatThrownBy(() -> storageService.storeFile(file))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("invalid path sequence");
    }

    // =========================================================================
    // 4. VERIFY EXTRACTED TEXT REACHES ANALYSIS SERVICE & IS SAVED
    // =========================================================================

    @Test
    @DisplayName("Should verify that successfully extracted resume text reaches analysis service and is saved")
    void testExtractedTextReachesAnalysisAndSavesCorrectly() {
        ResumeAnalysisRepository analysisRepo = mock(ResumeAnalysisRepository.class);
        ResumeSkillRepository skillRepo = mock(ResumeSkillRepository.class);
        ResumeRepository resumeRepo = mock(ResumeRepository.class);
        ObjectMapper objectMapper = new ObjectMapper();

        ResumeAnalysisService analysisService = new ResumeAnalysisService(analysisRepo, skillRepo, resumeRepo, objectMapper);

        String sampleText = """
                Jane Doe
                Email: jane.doe@example.com | Phone: +1-555-0144 | github.com/janedoe | linkedin.com/in/janedoe

                PROFESSIONAL SUMMARY
                Experienced Backend Software Engineer specializing in Java, Spring Boot microservices, and database performance tuning.

                EDUCATION
                Bachelor of Science in Computer Science, 2023

                TECHNICAL SKILLS
                Languages: Java, SQL, Python
                Frameworks: Spring Boot, Hibernate, JUnit
                Databases & Tools: MySQL, Docker, Git, AWS

                WORK EXPERIENCE
                Software Engineer - Tech Systems (2023 - Present)
                - Architected high-throughput REST APIs using Spring Boot and MySQL, improving latency by 35%.
                - Deployed microservices on AWS using Docker containers.

                PROJECTS
                Distributed Payment Gateway
                - Developed resilient payment services with Spring Boot and PostgreSQL.
                """;

        Resume resume = new Resume();
        resume.setId(200L);
        resume.setTitle("Jane Doe Resume");
        resume.setTargetRole("Java Backend Developer");
        resume.setExtractedText(sampleText);
        resume.setStatus(ResumeStatus.PROCESSING);

        when(analysisRepo.save(any(ResumeAnalysis.class))).thenAnswer(invocation -> {
            ResumeAnalysis a = invocation.getArgument(0);
            a.setId(10L);
            return a;
        });

        ResumeAnalysis resultAnalysis = analysisService.analyzeResume(resume, "Java Backend Developer");

        assertThat(resultAnalysis).isNotNull();
        assertThat(resultAnalysis.getOverallScore()).isGreaterThanOrEqualTo(70);
        assertThat(resultAnalysis.getSectionCompletenessScore()).isEqualTo(100);
        assertThat(resultAnalysis.getReadabilityScore()).isGreaterThanOrEqualTo(50);
        assertThat(resultAnalysis.getRelevanceScore()).isGreaterThan(70);

        // Verify status updated to ANALYZED on Resume
        assertThat(resume.getStatus()).isEqualTo(ResumeStatus.ANALYZED);
        verify(resumeRepo, times(1)).save(resume);

        // Verify skills were extracted and saved
        verify(skillRepo, atLeast(5)).save(any(ResumeSkill.class));

        // Verify analysis record was persisted
        verify(analysisRepo, times(1)).save(any(ResumeAnalysis.class));
    }
}
