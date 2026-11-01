# Resume Analyzer & Mock Interview

> *Analyze your resume. Practice with confidence. Prepare for your next opportunity.*

A complete, full-stack, database-backed web application designed to help students, job seekers, and software engineers prepare for their career transitions. The platform provides automated document text parsing (PDF/DOCX), scoring algorithms for readability, section completeness, and role relevance, suggestion generation using Google's X-Y-Z formula, multi-version snapshot history, and an interactive mock interview simulator evaluated against a 4-tier rubric.

---

## 🌟 Key Features

1. **Document Ingestion & Multi-Format Text Extraction**
   - Direct upload support for PDF (`.pdf`) and Word (`.docx`) documents via Apache PDFBox and Apache POI.
   - **Security & Integrity Checks**: Enforced 10MB file size limit and magic byte signature verification (`%PDF` for PDFs and `PK\x03\x04` for DOCX OpenXML packages).
   - **Scanned Document Detection**: Clear, user-friendly messaging when a document has insufficient readable text (< 50 selectable characters) without making unsupported OCR claims.
   - Text sanitization, non-printable character stripping, and whitespace normalization.

2. **Resume Quality & Relevance Engine**
   - **Section Completeness:** Automated identification of Contact Information, Professional Summary, Work Experience, Education, and Projects.
   - **Quantifiable Impact Detection:** Scans for metrics, percentages, latencies, throughput improvements, and team sizes.
   - **Action Verb & Phrasing Analysis:** Rewards strong action verbs (*engineered, optimized, automated, deployed*) and flags weak/passive verbs (*worked on, assisted with*).
   - **Target Role Keyword Alignment:** Multi-keyword match against backend, frontend, full-stack, DevOps, and data roles.
   - **Rule-Based Suggestions:** Tailored recommendations structured with Google's X-Y-Z formula (*"Accomplished [X], as measured by [Y], by doing [Z]"*).

3. **In-Browser Resume Editor & Suggestion Review**
   - Live character and word counters.
   - Actionable sidebar displaying all detected weaknesses and missing sections.
   - One-click **"Apply to Text"** button to insert suggested bullet points directly into the editor.
   - Multi-version snapshot history tracking every change with custom notes.

4. **Grounded Mock Interview Engine & Reliable Answer Persistence**
   - Configurable by interview type (*Technical, Behavioral, System Design, Mixed*) and difficulty (*Entry, Intermediate, Senior, Lead*).
   - Dynamic question generation strictly grounded in the candidate's actual extracted resume skills and projects.
   - **Save-Before-Advance & Auto-Save**: Candidate answers are persisted to MySQL with immediate synchronization before navigating questions.
   - **Idempotent Updates**: Existing answers update in place without duplicate row creation.
   - **Page Reload Resilience**: Refreshing the browser preserves all submitted answers and progress.
   - Session Finite State Machine (`CREATED` &rarr; `IN_PROGRESS` &rarr; `COMPLETED` / `ABANDONED`).

5. **4-Tier Rubric Evaluation & Performance Analytics**
   - Every answer is evaluated against candidate-submitted responses across four dimensions:
     - **Technical Correctness & Keyword Coverage** (30%)
     - **Conceptual Understanding & Context** (30%)
     - **Role & Question Relevance** (20%)
     - **Communication Clarity & Structure** (20%)
   - Generates strengths, areas for improvement, actionable feedback, and sample exemplary answers.
   - Track progress over time via the interactive user dashboard and interview history.

6. **Enterprise Security & Data Isolation**
   - Stateless JWT Bearer authentication (HMAC-SHA512).
   - BCrypt password hashing (12 rounds).
   - Strict row-level ownership checks ensuring users cannot view, edit, or delete another candidate's resumes or interview sessions.

---

## 🏗 Architecture & Tech Stack

| Layer | Technologies |
|---|---|
| **Backend** | Java 21 / 23, Spring Boot 3.3.4 (Web, Data JPA, Security, Actuator, Validation) |
| **Persistence** | MySQL 8.0, Hibernate ORM 6.5, HikariCP Connection Pooling |
| **Document Processing** | Apache PDFBox 2.0.31, Apache POI 5.2.5 |
| **Security & Auth** | Spring Security 6.3, JJWT 0.12.5 (JSON Web Tokens), BCrypt |
| **Frontend** | Semantic HTML5, CSS3 (Custom Responsive Navy Theme), Vanilla JavaScript (Modular ES6 API client) |
| **Testing** | JUnit 5, Mockito, AssertJ, Maven Surefire |

---

## 🗄 Database Schema (10 Relational Tables)

The application automatically maps and validates 10 JPA entities against MySQL:

1. `users` &mdash; User credentials, target role, full name, timestamps.
2. `resumes` &mdash; Uploaded resumes, storage paths, file metadata, extracted raw text.
3. `resume_versions` &mdash; Immutable version history with change notes and content snapshots.
4. `resume_skills` &mdash; Extracted technical skills mapped to categories (`TECHNICAL`, `FRAMEWORK`, `DATABASE`, `TOOL`, `SOFT_SKILL`).
5. `resume_analyses` &mdash; Quality scores (Overall, Completeness, Readability, Relevance), strengths, weaknesses, and summaries.
6. `resume_suggestions` &mdash; Actionable improvement items linked to resume analyses (`PENDING`, `ACCEPTED`, `REJECTED`).
7. `interview_sessions` &mdash; Interview state machine, overall score, strengths, and preparation topics.
8. `interview_questions` &mdash; Questions grounded in resume skills, category, difficulty, and context hints.
9. `interview_answers` &mdash; Candidate submitted answers linked to interview questions with unique constraint on `(session_id, question_id)`.
10. `answer_evaluations` &mdash; 4-tier rubric score breakdown, feedback, and sample answers.

---

## 🚀 Getting Started

### Prerequisites

- **Java JDK 21+** (JDK 23 supported)
- **MySQL Server 8.0+**
- **Apache Maven 3.9+** (or use the included `./mvnw` / `mvnw.cmd` wrapper)

### 1. Database Setup

Log into MySQL and initialize the database:

```sql
CREATE DATABASE IF NOT EXISTS resume_analyzer_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

Verify your credentials in `src/main/resources/application.properties` (or set environment variables):

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/resume_analyzer_db?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true
spring.datasource.username=root
spring.datasource.password=Yash@5877
server.port=8085
```

### 2. Build the Application

Build the application and run all unit/integration tests:

```powershell
# On Windows PowerShell
$env:JAVA_HOME = "C:\Program Files\Java\jdk-23"
.\mvnw.cmd clean test
```

Package the executable Spring Boot JAR:

```powershell
.\mvnw.cmd clean package -DskipTests
```

### 3. Run the Server

Start the application:

```powershell
# Run executable JAR
$env:JAVA_HOME = "C:\Program Files\Java\jdk-23"
& "C:\Program Files\Java\jdk-23\bin\java.exe" -jar target/resume-analyzer-mock-interview-1.0.0.jar

# Or run directly via Maven
.\mvnw.cmd spring-boot:run
```

Once started, open your web browser and navigate to:
**`http://localhost:8085`**

---

## 🧪 Automated Testing

The project includes **37 automated test cases across 7 test classes** covering authentication, document extraction, security headers, scoring algorithms, CRUD ownership rules, mock interview answer persistence, and rubric evaluation.

Execute tests via Maven:

```powershell
$env:JAVA_HOME = "C:\Program Files\Java\jdk-23"
.\mvnw.cmd test
```

### Test Suite Summary:
- **`AuthServiceTest`** (4 tests): Registration, duplicate prevention, password validation, and JWT authentication.
- **`DocumentExtractionAndValidationTest`** (14 tests): PDF & DOCX text extraction, header magic byte validation (`%PDF` & `PK`), 10MB file size limit, and unreadable/scanned PDF handling.
- **`InterviewAnswerPersistenceTest`** (8 tests): Answer saving, idempotent updates without duplicates, short keyword answers, page reload persistence, and rubric evaluation linking.
- **`InterviewEngineTest`** (3 tests): Grounded question generation, 4-tier rubric evaluation, and session FSM transitions.
- **`PdfGeneratorTest`** (1 test): Programmatic PDF document generator.
- **`ResumeAnalysisEngineTest`** (2 tests): Section parsing, metric detection, skill extraction, and Google X-Y-Z suggestions.
- **`ResumeCrudAndOwnershipTest`** (5 tests): CRUD isolation, cross-user authorization enforcement, and versioning.

---

## 📡 REST API Reference

All protected endpoints require the HTTP header:
`Authorization: Bearer <jwt_token>`

### Authentication & Profile
| Method | Endpoint | Description |
|---|---|---|
| `POST` | `/api/auth/register` | Register a new user account |
| `POST` | `/api/auth/login` | Login and receive JWT access token |
| `GET` | `/api/profile` | Get current authenticated user profile |
| `PUT` | `/api/profile` | Update profile (name, target role, headline, bio) |
| `POST` | `/api/profile/password` | Change user password |

### Resumes & Analysis
| Method | Endpoint | Description |
|---|---|---|
| `POST` | `/api/resumes` | Upload resume (multipart: `file`, `title`, `targetRole`) |
| `GET` | `/api/resumes` | List user resumes (supports `?search=keyword`) |
| `GET` | `/api/resumes/{id}` | Get complete resume detail, skills, and latest analysis |
| `PUT` | `/api/resumes/{id}` | Update title or target role |
| `DELETE` | `/api/resumes/{id}` | Delete resume and cascade delete all associated records |
| `POST` | `/api/resumes/{id}/versions` | Save revised resume text as a new numbered version |
| `POST` | `/api/resumes/{id}/analyze` | Trigger re-analysis of current resume text |
| `PATCH` | `/api/resumes/suggestions/{id}` | Update suggestion review status (`ACCEPTED` / `REJECTED`) |

### Mock Interviews
| Method | Endpoint | Description |
|---|---|---|
| `POST` | `/api/interviews` | Create session grounded in a resume (`resumeId`, `type`, `difficulty`, `questionCount`) |
| `GET` | `/api/interviews` | Get user interview history (supports `?role=` and `?status=`) |
| `GET` | `/api/interviews/{id}` | Get session details, questions, and submitted answers |
| `POST` | `/api/interviews/{id}/answers` | Save an answer for a specific question (idempotent upsert) |
| `POST` | `/api/interviews/{id}/submit` | Finalize session and trigger rubric scoring evaluation |
| `GET` | `/api/interviews/{id}/results` | Get full evaluation report, score breakdown, and sample answers |

### Dashboard
| Method | Endpoint | Description |
|---|---|---|
| `GET` | `/api/dashboard` | Get overall user stats, recent resumes, and recent interviews |
| `GET` | `/api/dashboard/stats` | Alias for dashboard statistics |

---

## 🖥 Frontend Web Application

The frontend is served directly through Spring Boot from `src/main/resources/static/`:

- **Landing Page (`/index.html`)**: Product introduction, features overview, and call-to-action.
- **Authentication (`/login.html`, `/register.html`)**: Clean forms with validation and JWT token management.
- **Dashboard (`/dashboard.html`)**: Aggregate metrics, recent resumes, interview stats, and quick-action links.
- **Resume Hub (`/resumes.html`)**: Card-based resume list, live server-side search, upload modal with drag-and-drop.
- **Quality Report (`/resume-detail.html`)**: Score gauges, section breakdown, identified skills, and suggestions.
- **Resume Editor (`/resume-editor.html`)**: Real-time editor, suggestion review sidebar with "Apply to Text", and version history.
- **Interview Setup (`/interview-setup.html`)**: Resume selector, interview type, difficulty level, and question count selector.
- **Active Interview (`/interview.html`)**: Question flow, progress tracker, context hints, answer editor, and save-before-advance protection.
- **Interview Results (`/interview-results.html`)**: Overall score badge, 4-tier rubric breakdown, strengths, areas for improvement, and sample answers.
- **Interview History (`/interview-history.html`)**: Past interview sessions table with filter controls and result links.
- **User Profile (`/profile.html`)**: Account details, target role preference, and password update.
- **Error Page (`/error.html`)**: User-friendly fallback error handling.

---

## 🔒 Security & Privacy

- **No Committed Secrets:** Credentials are parameterized and loaded via environment variables or local properties.
- **Password Protection:** Passwords are never stored in plaintext and are salted using Spring Security's `BCryptPasswordEncoder` (12 rounds).
- **Upload Safety:** Uploaded files are validated against allowed MIME types, assigned randomized UUID file names on disk, and isolated from public static assets.
