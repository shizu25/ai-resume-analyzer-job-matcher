# API Contract Specification

## Base URL
- Frontend -> Spring Boot Backend: `http://localhost:8080/api`
- Spring Boot Backend -> FastAPI Internal Service: `http://localhost:8001/internal`

---

## 1. Authentication Endpoints (`/api/auth`)

### `POST /api/auth/register`
- **Request Body**:
```json
{
  "email": "user@example.com",
  "password": "StrongPassword123!",
  "fullName": "Jane Doe"
}
```
- **Response `201 Created`**:
```json
{
  "success": true,
  "message": "User registered successfully",
  "data": {
    "userId": "uuid",
    "email": "user@example.com",
    "token": "jwt-token",
    "refreshToken": "refresh-token"
  }
}
```

### `POST /api/auth/login`
- **Request Body**:
```json
{
  "email": "user@example.com",
  "password": "StrongPassword123!"
}
```
- **Response `200 OK`**:
```json
{
  "success": true,
  "message": "Login successful",
  "data": {
    "userId": "uuid",
    "email": "user@example.com",
    "fullName": "Jane Doe",
    "token": "jwt-token",
    "refreshToken": "refresh-token"
  }
}
```

### `POST /api/auth/refresh`
- **Request Body**:
```json
{
  "refreshToken": "refresh-token"
}
```
- **Response `200 OK`**:
```json
{
  "success": true,
  "data": {
    "token": "new-jwt-token"
  }
}
```

### `POST /api/auth/logout`
- **Response `200 OK`**:
```json
{
  "success": true,
  "message": "Logged out successfully"
}
```

---

## 2. User Profile Endpoints (`/api/users`)
*Requires `Authorization: Bearer <token>`*

### `GET /api/users/me`
- **Response `200 OK`**:
```json
{
  "success": true,
  "data": {
    "id": "uuid",
    "email": "user@example.com",
    "fullName": "Jane Doe",
    "phone": "+1-555-0199",
    "location": "San Francisco, CA",
    "headline": "Senior Full-Stack Engineer",
    "linkedinUrl": "https://linkedin.com/in/janedoe",
    "githubUrl": "https://github.com/janedoe",
    "portfolioUrl": "https://janedoe.dev",
    "careerInterests": "Cloud Architecture, Distributed Systems, AI Engineering"
  }
}
```

### `PUT /api/users/me`
- **Request Body**:
```json
{
  "fullName": "Jane Doe",
  "phone": "+1-555-0199",
  "location": "San Francisco, CA",
  "headline": "Lead Systems Architect",
  "linkedinUrl": "https://linkedin.com/in/janedoe",
  "githubUrl": "https://github.com/janedoe",
  "portfolioUrl": "https://janedoe.dev",
  "careerInterests": "Distributed Systems, AI Platforms"
}
```
- **Response `200 OK`**: Returns updated profile data object.

---

## 3. Resume Management Endpoints (`/api/resumes`)
*Requires `Authorization: Bearer <token>`*

### `POST /api/resumes/upload`
- **Content-Type**: `multipart/form-data`
- **Body**: `file` (PDF or DOCX, max 10MB)
- **Response `201 Created`**:
```json
{
  "success": true,
  "message": "Resume uploaded successfully",
  "data": {
    "id": "uuid",
    "fileName": "Jane_Doe_Resume.pdf",
    "fileType": "application/pdf",
    "fileSize": 142050,
    "isActive": false,
    "parsingStatus": "UPLOADED",
    "createdAt": "2026-11-25T11:30:00Z"
  }
}
```

### `GET /api/resumes`
- **Response `200 OK`**:
```json
{
  "success": true,
  "data": [
    {
      "id": "uuid",
      "fileName": "Jane_Doe_Resume.pdf",
      "fileType": "application/pdf",
      "fileSize": 142050,
      "isActive": true,
      "parsingStatus": "COMPLETED",
      "createdAt": "2026-11-25T11:30:00Z"
    }
  ]
}
```

### `GET /api/resumes/{id}`
- **Response `200 OK`**: Returns resume metadata, extracted personal details, skills, education, experience, projects, suggestions, and warnings.

### `GET /api/resumes/{id}/download`
- **Response `200 OK`**: Binary stream with `Content-Disposition: attachment; filename="Jane_Doe_Resume.pdf"`

### `PATCH /api/resumes/{id}/active`
- **Response `200 OK`**: Sets this resume as user's active/default resume.

### `DELETE /api/resumes/{id}`
- **Response `200 OK`**: Deletes resume record and deletes file from disk.

### `POST /api/resumes/{id}/analyze`
- **Response `200 OK`**: Runs AI parsing pipeline via FastAPI, stores extracted data, returns structured analysis.

---

## 4. Internal AI Service Endpoints (`/internal`)
*Called only by Spring Boot*

### `POST /internal/resume/parse`
- **Content-Type**: `multipart/form-data`
- **Body**: `file`
- **Response `200 OK`**:
```json
{
  "candidate_name": "Jane Doe",
  "email": "jane@example.com",
  "phone": "+1-555-0199",
  "summary": "Full stack engineer...",
  "skills": [
    { "name": "Java", "category": "Programming" },
    { "name": "Spring Boot", "category": "Backend" }
  ],
  "education": [
    {
      "institution": "Stanford University",
      "degree": "B.S.",
      "field": "Computer Science",
      "start_year": 2018,
      "end_year": 2022,
      "grade": "3.9 GPA"
    }
  ],
  "experience": [
    {
      "company": "Tech Corp",
      "position": "Software Engineer",
      "location": "San Francisco, CA",
      "start_date": "2022",
      "end_date": "Present",
      "is_current": true,
      "description": "Architected microservices..."
    }
  ],
  "projects": [
    {
      "title": "Cloud Platform",
      "description": "Built resilient platform...",
      "technologies": "Go, Kubernetes, Docker",
      "link": "https://github.com/..."
    }
  ],
  "suggestions": [
    {
      "category": "Experience",
      "priority": "high",
      "issue": "Missing quantified impact metrics in bullet points",
      "recommendation": "Incorporate specific numbers, latency reductions, or revenue metrics."
    }
  ],
  "warnings": []
}
```

---

## 4. Career Intelligence Platform Endpoints

### 4.1. Personalized Skill-Gap Roadmaps

#### `POST /api/roadmaps/generate`
Generates or retrieves a cached skill-gap roadmap for a resume against a target job.
- **Request Body**:
```json
{
  "resumeId": "b1c8f42d-...",
  "jobId": "f7823c91-..."
}
```
- **Response `201 Created`**: Returns `RoadmapResponseDto` with `overallCoveragePct`, `demonstratedSkillsCount`, `mentionedSkillsCount`, `missingSkillsCount`, and prioritized `items`.

#### `POST /api/roadmaps/regenerate`
Forces regeneration of the skill roadmap with fresh AI analysis.

#### `GET /api/roadmaps`
Lists all roadmaps created for the authenticated candidate.

#### `GET /api/roadmaps/{id}`
Returns complete roadmap details including milestone steps, practical tasks, and durations.

#### `PATCH /api/roadmaps/{roadmapId}/items/{itemId}/progress`
Updates learning milestone progress status.
- **Request Body**:
```json
{
  "progressStatus": "COMPLETED" // NOT_STARTED, IN_PROGRESS, COMPLETED
}
```

---

### 4.2. Resume Versions & Improvement Simulator

#### `POST /api/resumes/{resumeId}/versions`
Clones a resume into an editable version without mutating the original.
- **Request Body**:
```json
{
  "versionName": "Tailored for Acme Corp",
  "targetJobId": "optional-job-uuid",
  "contentJson": "{}",
  "rawText": ""
}
```

#### `GET /api/resumes/{resumeId}/versions`
Lists all versions for a given parent resume.

#### `GET /api/resume-versions`
Lists all resume versions for the authenticated candidate.

#### `GET /api/resume-versions/{versionId}`
Retrieves single version details with editable JSON content.

#### `PUT /api/resume-versions/{versionId}`
Updates version content or name after candidate edits.

#### `POST /api/resume-versions/{versionId}/suggestions`
Generates zero-hallucination suggestions for this version against a target job.
- **Request Body**:
```json
{
  "jobId": "f7823c91-..."
}
```

#### `GET /api/resume-versions/{versionId}/suggestions`
Retrieves suggestions for a version.

#### `PATCH /api/resume-suggestions/{suggestionId}/status`
Updates status of a suggestion:
```json
{
  "status": "ACCEPTED" // ACCEPTED, REJECTED, PENDING
}
```

---

### 4.3. Dual-Version Score Comparison Simulation

#### `POST /api/resume-simulations`
Executes side-by-side simulation comparing baseline Version A against improved Version B using identical 80/20 scoring algorithms.
- **Request Body**:
```json
{
  "baseVersionId": "uuid-v1",
  "improvedVersionId": "uuid-v2",
  "jobId": "uuid-job"
}
```
- **Response `201 Created`**: Returns `ResumeSimulationDto` with `baseScore`, `improvedScore`, `scoreDelta`, `skillCoverageBase`, `skillCoverageImproved`, `projectRelevanceBase`, `projectRelevanceImproved`, `positiveChanges`, and `regressions`.

#### `GET /api/resume-simulations`
Lists candidate's past simulation runs.

#### `GET /api/resume-simulations/{id}`
Retrieves specific simulation details.

### `GET /health`
- **Response `200 OK`**: `{"status": "UP", "service": "ai-service"}`


---

## 4. Career Intelligence Platform APIs

### 4.1. Skill-Gap Roadmap Endpoints

#### Generate or Retrieve Roadmap
- **POST** `/api/roadmaps/generate`
- **Request Body**:
```json
{
  "jobId": "UUID",
  "resumeId": "UUID"
}
```
- **Response** `201 Created`:
```json
{
  "success": true,
  "message": "Skill-gap roadmap generated successfully",
  "data": {
    "id": "UUID",
    "jobId": "UUID",
    "jobTitle": "Backend Engineer",
    "company": "Tech Corp",
    "resumeId": "UUID",
    "resumeFileName": "resume.pdf",
    "overallCoveragePct": 65.0,
    "demonstratedSkillsCount": 3,
    "mentionedSkillsCount": 2,
    "missingSkillsCount": 2,
    "totalTasksCount": 5,
    "completedTasksCount": 1,
    "items": [
      {
        "id": "UUID",
        "skillName": "TensorFlow",
        "status": "MISSING",
        "priority": "HIGH",
        "priorityReason": "Mandatory deep learning framework requirement with high frequency",
        "whyRequired": "Essential for training neural networks in computer vision pipelines",
        "learningObjectives": ["Understand computational graphs", "Build CNN classification models"],
        "learningSequence": ["1. Fundamentals of Tensors", "2. Sequential & Functional APIs", "3. Model Training & Evaluation"],
        "practicalTasks": "Train a multi-class image classifier using TensorFlow/Keras",
        "suggestedProject": "Real-time gesture recognition pipeline",
        "estimatedDuration": "1-2 weeks",
        "completionCriteria": "Deploy trained model and verify validation loss under 0.2",
        "progressStatus": "NOT_STARTED",
        "completedAt": null
      }
    ]
  }
}
```

#### Regenerate Roadmap
- **POST** `/api/roadmaps/regenerate`
- Forces re-evaluation against latest resume and job description.

#### List User Roadmaps
- **GET** `/api/roadmaps`
- Returns all roadmaps generated by the authenticated candidate.

#### Update Item Progress Status
- **PATCH** `/api/roadmaps/{roadmapId}/items/{itemId}/progress`
- **Request Body**:
```json
{
  "progressStatus": "IN_PROGRESS" // NOT_STARTED, IN_PROGRESS, COMPLETED
}
```

---

### 4.2. Resume Versioning & Simulator Endpoints

#### Create Resume Version
- **POST** `/api/resumes/{resumeId}/versions`
- **Request Body**:
```json
{
  "versionName": "Tailored for AI Engineer",
  "targetJobId": "UUID",
  "contentJson": "{\"summary\": \"...\"}",
  "rawText": "..."
}
```
- **Behavior**: Preserves parent resume immutability. Increments `versionNumber` sequentially.

#### List Versions for Resume
- **GET** `/api/resumes/{resumeId}/versions`

#### List All User Versions
- **GET** `/api/resume-versions`

#### Update Version Draft
- **PUT** `/api/resume-versions/{versionId}`

#### Generate Zero-Hallucination Improvement Suggestions
- **POST** `/api/resume-versions/{versionId}/suggestions`
- **Request Body**:
```json
{
  "jobId": "UUID"
}
```

#### Update Suggestion Status
- **PATCH** `/api/resume-suggestions/{suggestionId}/status`
- **Request Body**:
```json
{
  "status": "ACCEPTED" // ACCEPTED, REJECTED, PENDING
}
```

#### Run Dual-Version Simulation
- **POST** `/api/resume-simulations`
- **Request Body**:
```json
{
  "baseVersionId": "UUID",
  "improvedVersionId": "UUID",
  "jobId": "UUID"
}
```
- **Response** `201 Created`:
```json
{
  "success": true,
  "message": "Resume simulation completed successfully",
  "data": {
    "id": "UUID",
    "baseVersionId": "UUID",
    "baseVersionName": "Version 1 (Original)",
    "improvedVersionId": "UUID",
    "improvedVersionName": "Version 2 (Tailored)",
    "jobId": "UUID",
    "jobTitle": "Backend Engineer",
    "baseScore": 58.0,
    "improvedScore": 84.0,
    "scoreDelta": 26.0,
    "skillCoverageBase": 55.0,
    "skillCoverageImproved": 88.0,
    "projectRelevanceBase": 40.0,
    "projectRelevanceImproved": 78.0,
    "explanation": "Revisions expanded concrete evidence for required frameworks...",
    "positiveChanges": [
      "Added required skill Docker (+25.0% coverage)",
      "Replaced weak descriptions with quantified project impact"
    ],
    "regressions": []
  }
}
```

---

### 4.3. FastAPI Internal AI Engine Endpoints (`ai-service:8001`)

- **POST** `/internal/roadmap/generate`: Generates classification graph, priority matrix, learning milestones.
- **POST** `/internal/simulator/suggestions`: Generates strictly grounded, zero-hallucination sentence improvements.
- **POST** `/internal/simulator/simulate`: Computes dual-score metrics using identical 80/20 weights and detects positive/negative deltas.
