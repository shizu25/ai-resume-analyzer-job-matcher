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

### `GET /health`
- **Response `200 OK`**: `{"status": "UP", "service": "ai-service"}`
