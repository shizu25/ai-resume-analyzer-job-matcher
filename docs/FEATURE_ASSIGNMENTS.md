# Feature Assignments & Team Boundaries

## Member 1 Responsibilities (Current Implementation Scope)
1. **Module A - Authentication & User Management**:
   - User registration & login with JWT.
   - BCrypt password encryption & token lifecycle.
   - Secure HttpOnly / Bearer token storage.
2. **Module B - User Profile Management**:
   - Profile retrieval and update APIs (`/api/users/me`).
   - Profile editing UI with social links, headline, and career interests.
3. **Module C - Resume Upload & Secure Storage**:
   - Multipart file upload (PDF and DOCX).
   - Magic byte validation and size limit enforcement (10MB).
   - Server-side UUID file naming and isolation in `/storage/resumes`.
4. **Module D - Resume Management**:
   - Resumes list, preview/download with strict ownership validation.
   - Active resume toggle and resume deletion (including storage cleanup).
5. **Module E & F - Resume Parsing & Information Extraction**:
   - FastAPI microservice with PyMuPDF and python-docx.
   - Candidate contact information extraction.
   - Multi-category skill extraction using categorized dictionaries and NLP.
   - Education, work experience, and project extraction.
   - Scan/image-only PDF detection with clear extraction warnings.
6. **Module G - Resume Analysis Interface**:
   - Rich interactive resume analysis page with tabs/sections for parsed entities.
   - Parsing warnings, confidence indicators, and raw text preview.
7. **Module H - Resume Improvement Suggestions**:
   - Rule-based analysis for missing sections, weak bullet points, and unquantified achievements.
   - Configurable LLM suggestion enhancement with graceful offline fallback.

---

## Member 2 Responsibilities (Separate Scope - Do Not Modify)
1. Job Management (CRUD, job listing, company management).
2. Job Description Parsing & skill requirement extraction.
3. Semantic Job Matching Engine & Compatibility Scoring.
4. Missing Skill Gap Analysis & Learning Recommendations.
5. Job Recommendations Interface.
6. Application & Interview Lifecycle Tracking.
7. Recruitment & Application Analytics Dashboard.
