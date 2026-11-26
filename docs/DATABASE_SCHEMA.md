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

*(Note: Member 2 tables `jobs`, `applications`, `job_match_results` link cleanly to `resumes(id)` and `users(id)`).*

---

## 4. Career Intelligence Platform (V4 Migration)

### 4.1. `skill_roadmaps`
Stores personalized skill-gap assessment summaries generated against a target job.
```sql
CREATE TABLE skill_roadmaps (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    job_id UUID NOT NULL REFERENCES jobs(id) ON DELETE CASCADE,
    resume_id UUID NOT NULL REFERENCES resumes(id) ON DELETE CASCADE,
    overall_coverage_pct DOUBLE PRECISION NOT NULL,
    demonstrated_skills_count INT NOT NULL DEFAULT 0,
    mentioned_skills_count INT NOT NULL DEFAULT 0,
    missing_skills_count INT NOT NULL DEFAULT 0,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT uq_skill_roadmaps_user_job_resume UNIQUE (user_id, job_id, resume_id),
    CONSTRAINT chk_skill_roadmaps_coverage CHECK (overall_coverage_pct >= 0.0 AND overall_coverage_pct <= 100.0)
);
```

### 4.2. `roadmap_items`
Individual learning milestones categorized into Demonstrated, Mentioned, Missing, or Related with transparent priority scoring and status tracking.
```sql
CREATE TABLE roadmap_items (
    id UUID PRIMARY KEY,
    roadmap_id UUID NOT NULL REFERENCES skill_roadmaps(id) ON DELETE CASCADE,
    skill_name VARCHAR(100) NOT NULL,
    status VARCHAR(50) NOT NULL, -- DEMONSTRATED, MENTIONED, MISSING, RELATED
    priority VARCHAR(20) NOT NULL, -- HIGH, MEDIUM, LOW
    priority_reason TEXT NOT NULL,
    why_required TEXT NOT NULL,
    learning_objectives TEXT NOT NULL,
    learning_sequence TEXT NOT NULL,
    practical_tasks TEXT NOT NULL,
    suggested_project TEXT NOT NULL,
    estimated_duration VARCHAR(100) NOT NULL,
    completion_criteria TEXT NOT NULL,
    progress_status VARCHAR(30) NOT NULL DEFAULT 'NOT_STARTED', -- NOT_STARTED, IN_PROGRESS, COMPLETED
    completed_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL
);
```

### 4.3. `resume_versions`
Preserves immutable original resumes while tracking editable candidate variants.
```sql
CREATE TABLE resume_versions (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    parent_resume_id UUID NOT NULL REFERENCES resumes(id) ON DELETE CASCADE,
    version_number INT NOT NULL,
    version_name VARCHAR(255) NOT NULL,
    target_job_id UUID REFERENCES jobs(id) ON DELETE SET NULL,
    content_json TEXT NOT NULL,
    raw_text TEXT NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT uq_resume_versions_parent_number UNIQUE (parent_resume_id, version_number)
);
```

### 4.4. `resume_improvement_suggestions`
Grounded, zero-hallucination suggestions for specific resume versions against target roles.
```sql
CREATE TABLE resume_improvement_suggestions (
    id UUID PRIMARY KEY,
    version_id UUID NOT NULL REFERENCES resume_versions(id) ON DELETE CASCADE,
    job_id UUID NOT NULL REFERENCES jobs(id) ON DELETE CASCADE,
    category VARCHAR(100) NOT NULL,
    original_text TEXT NOT NULL,
    suggested_text TEXT NOT NULL,
    issue TEXT NOT NULL,
    reason TEXT NOT NULL,
    related_requirement VARCHAR(255),
    expected_benefit TEXT NOT NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'PENDING', -- PENDING, ACCEPTED, REJECTED
    created_at TIMESTAMP WITH TIME ZONE NOT NULL
);
```

### 4.5. `resume_simulations`
Dual-version match score comparison records tracking deltas, positive changes, and regressions.
```sql
CREATE TABLE resume_simulations (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    base_version_id UUID NOT NULL REFERENCES resume_versions(id) ON DELETE CASCADE,
    improved_version_id UUID NOT NULL REFERENCES resume_versions(id) ON DELETE CASCADE,
    job_id UUID NOT NULL REFERENCES jobs(id) ON DELETE CASCADE,
    base_score DOUBLE PRECISION NOT NULL,
    improved_score DOUBLE PRECISION NOT NULL,
    score_delta DOUBLE PRECISION NOT NULL,
    skill_coverage_base DOUBLE PRECISION NOT NULL,
    skill_coverage_improved DOUBLE PRECISION NOT NULL,
    project_relevance_base DOUBLE PRECISION NOT NULL,
    project_relevance_improved DOUBLE PRECISION NOT NULL,
    explanation TEXT NOT NULL,
    positive_changes TEXT NOT NULL,
    regressions TEXT NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL
);
```


## Feature Evolution: Career Intelligence Platform (Migration V4)

### 11. `skill_roadmaps`
- `id` (UUID, PRIMARY KEY)
- `user_id` (UUID, NOT NULL, FOREIGN KEY -> users(id) ON DELETE CASCADE)
- `job_id` (UUID, NOT NULL, FOREIGN KEY -> jobs(id) ON DELETE CASCADE)
- `resume_id` (UUID, NOT NULL, FOREIGN KEY -> resumes(id) ON DELETE CASCADE)
- `overall_coverage_pct` (DOUBLE PRECISION NOT NULL)
- `demonstrated_skills_count` (INT NOT NULL DEFAULT 0)
- `mentioned_skills_count` (INT NOT NULL DEFAULT 0)
- `missing_skills_count` (INT NOT NULL DEFAULT 0)
- `created_at` (TIMESTAMP WITH TIME ZONE, NOT NULL)
- `updated_at` (TIMESTAMP WITH TIME ZONE, NOT NULL)
- **Constraint**: UNIQUE (user_id, job_id, resume_id)

### 12. `roadmap_items`
- `id` (UUID, PRIMARY KEY)
- `roadmap_id` (UUID, NOT NULL, FOREIGN KEY -> skill_roadmaps(id) ON DELETE CASCADE)
- `skill_name` (VARCHAR(100), NOT NULL)
- `status` (VARCHAR(50), NOT NULL) -- DEMONSTRATED, MENTIONED, MISSING, RELATED
- `priority` (VARCHAR(20), NOT NULL) -- HIGH, MEDIUM, LOW
- `priority_reason` (TEXT NOT NULL)
- `why_required` (TEXT NOT NULL)
- `learning_objectives` (TEXT NOT NULL, JSON List)
- `learning_sequence` (TEXT NOT NULL, JSON List)
- `practical_tasks` (TEXT NOT NULL)
- `suggested_project` (TEXT NOT NULL)
- `estimated_duration` (VARCHAR(100), NOT NULL)
- `completion_criteria` (TEXT NOT NULL)
- `progress_status` (VARCHAR(30), NOT NULL DEFAULT 'NOT_STARTED') -- NOT_STARTED, IN_PROGRESS, COMPLETED
- `completed_at` (TIMESTAMP WITH TIME ZONE)
- `created_at` (TIMESTAMP WITH TIME ZONE, NOT NULL)
- `updated_at` (TIMESTAMP WITH TIME ZONE, NOT NULL)

### 13. `resume_versions`
- `id` (UUID, PRIMARY KEY)
- `user_id` (UUID, NOT NULL, FOREIGN KEY -> users(id) ON DELETE CASCADE)
- `parent_resume_id` (UUID, NOT NULL, FOREIGN KEY -> resumes(id) ON DELETE CASCADE)
- `version_number` (INT NOT NULL)
- `version_name` (VARCHAR(255), NOT NULL)
- `target_job_id` (UUID, FOREIGN KEY -> jobs(id) ON DELETE SET NULL)
- `content_json` (TEXT NOT NULL)
- `raw_text` (TEXT NOT NULL)
- `created_at` (TIMESTAMP WITH TIME ZONE, NOT NULL)
- `updated_at` (TIMESTAMP WITH TIME ZONE, NOT NULL)
- **Constraint**: UNIQUE (parent_resume_id, version_number)

### 14. `resume_improvement_suggestions`
- `id` (UUID, PRIMARY KEY)
- `version_id` (UUID, NOT NULL, FOREIGN KEY -> resume_versions(id) ON DELETE CASCADE)
- `job_id` (UUID, NOT NULL, FOREIGN KEY -> jobs(id) ON DELETE CASCADE)
- `category` (VARCHAR(100), NOT NULL)
- `original_text` (TEXT NOT NULL)
- `suggested_text` (TEXT NOT NULL)
- `issue` (TEXT NOT NULL)
- `reason` (TEXT NOT NULL)
- `related_requirement` (VARCHAR(255))
- `expected_benefit` (TEXT NOT NULL)
- `status` (VARCHAR(30), NOT NULL DEFAULT 'PENDING') -- PENDING, ACCEPTED, REJECTED
- `created_at` (TIMESTAMP WITH TIME ZONE, NOT NULL)

### 15. `resume_simulations`
- `id` (UUID, PRIMARY KEY)
- `user_id` (UUID, NOT NULL, FOREIGN KEY -> users(id) ON DELETE CASCADE)
- `base_version_id` (UUID, NOT NULL, FOREIGN KEY -> resume_versions(id) ON DELETE CASCADE)
- `improved_version_id` (UUID, NOT NULL, FOREIGN KEY -> resume_versions(id) ON DELETE CASCADE)
- `job_id` (UUID, NOT NULL, FOREIGN KEY -> jobs(id) ON DELETE CASCADE)
- `base_score` (DOUBLE PRECISION NOT NULL)
- `improved_score` (DOUBLE PRECISION NOT NULL)
- `score_delta` (DOUBLE PRECISION NOT NULL)
- `skill_coverage_base` (DOUBLE PRECISION NOT NULL)
- `skill_coverage_improved` (DOUBLE PRECISION NOT NULL)
- `project_relevance_base` (DOUBLE PRECISION NOT NULL)
- `project_relevance_improved` (DOUBLE PRECISION NOT NULL)
- `explanation` (TEXT NOT NULL)
- `positive_changes` (TEXT NOT NULL, JSON List)
- `regressions` (TEXT NOT NULL, JSON List)
- `created_at` (TIMESTAMP WITH TIME ZONE, NOT NULL)
