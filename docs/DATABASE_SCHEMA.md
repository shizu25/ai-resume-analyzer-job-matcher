# Database Schema Specification

## Migration Strategy
- **Migration Tool**: Flyway (`backend/src/main/resources/db/migration/`)
- **DDL Validation**: `spring.jpa.hibernate.ddl-auto=validate`
- **ID Strategy**: Universally Unique Identifiers (UUID v4) for all primary keys.

## Tables Overview

### 1. `users` (Managed by Member 1)
- `id` (UUID, PRIMARY KEY)
- `email` (VARCHAR(255), UNIQUE, NOT NULL)
- `password_hash` (VARCHAR(255), NOT NULL)
- `role` (VARCHAR(50), NOT NULL DEFAULT 'ROLE_USER')
- `created_at` (TIMESTAMP WITH TIME ZONE, NOT NULL)
- `updated_at` (TIMESTAMP WITH TIME ZONE, NOT NULL)

### 2. `user_profiles` (Managed by Member 1)
- `id` (UUID, PRIMARY KEY)
- `user_id` (UUID, UNIQUE, NOT NULL, FOREIGN KEY -> users(id) ON DELETE CASCADE)
- `full_name` (VARCHAR(255))
- `phone` (VARCHAR(50))
- `location` (VARCHAR(255))
- `headline` (VARCHAR(255))
- `linkedin_url` (VARCHAR(500))
- `github_url` (VARCHAR(500))
- `portfolio_url` (VARCHAR(500))
- `career_interests` (TEXT)
- `created_at` (TIMESTAMP WITH TIME ZONE, NOT NULL)
- `updated_at` (TIMESTAMP WITH TIME ZONE, NOT NULL)

### 3. `resumes` (Managed by Member 1)
- `id` (UUID, PRIMARY KEY)
- `user_id` (UUID, NOT NULL, FOREIGN KEY -> users(id) ON DELETE CASCADE)
- `file_name` (VARCHAR(255), NOT NULL) -- Original filename
- `stored_file_name` (VARCHAR(255), NOT NULL) -- Secure disk filename
- `file_path` (VARCHAR(500), NOT NULL)
- `file_type` (VARCHAR(100), NOT NULL)
- `file_size` (BIGINT, NOT NULL)
- `is_active` (BOOLEAN NOT NULL DEFAULT FALSE)
- `parsing_status` (VARCHAR(50), NOT NULL DEFAULT 'UPLOADED') -- UPLOADED, PROCESSING, COMPLETED, FAILED
- `parsed_text` (TEXT)
- `created_at` (TIMESTAMP WITH TIME ZONE, NOT NULL)
- `updated_at` (TIMESTAMP WITH TIME ZONE, NOT NULL)

### 4. `resume_skills` (Managed by Member 1)
- `id` (UUID, PRIMARY KEY)
- `resume_id` (UUID, NOT NULL, FOREIGN KEY -> resumes(id) ON DELETE CASCADE)
- `name` (VARCHAR(100), NOT NULL)
- `category` (VARCHAR(100), NOT NULL) -- Programming, Frontend, Backend, Database, Cloud/DevOps, Soft Skills, Other
- `created_at` (TIMESTAMP WITH TIME ZONE, NOT NULL)

### 5. `resume_education` (Managed by Member 1)
- `id` (UUID, PRIMARY KEY)
- `resume_id` (UUID, NOT NULL, FOREIGN KEY -> resumes(id) ON DELETE CASCADE)
- `institution` (VARCHAR(255))
- `degree` (VARCHAR(255))
- `field_of_study` (VARCHAR(255))
- `start_year` (INTEGER)
- `end_year` (INTEGER)
- `grade` (VARCHAR(50))
- `created_at` (TIMESTAMP WITH TIME ZONE, NOT NULL)

### 6. `resume_experience` (Managed by Member 1)
- `id` (UUID, PRIMARY KEY)
- `resume_id` (UUID, NOT NULL, FOREIGN KEY -> resumes(id) ON DELETE CASCADE)
- `company` (VARCHAR(255))
- `position` (VARCHAR(255))
- `location` (VARCHAR(255))
- `start_date` (VARCHAR(50))
- `end_date` (VARCHAR(50))
- `is_current` (BOOLEAN DEFAULT FALSE)
- `description` (TEXT)
- `created_at` (TIMESTAMP WITH TIME ZONE, NOT NULL)

### 7. `resume_projects` (Managed by Member 1)
- `id` (UUID, PRIMARY KEY)
- `resume_id` (UUID, NOT NULL, FOREIGN KEY -> resumes(id) ON DELETE CASCADE)
- `title` (VARCHAR(255))
- `description` (TEXT)
- `technologies` (TEXT)
- `link` (VARCHAR(500))
- `created_at` (TIMESTAMP WITH TIME ZONE, NOT NULL)

### 8. `resume_suggestions` (Managed by Member 1)
- `id` (UUID, PRIMARY KEY)
- `resume_id` (UUID, NOT NULL, FOREIGN KEY -> resumes(id) ON DELETE CASCADE)
- `category` (VARCHAR(100), NOT NULL)
- `priority` (VARCHAR(50), NOT NULL) -- high, medium, low
- `issue` (TEXT, NOT NULL)
- `recommendation` (TEXT, NOT NULL)
- `created_at` (TIMESTAMP WITH TIME ZONE, NOT NULL)

### 9. `resume_warnings` (Managed by Member 1)
- `id` (UUID, PRIMARY KEY)
- `resume_id` (UUID, NOT NULL, FOREIGN KEY -> resumes(id) ON DELETE CASCADE)
- `warning_message` (TEXT, NOT NULL)
- `created_at` (TIMESTAMP WITH TIME ZONE, NOT NULL)

*(Note: Future tables for Member 2 such as `jobs`, `job_skills`, `applications` will link cleanly to `resumes(id)` and `users(id)` without schema collisions).*
