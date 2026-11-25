# Development Guidelines

## 1. Branching Strategy
- Main branch: `main`
- Integration branch: `develop`
- Feature branch (Member 1): `feature/member-1-resume-management`
- Feature branch (Member 2): `feature/member-2-job-matching`
- **Rules**:
  - Never commit directly to `develop` or `main`.
  - Never merge feature branches directly into `develop` without team code review.
  - Commit with conventional commits: `feat:`, `fix:`, `docs:`, `test:`, `refactor:`.

## 2. Code Standards
- **Java (Spring Boot)**:
  - Java 21 LTS standard features.
  - Clean architecture with controllers, services, repositories, and DTOs.
  - No direct entity exposure in API responses; always use request/response DTOs.
  - Global exception handling using `@RestControllerAdvice`.
- **Python (FastAPI)**:
  - PEP 8 compliant, type-annotated code.
  - Pydantic models for request/response serialization.
  - Modular parser structure separating PDF/DOCX extractors, section detect, skill dictionary, and suggestions.
- **Frontend (React)**:
  - Functional components with React Hooks.
  - Clean modular components with Tailwind CSS styling.
  - Centralized Axios client with automatic token attachment and error interceptors.
- **Database (Flyway & PostgreSQL)**:
  - All migrations versioned monotonically: `V1__...`, `V2__...`.
  - Schema modification only through Flyway; never modify existing migrations.
  - UUIDs for all primary keys.
