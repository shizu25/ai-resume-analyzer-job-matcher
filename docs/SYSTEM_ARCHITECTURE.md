# System Architecture

## Architecture Diagram

```
+-------------------------------------------------------------+
|                      React Frontend                         |
|      (Vite + Tailwind CSS + React Router + Axios)          |
+------------------------------+------------------------------+
                               |
                               | REST API (JSON)
                               v
+-------------------------------------------------------------+
|                   Spring Boot 3.3 Backend                   |
|       (Security, JWT, JPA, Flyway, File Storage Engine)     |
+---------------+-----------------------------+---------------+
                |                             |
                | JDBC / SQL                  | HTTP REST (Internal)
                v                             v
+-------------------------------+ +---------------------------+
|      PostgreSQL Database      | |   Python FastAPI Service  |
|  (Users, Profiles, Resumes,   | | (PyMuPDF, docx, NLP,      |
|   Skills, Education, Exp,     | |  Rule Engine & LLM)       |
|   Projects, Suggestions)      | |                           |
+-------------------------------+ +---------------------------+
```

## Security & Network Boundaries
1. **Frontend Isolation**: React communicates exclusively with Spring Boot on `http://localhost:8080/api`. The internal AI service (`http://localhost:8001`) is never exposed directly to the browser.
2. **Authentication**: Stateless JWT token authentication with BCrypt password hashing.
3. **Authorization & Resource Ownership**: Spring Boot strictly enforces user ownership on all endpoints (`/api/resumes/**`, `/api/users/**`). A user cannot access, modify, or download another user's documents.
4. **File Storage**: Uploaded files are renamed with safe UUID identifiers and stored in a non-web-accessible directory (`/storage/resumes`). MIME types and magic byte headers are strictly validated.
5. **Database Integrity**: Hibernate schema auto-generation is disabled (`spring.jpa.hibernate.ddl-auto=validate`). All database state is managed strictly through versioned Flyway migrations.
