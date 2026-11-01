package com.resumeanalyzer;

import com.resumeanalyzer.entity.DifficultyLevel;
import com.resumeanalyzer.entity.InterviewType;
import com.resumeanalyzer.entity.Resume;
import com.resumeanalyzer.entity.ResumeSkill;
import com.resumeanalyzer.entity.SkillCategory;
import com.resumeanalyzer.service.ai.AIService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class InterviewEngineTest {

    private AIService aiService;

    @BeforeEach
    void setUp() {
        aiService = new AIService();
    }

    @Test
    @DisplayName("Should generate grounded interview questions matching resume skills")
    void testGroundedQuestionGeneration() {
        Resume resume = new Resume();
        resume.setTitle("Full Stack Resume");
        resume.setSkills(List.of(
                new ResumeSkill(resume, "Java", SkillCategory.TECHNICAL),
                new ResumeSkill(resume, "Spring Boot", SkillCategory.FRAMEWORK),
                new ResumeSkill(resume, "MySQL", SkillCategory.DATABASE)
        ));

        List<AIService.GeneratedQuestionData> questions = aiService.generateQuestions(
                resume,
                "Java Backend Developer",
                InterviewType.TECHNICAL,
                DifficultyLevel.INTERMEDIATE,
                "Spring Boot",
                5
        );

        assertThat(questions).hasSize(5);

        // First question should reference the resume's skills
        assertThat(questions.get(0).getQuestionText()).contains("Java");
        assertThat(questions.get(0).getCategory()).contains("Resume Project");

        // Verify balanced mix of categories
        assertThat(questions).anyMatch(q -> q.getCategory().contains("Core Fundamentals"));
        assertThat(questions).anyMatch(q -> q.getCategory().contains("Database"));
        assertThat(questions).anyMatch(q -> q.getCategory().contains("System Design"));
        assertThat(questions).anyMatch(q -> q.getCategory().contains("Behavioral"));
    }

    @Test
    @DisplayName("Should evaluate answers accurately according to rubric weights")
    void testEvaluateAnswerRubric() {
        String question = "How does Java handle concurrency and thread-safety?";
        String goodAnswer = """
            In Java, concurrency is managed using several mechanisms depending on the isolation level required.
            For critical sections, synchronized blocks and reentrant locks guarantee mutual exclusion by acquiring the monitor lock.
            For atomic operations without locking overhead, java.util.concurrent.atomic classes utilize CPU-level Compare-And-Swap (CAS).
            Furthermore, the volatile keyword guarantees memory visibility across threads by preventing CPU cache reordering, ensuring reads and writes hit main memory directly.
            In my backend project, we used ConcurrentHashMap to handle multi-threaded cache access without lock contention.
            """;

        AIService.EvaluatedAnswerData evaluation = aiService.evaluateAnswer(question, "Core Fundamentals", goodAnswer);

        assertThat(evaluation).isNotNull();
        // Technical terms (lock, thread, volatile, atomic, etc.) should drive high scores
        assertThat(evaluation.getCorrectness()).isGreaterThanOrEqualTo(75);
        assertThat(evaluation.getUnderstanding()).isGreaterThanOrEqualTo(75);
        assertThat(evaluation.getClarity()).isGreaterThanOrEqualTo(80);
        assertThat(evaluation.getTotalScore()).isGreaterThanOrEqualTo(75);
        assertThat(evaluation.getFeedback()).contains("Solid foundational response");
        assertThat(evaluation.getSampleAnswer()).isNotBlank();
    }

    @Test
    @DisplayName("Should penalize empty or ultra-short answers and provide guidance")
    void testEvaluateShortAnswer() {
        String question = "Explain database indexing in MySQL.";
        String shortAnswer = "It makes queries faster.";

        AIService.EvaluatedAnswerData evaluation = aiService.evaluateAnswer(question, "Database", shortAnswer);

        assertThat(evaluation).isNotNull();
        assertThat(evaluation.getTotalScore()).isLessThan(45);
        assertThat(evaluation.getFeedback()).contains("too brief");
        assertThat(evaluation.getMissingPoints()).isNotBlank();
    }
}
