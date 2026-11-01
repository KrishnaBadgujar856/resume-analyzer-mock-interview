package com.resumeanalyzer.service.resume;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.resumeanalyzer.dto.resume.ResumeAnalysisDto;
import com.resumeanalyzer.dto.resume.ResumeSuggestionDto;
import com.resumeanalyzer.entity.*;
import com.resumeanalyzer.repository.ResumeAnalysisRepository;
import com.resumeanalyzer.repository.ResumeRepository;
import com.resumeanalyzer.repository.ResumeSkillRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class ResumeAnalysisService {

    private static final Logger logger = LoggerFactory.getLogger(ResumeAnalysisService.class);
    private final ResumeAnalysisRepository analysisRepository;
    private final ResumeSkillRepository skillRepository;
    private final ResumeRepository resumeRepository;
    private final ObjectMapper objectMapper;

    // Common skill dictionary categorized
    private static final Map<String, SkillCategory> SKILL_DICTIONARY = new LinkedHashMap<>();

    static {
        // Languages & Core Technical
        String[] techLanguages = {"Java", "Python", "JavaScript", "TypeScript", "C++", "C#", "Go", "Golang", "Rust", "Kotlin", "Swift", "PHP", "Ruby", "Scala", "R", "SQL", "HTML", "CSS", "HTML5", "CSS3"};
        for (String s : techLanguages) SKILL_DICTIONARY.put(s.toLowerCase(), SkillCategory.TECHNICAL);

        // Frameworks & Libraries
        String[] frameworks = {"Spring Boot", "Spring", "Hibernate", "React", "React.js", "Angular", "Vue", "Vue.js", "Node.js", "Express", "Express.js", "Django", "Flask", "FastAPI", "ASP.NET", ".NET Core", "Next.js", "Tailwind CSS", "Bootstrap", "Redux", "JUnit", "Mockito"};
        for (String s : frameworks) SKILL_DICTIONARY.put(s.toLowerCase(), SkillCategory.FRAMEWORK);

        // Databases
        String[] databases = {"MySQL", "PostgreSQL", "MongoDB", "Redis", "Oracle", "SQLite", "Cassandra", "Elasticsearch", "DynamoDB", "MariaDB", "Firebase"};
        for (String s : databases) SKILL_DICTIONARY.put(s.toLowerCase(), SkillCategory.DATABASE);

        // Tools & Cloud/DevOps
        String[] tools = {"Git", "GitHub", "GitLab", "Docker", "Kubernetes", "AWS", "Amazon Web Services", "Azure", "GCP", "Google Cloud", "Jenkins", "CI/CD", "Linux", "Maven", "Gradle", "Jira", "Postman", "Nginx", "Terraform", "Prometheus", "Grafana"};
        for (String s : tools) SKILL_DICTIONARY.put(s.toLowerCase(), SkillCategory.TOOL);

        // Soft Skills
        String[] softSkills = {"Agile", "Scrum", "Communication", "Leadership", "Teamwork", "Problem Solving", "Time Management", "Critical Thinking", "Collaboration", "Mentoring"};
        for (String s : softSkills) SKILL_DICTIONARY.put(s.toLowerCase(), SkillCategory.SOFT_SKILL);
    }

    // Role-specific keyword benchmarks
    private static final Map<String, List<String>> ROLE_KEYWORDS = new HashMap<>();

    static {
        ROLE_KEYWORDS.put("java backend developer", List.of("java", "spring boot", "spring", "hibernate", "mysql", "postgresql", "rest", "api", "microservices", "jpa", "maven", "docker", "junit"));
        ROLE_KEYWORDS.put("backend developer", List.of("api", "database", "sql", "rest", "backend", "microservices", "docker", "node.js", "python", "java", "authentication"));
        ROLE_KEYWORDS.put("frontend developer", List.of("javascript", "typescript", "react", "html", "css", "vue", "angular", "responsive", "redux", "tailwind", "ui", "ux", "webpack"));
        ROLE_KEYWORDS.put("full stack developer", List.of("javascript", "react", "node.js", "java", "spring boot", "sql", "database", "api", "rest", "git", "html", "css", "docker"));
        ROLE_KEYWORDS.put("devops engineer", List.of("docker", "kubernetes", "aws", "ci/cd", "jenkins", "linux", "terraform", "monitoring", "git", "cloud", "ansible", "bash"));
        ROLE_KEYWORDS.put("data analyst", List.of("sql", "python", "excel", "tableau", "power bi", "pandas", "visualization", "statistics", "data cleaning", "reporting"));
        ROLE_KEYWORDS.put("software engineer", List.of("data structures", "algorithms", "git", "debugging", "testing", "clean code", "system design", "sql", "api"));
    }

    public ResumeAnalysisService(ResumeAnalysisRepository analysisRepository,
                                 ResumeSkillRepository skillRepository,
                                 ResumeRepository resumeRepository,
                                 ObjectMapper objectMapper) {
        this.analysisRepository = analysisRepository;
        this.skillRepository = skillRepository;
        this.resumeRepository = resumeRepository;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public ResumeAnalysis analyzeResume(Resume resume, String targetRole) {
        String text = resume.getExtractedText() != null ? resume.getExtractedText() : "";
        String role = targetRole != null && !targetRole.isBlank() ? targetRole.trim() : (resume.getTargetRole() != null ? resume.getTargetRole() : "Software Engineer");

        // 1. Detect Sections
        Map<String, Boolean> sections = detectSections(text);
        List<String> missingSections = new ArrayList<>();
        if (!sections.get("Education")) missingSections.add("Education");
        if (!sections.get("Experience")) missingSections.add("Work Experience / Internships");
        if (!sections.get("Projects")) missingSections.add("Projects");
        if (!sections.get("Skills")) missingSections.add("Skills");
        if (!sections.get("Contact")) missingSections.add("Contact Information (Email / Phone / LinkedIn)");

        // 2. Extract Skills
        List<ExtractedSkill> extractedSkills = extractSkills(text);
        // Persist extracted skills to Resume
        skillRepository.deleteByResumeId(resume.getId());
        for (ExtractedSkill es : extractedSkills) {
            ResumeSkill skill = new ResumeSkill(resume, es.name, es.category);
            skillRepository.save(skill);
        }

        // 3. Compute Section Completeness Score (0-100)
        int detectedCount = 5 - missingSections.size();
        int sectionScore = Math.min(100, (detectedCount * 20));

        // 4. Compute Readability & Quality Score (0-100)
        ReadabilityMetrics readability = evaluateReadability(text);

        // 5. Compute Role Relevance Score (0-100)
        int relevanceScore = calculateRoleRelevance(text, extractedSkills, role);

        // 6. Overall Weighted Score: Completeness (35%), Readability (35%), Relevance (30%)
        int overallScore = (int) Math.round((sectionScore * 0.35) + (readability.score * 0.35) + (relevanceScore * 0.30));
        overallScore = Math.max(15, Math.min(100, overallScore));

        // 7. Formulate Strengths & Weaknesses
        List<String> strengths = new ArrayList<>();
        List<String> weaknesses = new ArrayList<>();

        if (extractedSkills.size() >= 8) {
            strengths.add("Strong technical breadth with " + extractedSkills.size() + " identified tools and technologies.");
        } else if (extractedSkills.size() >= 4) {
            strengths.add("Good core technical foundation covering key languages and tools.");
        } else {
            weaknesses.add("Low number of identifiable technical skills. Highlight core languages, libraries, and frameworks.");
        }

        if (readability.metricsCount >= 3) {
            strengths.add("Excellent use of quantified impact and metrics (e.g., percentages, latencies, user numbers) across achievements.");
        } else if (readability.metricsCount == 0) {
            weaknesses.add("Lacks quantifiable metrics. Add numbers, percentages, or scale (e.g., 'improved performance by 25%').");
        }

        if (readability.actionVerbCount >= 5) {
            strengths.add("Strong, proactive action verbs used consistently to convey accomplishments.");
        } else {
            weaknesses.add("Contains passive phrasing or weak verbs (e.g., 'worked on', 'responsible for'). Replace with strong action verbs.");
        }

        if (missingSections.isEmpty()) {
            strengths.add("Comprehensive resume structure covering all essential sections (Education, Projects, Skills, Contact).");
        } else {
            weaknesses.add("Missing essential sections: " + String.join(", ", missingSections) + ".");
        }

        if (relevanceScore >= 75) {
            strengths.add("High relevance and keyword alignment for the '" + role + "' target role.");
        } else if (relevanceScore < 50) {
            weaknesses.add("Resume contains few keywords specifically associated with '" + role + "'.");
        }

        // 8. Generate Actionable Suggestions
        List<ResumeSuggestion> suggestions = generateSuggestions(text, readability, missingSections, extractedSkills, role);

        // 9. Summary
        String summary = String.format(
                "Resume evaluated for target role '%s'. Overall Quality Score: %d/100 (Completeness: %d/100, Readability: %d/100, Relevance: %d/100). Identified %d core technical skills with %d targeted improvement suggestions.",
                role, overallScore, sectionScore, readability.score, relevanceScore, extractedSkills.size(), suggestions.size()
        );

        // 10. Persist ResumeAnalysis
        ResumeAnalysis analysis = new ResumeAnalysis();
        analysis.setResume(resume);
        analysis.setOverallScore(overallScore);
        analysis.setSectionCompletenessScore(sectionScore);
        analysis.setReadabilityScore(readability.score);
        analysis.setRelevanceScore(relevanceScore);
        analysis.setTargetRole(role);
        analysis.setSummary(summary);
        analysis.setStrengthsJson(toJson(strengths));
        analysis.setWeaknessesJson(toJson(weaknesses));
        analysis.setMissingSectionsJson(toJson(missingSections));

        for (ResumeSuggestion s : suggestions) {
            s.setAnalysis(analysis);
        }
        analysis.setSuggestions(suggestions);

        ResumeAnalysis savedAnalysis = analysisRepository.save(analysis);

        // Update resume status and targetRole
        resume.setStatus(ResumeStatus.ANALYZED);
        resume.setTargetRole(role);
        resumeRepository.save(resume);

        return savedAnalysis;
    }

    public ResumeAnalysisDto toDto(ResumeAnalysis analysis) {
        if (analysis == null) return null;
        ResumeAnalysisDto dto = new ResumeAnalysisDto();
        dto.setId(analysis.getId());
        dto.setOverallScore(analysis.getOverallScore());
        dto.setSectionCompletenessScore(analysis.getSectionCompletenessScore());
        dto.setReadabilityScore(analysis.getReadabilityScore());
        dto.setRelevanceScore(analysis.getRelevanceScore());
        dto.setTargetRole(analysis.getTargetRole());
        dto.setSummary(analysis.getSummary());
        dto.setCreatedAt(analysis.getCreatedAt());

        dto.setStrengths(fromJsonList(analysis.getStrengthsJson()));
        dto.setWeaknesses(fromJsonList(analysis.getWeaknessesJson()));
        dto.setMissingSections(fromJsonList(analysis.getMissingSectionsJson()));

        if (analysis.getSuggestions() != null) {
            List<ResumeSuggestionDto> sDtos = analysis.getSuggestions().stream().map(s -> new ResumeSuggestionDto(
                    s.getId(),
                    s.getCategory(),
                    s.getIssueDescription(),
                    s.getSuggestedAction(),
                    s.getOriginalSnippet(),
                    s.getSuggestedSnippet(),
                    s.getStatus()
            )).collect(Collectors.toList());
            dto.setSuggestions(sDtos);
        }

        return dto;
    }

    private Map<String, Boolean> detectSections(String text) {
        String lower = text.toLowerCase();
        Map<String, Boolean> sections = new HashMap<>();

        sections.put("Education", containsPattern(lower, "(education|academics|academic background|degree|b\\.tech|b\\.e|bachelor|master)"));
        sections.put("Experience", containsPattern(lower, "(experience|employment|work history|internship|professional experience)"));
        sections.put("Projects", containsPattern(lower, "(projects|academic projects|key projects|personal projects)"));
        sections.put("Skills", containsPattern(lower, "(skills|technical skills|technologies|proficiencies|competencies)"));
        sections.put("Contact", containsPattern(lower, "([a-z0-9._%+-]+@[a-z0-9.-]+\\.[a-z]{2,}|linkedin\\.com|github\\.com|\\+?\\d{10,})"));

        return sections;
    }

    private boolean containsPattern(String text, String regex) {
        Pattern pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
        return pattern.matcher(text).find();
    }

    private List<ExtractedSkill> extractSkills(String text) {
        String lower = text.toLowerCase();
        Set<String> matchedSkillNames = new HashSet<>();
        List<ExtractedSkill> list = new ArrayList<>();

        for (Map.Entry<String, SkillCategory> entry : SKILL_DICTIONARY.entrySet()) {
            String skillKey = entry.getKey();
            Pattern p = Pattern.compile("\\b" + Pattern.quote(skillKey) + "\\b", Pattern.CASE_INSENSITIVE);
            if (p.matcher(lower).find()) {
                if (matchedSkillNames.add(skillKey)) {
                    // Proper casing
                    String formattedName = capitalizeSkill(skillKey);
                    list.add(new ExtractedSkill(formattedName, entry.getValue()));
                }
            }
        }

        return list;
    }

    private String capitalizeSkill(String skill) {
        for (String original : List.of("JavaScript", "TypeScript", "C++", "C#", "SQL", "HTML5", "CSS3", "Spring Boot", "React.js", "Node.js", "Vue.js", "ASP.NET", ".NET Core", "AWS", "GCP", "CI/CD", "PostgreSQL", "MongoDB", "JUnit")) {
            if (original.equalsIgnoreCase(skill)) return original;
        }
        return Character.toUpperCase(skill.charAt(0)) + skill.substring(1);
    }

    private ReadabilityMetrics evaluateReadability(String text) {
        String lower = text.toLowerCase();

        // 1. Action verbs detection
        String[] strongActionVerbs = {"engineered", "architected", "developed", "implemented", "optimized", "built", "designed", "accelerated", "reduced", "automated", "orchestrated", "spearheaded", "integrated", "refactored", "resolved"};
        int actionVerbCount = 0;
        for (String verb : strongActionVerbs) {
            if (Pattern.compile("\\b" + verb + "\\b", Pattern.CASE_INSENSITIVE).matcher(lower).find()) {
                actionVerbCount++;
            }
        }

        // 2. Metrics & Quantification detection (% or numbers + metrics)
        Pattern metricPattern = Pattern.compile("(\\b\\d+[%]|\\b\\d+\\s*(x|ms|seconds|users|million|k|fps|gb|mb)\\b|increased by|reduced by|improved by)", Pattern.CASE_INSENSITIVE);
        Matcher metricMatcher = metricPattern.matcher(text);
        int metricCount = 0;
        while (metricMatcher.find()) {
            metricCount++;
        }

        // 3. Weak phrasing detection
        String[] weakPhrases = {"responsible for", "worked on", "assisted in", "helped with", "handled", "duties included"};
        int weakPhrasesCount = 0;
        for (String weak : weakPhrases) {
            if (lower.contains(weak)) weakPhrasesCount++;
        }

        // 4. Length check (Word count)
        String[] words = text.split("\\s+");
        int wordCount = words.length;

        // Score formulation (base 50)
        int score = 55;
        score += Math.min(25, actionVerbCount * 4);
        score += Math.min(20, metricCount * 6);
        score -= Math.min(15, weakPhrasesCount * 5);

        // Optimal resume length between 250 and 800 words
        if (wordCount < 200) score -= 15;
        if (wordCount > 1000) score -= 5;

        score = Math.max(20, Math.min(100, score));

        return new ReadabilityMetrics(score, actionVerbCount, metricCount, weakPhrasesCount, wordCount);
    }

    private int calculateRoleRelevance(String text, List<ExtractedSkill> skills, String role) {
        String lowerRole = role.toLowerCase();
        List<String> targetKeywords = null;

        for (Map.Entry<String, List<String>> entry : ROLE_KEYWORDS.entrySet()) {
            if (lowerRole.contains(entry.getKey()) || entry.getKey().contains(lowerRole)) {
                targetKeywords = entry.getValue();
                break;
            }
        }

        if (targetKeywords == null) {
            targetKeywords = ROLE_KEYWORDS.get("software engineer");
        }

        int matches = 0;
        String lowerText = text.toLowerCase();
        for (String kw : targetKeywords) {
            if (Pattern.compile("\\b" + Pattern.quote(kw) + "\\b", Pattern.CASE_INSENSITIVE).matcher(lowerText).find()) {
                matches++;
            }
        }

        double ratio = (double) matches / targetKeywords.size();
        int relevance = (int) Math.round(ratio * 100);
        return Math.max(25, Math.min(100, relevance));
    }

    private List<ResumeSuggestion> generateSuggestions(String text, ReadabilityMetrics readability,
                                                      List<String> missingSections, List<ExtractedSkill> skills,
                                                      String role) {
        List<ResumeSuggestion> list = new ArrayList<>();
        String lower = text.toLowerCase();

        // 1. Missing sections suggestions
        for (String missing : missingSections) {
            ResumeSuggestion s = new ResumeSuggestion();
            s.setCategory("Section Completeness");
            s.setIssueDescription("The resume appears to be missing a dedicated '" + missing + "' section.");
            s.setSuggestedAction("Add an explicit heading for '" + missing + "' with structured bullet points.");
            s.setOriginalSnippet("");
            s.setSuggestedSnippet("### " + missing + "\n- Include relevant details, dates, and concise bullet points.");
            list.add(s);
        }

        // 2. Weak phrasing replacement
        if (lower.contains("responsible for")) {
            ResumeSuggestion s = new ResumeSuggestion();
            s.setCategory("Action Verbs & Impact");
            s.setIssueDescription("Found passive phrase 'responsible for', which weakens ownership of accomplishments.");
            s.setSuggestedAction("Replace 'responsible for' with proactive verbs such as 'Engineered', 'Orchestrated', or 'Developed'.");
            s.setOriginalSnippet("Responsible for building API endpoints...");
            s.setSuggestedSnippet("Engineered scalable RESTful API endpoints reducing response latency by 20%...");
            list.add(s);
        }

        if (lower.contains("worked on")) {
            ResumeSuggestion s = new ResumeSuggestion();
            s.setCategory("Action Verbs & Impact");
            s.setIssueDescription("Found generic phrase 'worked on' instead of a specific technical contribution.");
            s.setSuggestedAction("Specify exact architectural or engineering contribution: 'Architected', 'Designed', 'Optimized'.");
            s.setOriginalSnippet("Worked on database queries and UI components...");
            s.setSuggestedSnippet("Optimized complex relational SQL queries and developed reusable responsive UI components...");
            list.add(s);
        }

        // 3. Metric Quantification
        if (readability.metricsCount < 2) {
            ResumeSuggestion s = new ResumeSuggestion();
            s.setCategory("Quantifiable Impact");
            s.setIssueDescription("Bullet points lack measurable metrics (percentages, performance gains, users impacted).");
            s.setSuggestedAction("Follow Google's X-Y-Z formula: 'Accomplished [X], as measured by [Y], by doing [Z]'.");
            s.setOriginalSnippet("Improved system performance and user experience.");
            s.setSuggestedSnippet("Improved API throughput by 35% and reduced query latency from 450ms to 95ms through database indexing.");
            list.add(s);
        }

        // 4. GitHub / Portfolio link check
        if (!lower.contains("github.com") && !lower.contains("gitlab.com")) {
            ResumeSuggestion s = new ResumeSuggestion();
            s.setCategory("Online Presence");
            s.setIssueDescription("No GitHub profile link detected in contact information.");
            s.setSuggestedAction("Add your GitHub profile URL at the top so interviewers can review your real source code and contributions.");
            s.setOriginalSnippet("Contact details");
            s.setSuggestedSnippet("GitHub: github.com/your-username | LinkedIn: linkedin.com/in/your-profile");
            list.add(s);
        }

        // 5. Role Keyword Optimization
        if (role.toLowerCase().contains("backend") || role.toLowerCase().contains("java")) {
            boolean hasSpring = skills.stream().anyMatch(sk -> sk.name.equalsIgnoreCase("Spring Boot") || sk.name.equalsIgnoreCase("Spring"));
            if (!hasSpring) {
                ResumeSuggestion s = new ResumeSuggestion();
                s.setCategory("Role Alignment");
                s.setIssueDescription("Targeting " + role + " but standard enterprise frameworks (e.g., Spring Boot, JPA) are not highlighted.");
                s.setSuggestedAction("Ensure your backend framework proficiencies and project usage are prominently listed.");
                s.setOriginalSnippet("Backend tools: Java, SQL");
                s.setSuggestedSnippet("Backend Technologies: Java 21, Spring Boot, Spring Data JPA, Hibernate, MySQL, REST APIs");
                list.add(s);
            }
        }

        return list;
    }

    private String toJson(List<String> list) {
        try {
            return objectMapper.writeValueAsString(list);
        } catch (Exception e) {
            return "[]";
        }
    }

    private List<String> fromJsonList(String json) {
        if (json == null || json.isBlank()) return Collections.emptyList();
        try {
            return objectMapper.readValue(json, new TypeReference<List<String>>() {});
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }

    private static class ReadabilityMetrics {
        final int score;
        final int actionVerbCount;
        final int metricsCount;
        final int weakPhrasesCount;
        final int wordCount;

        ReadabilityMetrics(int score, int actionVerbCount, int metricsCount, int weakPhrasesCount, int wordCount) {
            this.score = score;
            this.actionVerbCount = actionVerbCount;
            this.metricsCount = metricsCount;
            this.weakPhrasesCount = weakPhrasesCount;
            this.wordCount = wordCount;
        }
    }

    private static class ExtractedSkill {
        final String name;
        final SkillCategory category;

        ExtractedSkill(String name, SkillCategory category) {
            this.name = name;
            this.category = category;
        }
    }
}
