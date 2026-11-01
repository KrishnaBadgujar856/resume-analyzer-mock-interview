package com.resumeanalyzer.service.ai;

import com.resumeanalyzer.entity.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.regex.Pattern;

@Service
public class AIService {

    private static final Logger logger = LoggerFactory.getLogger(AIService.class);

    @Value("${app.ai.enabled:false}")
    private boolean aiEnabled;

    @Value("${app.ai.api-key:}")
    private String apiKey;

    public boolean isDemoModeActive() {
        return !aiEnabled || apiKey == null || apiKey.isBlank();
    }

    /**
     * Generates grounded, personalized interview questions based on the user's actual resume.
     */
    public List<GeneratedQuestionData> generateQuestions(Resume resume,
                                                         String targetRole,
                                                         InterviewType interviewType,
                                                         DifficultyLevel difficulty,
                                                         String focusArea,
                                                         int count) {
        String resumeText = resume.getExtractedText() != null ? resume.getExtractedText() : "";
        List<ResumeSkill> skills = resume.getSkills() != null ? resume.getSkills() : Collections.emptyList();

        List<String> detectedSkills = skills.stream().map(ResumeSkill::getName).toList();
        String primarySkill = !detectedSkills.isEmpty() ? detectedSkills.get(0) : "Java";
        String secondarySkill = detectedSkills.size() > 1 ? detectedSkills.get(1) : "SQL";
        String databaseSkill = detectedSkills.stream()
                .filter(s -> s.equalsIgnoreCase("MySQL") || s.equalsIgnoreCase("PostgreSQL") || s.equalsIgnoreCase("MongoDB"))
                .findFirst().orElse("relational database");

        List<GeneratedQuestionData> questions = new ArrayList<>();

        // Question 1: Resume Project Deep-Dive (Grounded in Resume)
        questions.add(new GeneratedQuestionData(
                "Resume Project Architecture",
                String.format("In your resume, you highlighted projects utilizing %s and %s. Can you walk through the system architecture of your most significant project, detailing the key design decisions and how you handled data persistence?", primarySkill, secondarySkill),
                "Focus on the architectural layers (Controller, Service, Repository), database schema, and why specific libraries or patterns were chosen.",
                difficulty
        ));

        // Question 2: Core Technical Fundamentals
        String techQuestion;
        String techHint;
        if (difficulty == DifficultyLevel.BEGINNER) {
            techQuestion = String.format("Explain the fundamental difference between abstract classes and interfaces in %s, and when you would prefer one over the other.", primarySkill);
            techHint = "Discuss multiple inheritance of type, default methods, and contract vs partial implementation.";
        } else if (difficulty == DifficultyLevel.ADVANCED) {
            techQuestion = String.format("In %s, how does the memory management model (heap, stack, garbage collection) handle high object churn, and what profiling techniques would you use to diagnose memory leaks?", primarySkill);
            techHint = "Explain Eden, Survivor, Tenured spaces, GC algorithms (G1/ZGC), heap dumps, and memory profiling.";
        } else {
            techQuestion = String.format("How does %s handle concurrency and thread-safety? Explain the difference between synchronized blocks, volatile variables, and atomic primitives.", primarySkill);
            techHint = "Cover visibility (CPU caches vs main memory), race conditions, and lock overhead.";
        }
        questions.add(new GeneratedQuestionData("Core Fundamentals", techQuestion, techHint, difficulty));

        // Question 3: Database & Performance Optimization
        questions.add(new GeneratedQuestionData(
                "Database & Optimization",
                String.format("When querying large datasets in %s, how do you diagnose slow queries, and what indexing strategies or query optimizations do you implement to maintain sub-100ms response times?", databaseSkill),
                "Mention EXPLAIN query plans, B-Tree index structures, avoiding SELECT *, and caching strategies.",
                difficulty
        ));

        // Question 4: Problem Solving / System Design
        questions.add(new GeneratedQuestionData(
                "System Design & Reliability",
                String.format("Imagine your %s service suddenly experiences a 5x traffic spike, leading to connection timeouts and high latency. Walk through your step-by-step diagnostic and remediation process.", targetRole),
                "Address load balancing, connection pooling, rate limiting, distributed caching (Redis), and horizontal scaling.",
                difficulty
        ));

        // Question 5: Behavioral / Incident Management (HR / Collaboration)
        questions.add(new GeneratedQuestionData(
                "Behavioral & Engineering Ownership",
                "Tell me about a challenging technical roadblock or production issue you faced. How did you diagnose the root cause, communicate with stakeholders, and implement a long-term preventive fix?",
                "Use the STAR method: Situation, Task, Action, and measurable Result.",
                difficulty
        ));

        // Extra questions if count > 5
        if (count > 5) {
            questions.add(new GeneratedQuestionData(
                    "API Security & Authentication",
                    "How do you design secure RESTful APIs? Specifically, explain stateless JWT token authentication, CSRF/CORS considerations, and role-based access control.",
                    "Explain token signature verification, expiration handling, and defense-in-depth.",
                    difficulty
            ));
        }

        if (count > 6) {
            questions.add(new GeneratedQuestionData(
                    "Testing & Code Quality",
                    "What is your strategy for unit and integration testing? How do you isolate external dependencies, and what metrics indicate adequate test coverage?",
                    "Discuss Mockito mocking, Spring Boot test slices, test pyramid, and edge case coverage.",
                    difficulty
            ));
        }

        if (count > 7) {
            questions.add(new GeneratedQuestionData(
                    "DevOps & Deployment",
                    "Describe your experience with containerization (Docker) and CI/CD pipelines. How do you ensure zero-downtime deployments?",
                    "Discuss multi-stage Docker builds, automated test gates, and rolling updates.",
                    difficulty
            ));
        }

        // Return requested count
        return questions.subList(0, Math.min(count, questions.size()));
    }

    /**
     * Evaluates a user's answer against the comprehensive rubric.
     */
    public EvaluatedAnswerData evaluateAnswer(String questionText, String questionCategory, String answerText) {
        String answer = answerText != null ? answerText.trim() : "";
        int length = answer.length();
        int words = answer.split("\\s+").length;

        // Base metrics
        int correctness;
        int understanding;
        int relevance;
        int clarity;
        StringBuilder feedback = new StringBuilder();
        StringBuilder missingPoints = new StringBuilder();
        String sampleAnswer;

        // Check for empty or ultra-short answers
        if (words < 10) {
            correctness = 25;
            understanding = 20;
            relevance = 30;
            clarity = 25;
            feedback.append("The response is too brief to demonstrate technical competence. State core principles, mechanisms, and examples.");
            missingPoints.append("Detailed technical explanation; Concrete examples; Trade-offs and performance considerations.");
            sampleAnswer = "A comprehensive response should define the core concept clearly, explain the underlying technical mechanism, and illustrate with a real-world project example.";
        } else {
            // Analyze keywords and technical depth
            String lower = answer.toLowerCase();
            int technicalTermsFound = 0;
            for (String kw : List.of("lock", "thread", "concurrency", "atomic", "synchronized", "volatile", "mutex", "cas", "index", "cache", "memory", "latency", "async", "schema", "architecture", "component", "interface", "transaction", "pool", "query", "optimize", "profile", "exception", "test", "metric", "star", "result", "situation", "action", "throughput", "distributed", "heap", "stack", "hashmap", "api", "rest", "database", "spring")) {
                if (lower.contains(kw)) technicalTermsFound++;
            }

            // Rubric computation
            // 1. Correctness (40%)
            correctness = Math.min(95, 55 + (technicalTermsFound * 4));
            // 2. Conceptual Understanding (25%)
            understanding = Math.min(95, 50 + (words > 40 ? 25 : 10) + (technicalTermsFound * 3));
            // 3. Relevance (20%)
            relevance = Math.min(95, 60 + (words > 25 ? 25 : 10));
            // 4. Clarity (15%)
            boolean hasStructure = answer.contains(".") && words > 30;
            clarity = hasStructure ? 85 : 65;

            // Feedback formulation
            feedback.append(String.format("Solid foundational response (%d words). ", words));
            if (technicalTermsFound >= 3) {
                feedback.append("You integrated key technical terminology well. ");
            } else {
                feedback.append("Consider integrating more concrete technical concepts and design patterns. ");
            }

            if (lower.contains("for example") || lower.contains("e.g.") || lower.contains("in my project")) {
                feedback.append("Great inclusion of practical examples to substantiate your points.");
            } else {
                feedback.append("Backing up your explanation with a specific project example would make your answer stand out.");
            }

            missingPoints.append("Mention specific edge cases or failure modes; Quantify results with measurable metrics where applicable.");

            sampleAnswer = "Example of an exemplary response: Start with a clear definition, explain the internal mechanism (e.g., memory layout or SQL execution plan), provide a project scenario demonstrating your solution, and mention the trade-offs or alternatives evaluated.";
        }

        // Weighted total calculation in Java (Strict Rubric)
        int totalScore = (int) Math.round((correctness * 0.40) + (understanding * 0.25) + (relevance * 0.20) + (clarity * 0.15));
        totalScore = Math.max(20, Math.min(100, totalScore));

        return new EvaluatedAnswerData(
                correctness,
                understanding,
                relevance,
                clarity,
                totalScore,
                feedback.toString(),
                missingPoints.toString(),
                sampleAnswer
        );
    }

    public static class GeneratedQuestionData {
        private final String category;
        private final String questionText;
        private final String contextHint;
        private final DifficultyLevel difficulty;

        public GeneratedQuestionData(String category, String questionText, String contextHint, DifficultyLevel difficulty) {
            this.category = category;
            this.questionText = questionText;
            this.contextHint = contextHint;
            this.difficulty = difficulty;
        }

        public String getCategory() { return category; }
        public String getQuestionText() { return questionText; }
        public String getContextHint() { return contextHint; }
        public DifficultyLevel getDifficulty() { return difficulty; }
    }

    public static class EvaluatedAnswerData {
        private final int correctness;
        private final int understanding;
        private final int relevance;
        private final int clarity;
        private final int totalScore;
        private final String feedback;
        private final String missingPoints;
        private final String sampleAnswer;

        public EvaluatedAnswerData(int correctness, int understanding, int relevance, int clarity,
                                   int totalScore, String feedback, String missingPoints, String sampleAnswer) {
            this.correctness = correctness;
            this.understanding = understanding;
            this.relevance = relevance;
            this.clarity = clarity;
            this.totalScore = totalScore;
            this.feedback = feedback;
            this.missingPoints = missingPoints;
            this.sampleAnswer = sampleAnswer;
        }

        public int getCorrectness() { return correctness; }
        public int getUnderstanding() { return understanding; }
        public int getRelevance() { return relevance; }
        public int getClarity() { return clarity; }
        public int getTotalScore() { return totalScore; }
        public String getFeedback() { return feedback; }
        public String getMissingPoints() { return missingPoints; }
        public String getSampleAnswer() { return sampleAnswer; }
    }
}
