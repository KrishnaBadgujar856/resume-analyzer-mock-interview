package com.resumeanalyzer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.resumeanalyzer.entity.*;
import com.resumeanalyzer.repository.ResumeAnalysisRepository;
import com.resumeanalyzer.repository.ResumeRepository;
import com.resumeanalyzer.repository.ResumeSkillRepository;
import com.resumeanalyzer.service.resume.ResumeAnalysisService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ResumeAnalysisEngineTest {

    @Mock
    private ResumeAnalysisRepository analysisRepository;

    @Mock
    private ResumeSkillRepository skillRepository;

    @Mock
    private ResumeRepository resumeRepository;

    private ResumeAnalysisService analysisService;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        analysisService = new ResumeAnalysisService(analysisRepository, skillRepository, resumeRepository, objectMapper);
    }

    @Test
    @DisplayName("Should extract skills, calculate scores, and generate actionable suggestions")
    void testAnalyzeResumeComprehensive() {
        Resume resume = new Resume();
        resume.setId(101L);
        resume.setTitle("Java Backend Resume");
        resume.setTargetRole("Java Backend Developer");
        resume.setExtractedText("""
            Alex Morgan
            Email: alex.morgan@example.com | Phone: +1-555-0199 | github.com/alexmorgan | linkedin.com/in/alexmorgan
            
            EDUCATION
            Bachelor of Technology in Computer Science, State University, 2024
            
            TECHNICAL SKILLS
            Languages: Java, SQL, Python, JavaScript
            Frameworks: Spring Boot, Hibernate, JUnit
            Databases & Tools: MySQL, Docker, Git, AWS
            
            WORK EXPERIENCE
            Software Engineer Intern - Acme Corp (2023 - 2024)
            - Engineered scalable RESTful APIs using Spring Boot and MySQL, improving request throughput by 30%.
            - Optimized relational database queries, reducing query latency from 350ms to 80ms.
            - Automated deployment pipelines using Docker and Git.
            
            PROJECTS
            E-Commerce Microservices Platform
            - Developed backend services with Spring Boot and MySQL.
            - Implemented JWT token authentication and role-based access control.
            """);

        when(analysisRepository.save(any(ResumeAnalysis.class))).thenAnswer(invocation -> {
            ResumeAnalysis a = invocation.getArgument(0);
            a.setId(1L);
            return a;
        });

        ResumeAnalysis analysis = analysisService.analyzeResume(resume, "Java Backend Developer");

        assertThat(analysis).isNotNull();
        // Section completeness should be 100% since all 5 sections are present
        assertThat(analysis.getSectionCompletenessScore()).isEqualTo(100);
        // Overall score should be high due to quantified metrics (30%, 350ms, 80ms) and strong verbs
        assertThat(analysis.getOverallScore()).isGreaterThanOrEqualTo(75);
        assertThat(analysis.getReadabilityScore()).isGreaterThan(60);
        assertThat(analysis.getRelevanceScore()).isGreaterThan(70);

        // Verify skill persistence calls
        verify(skillRepository, atLeastOnce()).save(any(ResumeSkill.class));
        verify(analysisRepository, times(1)).save(any(ResumeAnalysis.class));
    }

    @Test
    @DisplayName("Should detect missing sections and generate suggestions for weak resumes")
    void testAnalyzeResumeWithMissingSections() {
        Resume resume = new Resume();
        resume.setId(102L);
        resume.setTitle("Sparse Resume");
        resume.setExtractedText("""
            John Doe
            Just some random text with no clear headings.
            Responsible for working on code.
            """);

        when(analysisRepository.save(any(ResumeAnalysis.class))).thenAnswer(inv -> inv.getArgument(0));

        ResumeAnalysis analysis = analysisService.analyzeResume(resume, "Backend Developer");

        assertThat(analysis).isNotNull();
        // Section completeness should be low because Education, Projects, Skills, Contact are missing
        assertThat(analysis.getSectionCompletenessScore()).isLessThanOrEqualTo(40);
        assertThat(analysis.getSuggestions()).isNotEmpty();

        // Should generate suggestion for weak phrasing 'responsible for'
        boolean hasActionVerbSuggestion = analysis.getSuggestions().stream()
                .anyMatch(s -> s.getCategory().contains("Action Verbs"));
        assertThat(hasActionVerbSuggestion).isTrue();
    }
}
