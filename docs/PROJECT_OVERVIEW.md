# AI-Powered Resume Analyzer and Job Matcher

## Project Overview
An intelligent, full-stack enterprise application designed to streamline the recruitment and job-seeking lifecycle. The system provides AI-driven resume parsing, multi-category skill extraction, professional resume enhancement suggestions, semantic job matching, and real-time application tracking.

## Monorepo Architecture
The project is organized as a three-tier microservice architecture:
1. **Frontend (`/frontend`)**: React.js SPA built with Vite, Tailwind CSS, React Router, and Axios.
2. **Backend (`/backend`)**: Java 21 & Spring Boot 3.3.x REST API handling authentication (JWT), user management, resume storage, database persistence via Spring Data JPA, and Flyway database migrations.
3. **AI Service (`/ai-service`)**: Python 3 FastAPI microservice powering resume text extraction (PDF & DOCX via PyMuPDF and python-docx), NLP-based entity & skill extraction, rule-based suggestion evaluation, and optional LLM integration.
4. **Storage (`/storage/resumes`)**: Secure local disk storage for uploaded resume documents.

## Team Ownership & Modular Responsibilities
- **Member 1 (Current Scope)**:
  - Authentication & User Management (JWT, Spring Security)
  - User Profile Management
  - Resume Upload, Secure Storage & Management
  - Resume Parsing & Multi-Category Information Extraction (FastAPI + PyMuPDF / python-docx)
  - Resume Analysis Dashboard & Rule-Based / LLM Improvement Suggestions
- **Member 2**:
  - Job Management & Job Description Parsing
  - Job Matching & Compatibility Scoring Engine
  - Missing Skill Identification & Job Recommendations
  - Application & Interview Tracking
  - Recruitment Analytics Dashboard

## Differentiating Capabilities: Career Intelligence Platform
Beyond conventional ATS scanning, the platform elevates candidate success with:
1. **Personalized Skill-Gap Roadmap**:
   - Evidence-based classification: Demonstrated, Mentioned, Missing, and Related skills.
   - Transparent, deterministic priority calculation (High, Medium, Low) considering job criticality, semantic relatedness, and learning effort.
   - Actionable step-by-step milestones with practical exercises, suggested mini-projects, duration estimates, and completion criteria.
   - Candidate-isolated persistent progress tracking (`NOT_STARTED`, `IN_PROGRESS`, `COMPLETED`).
2. **Resume Improvement Simulator with Version Comparison**:
   - Immutable original resume preservation with multi-version branching (`resume_versions`).
   - Zero-hallucination AI suggestion engine grounded in actual candidate content.
   - Interactive split-screen editor with single-click revision application and manual overrides.
   - Dual-version simulation comparison with identical 80/20 scoring algorithm, quantified match score deltas, and automated regression detection.
