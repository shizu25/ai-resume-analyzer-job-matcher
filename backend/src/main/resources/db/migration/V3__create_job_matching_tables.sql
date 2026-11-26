-- Migration: V3__create_job_matching_tables.sql
-- Description: Create jobs, job_matches, and job_applications tables with constraints, indexes, and initial INR seed jobs

CREATE TABLE jobs (
    id UUID PRIMARY KEY,
    created_by UUID REFERENCES users(id) ON DELETE SET NULL,
    title VARCHAR(255) NOT NULL,
    company VARCHAR(255) NOT NULL,
    location VARCHAR(255) NOT NULL,
    work_mode VARCHAR(50) NOT NULL DEFAULT 'ONSITE',
    job_type VARCHAR(50) NOT NULL DEFAULT 'FULL_TIME',
    experience_level VARCHAR(50) NOT NULL DEFAULT 'MID',
    salary_min INTEGER,
    salary_max INTEGER,
    salary_currency VARCHAR(10) NOT NULL DEFAULT 'INR',
    salary_period VARCHAR(20) NOT NULL DEFAULT 'ANNUAL',
    description TEXT NOT NULL,
    required_skills TEXT NOT NULL,
    preferred_skills TEXT,
    application_url VARCHAR(500),
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT chk_jobs_work_mode CHECK (work_mode IN ('ONSITE', 'REMOTE', 'HYBRID')),
    CONSTRAINT chk_jobs_job_type CHECK (job_type IN ('FULL_TIME', 'PART_TIME', 'CONTRACT', 'INTERNSHIP')),
    CONSTRAINT chk_jobs_experience_level CHECK (experience_level IN ('ENTRY', 'MID', 'SENIOR', 'LEAD')),
    CONSTRAINT chk_jobs_salary_period CHECK (salary_period IN ('ANNUAL', 'HOURLY')),
    CONSTRAINT chk_jobs_salary_range CHECK (salary_min IS NULL OR salary_max IS NULL OR salary_min <= salary_max)
);

CREATE TABLE job_matches (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    resume_id UUID NOT NULL REFERENCES resumes(id) ON DELETE CASCADE,
    job_id UUID NOT NULL REFERENCES jobs(id) ON DELETE CASCADE,
    match_score DOUBLE PRECISION NOT NULL,
    required_skill_match_pct DOUBLE PRECISION NOT NULL,
    preferred_skill_match_pct DOUBLE PRECISION NOT NULL DEFAULT 0.0,
    matched_skills TEXT NOT NULL,
    missing_skills TEXT NOT NULL,
    recommendations TEXT NOT NULL,
    calculated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT uq_job_matches_resume_job UNIQUE (resume_id, job_id),
    CONSTRAINT chk_job_matches_score CHECK (match_score >= 0.0 AND match_score <= 100.0),
    CONSTRAINT chk_job_matches_req_pct CHECK (required_skill_match_pct >= 0.0 AND required_skill_match_pct <= 100.0),
    CONSTRAINT chk_job_matches_pref_pct CHECK (preferred_skill_match_pct >= 0.0 AND preferred_skill_match_pct <= 100.0)
);

CREATE TABLE job_applications (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    job_id UUID NOT NULL REFERENCES jobs(id) ON DELETE CASCADE,
    resume_id UUID REFERENCES resumes(id) ON DELETE SET NULL,
    status VARCHAR(50) NOT NULL DEFAULT 'APPLIED',
    match_score_snapshot DOUBLE PRECISION,
    notes TEXT,
    applied_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT uq_job_applications_user_job UNIQUE (user_id, job_id),
    CONSTRAINT chk_job_applications_status CHECK (status IN ('APPLIED', 'INTERVIEWING', 'OFFER', 'REJECTED')),
    CONSTRAINT chk_job_applications_match_snapshot CHECK (match_score_snapshot IS NULL OR (match_score_snapshot >= 0.0 AND match_score_snapshot <= 100.0))
);

-- Indexes for jobs
CREATE INDEX idx_jobs_created_by ON jobs(created_by);
CREATE INDEX idx_jobs_is_active ON jobs(is_active);
CREATE INDEX idx_jobs_work_mode ON jobs(work_mode);
CREATE INDEX idx_jobs_job_type ON jobs(job_type);
CREATE INDEX idx_jobs_experience_level ON jobs(experience_level);
CREATE INDEX idx_jobs_created_at ON jobs(created_at DESC);

-- Indexes for job_matches
CREATE INDEX idx_job_matches_user_id ON job_matches(user_id);
CREATE INDEX idx_job_matches_resume_id ON job_matches(resume_id);
CREATE INDEX idx_job_matches_job_id ON job_matches(job_id);
CREATE INDEX idx_job_matches_user_score ON job_matches(user_id, match_score DESC);

-- Indexes for job_applications
CREATE INDEX idx_job_applications_user_id ON job_applications(user_id);
CREATE INDEX idx_job_applications_job_id ON job_applications(job_id);
CREATE INDEX idx_job_applications_status ON job_applications(status);
CREATE INDEX idx_job_applications_user_applied_at ON job_applications(user_id, applied_at DESC);

-- Seed 8 realistic technology jobs (created_by = NULL protects system-seeded jobs; application_url = NULL)
INSERT INTO jobs (
    id, created_by, title, company, location, work_mode, job_type, experience_level,
    salary_min, salary_max, salary_currency, salary_period, description, required_skills, preferred_skills,
    application_url, is_active, created_at, updated_at
) VALUES
(
    '11111111-1111-4111-8111-111111111101', NULL,
    'Senior Java Backend Engineer', 'Nimbus Cloud Systems', 'Bengaluru, Karnataka', 'HYBRID', 'FULL_TIME', 'SENIOR',
    2200000, 3200000, 'INR', 'ANNUAL',
    'Design and scale distributed REST APIs and event-driven microservices powering our enterprise cloud control plane.',
    '["Java","Spring Boot","PostgreSQL","Docker","REST API","Microservices"]',
    '["Kubernetes","Kafka","Redis","AWS","Flyway"]',
    NULL, TRUE, NOW(), NOW()
),
(
    '11111111-1111-4111-8111-111111111102', NULL,
    'Full-Stack Software Engineer (React & Spring)', 'FinPulse Technologies', 'Mumbai, Maharashtra', 'HYBRID', 'FULL_TIME', 'MID',
    1400000, 2100000, 'INR', 'ANNUAL',
    'Build modern financial analytics dashboards in React and resilient backend APIs using Java and Spring Boot.',
    '["Java","Spring Boot","React","TypeScript","PostgreSQL","Tailwind CSS"]',
    '["Docker","Redis","GitHub Actions","JUnit"]',
    NULL, TRUE, NOW(), NOW()
),
(
    '11111111-1111-4111-8111-111111111103', NULL,
    'Python AI & NLP Engineer', 'VectorMind AI', 'Remote', 'REMOTE', 'FULL_TIME', 'MID',
    1600000, 2400000, 'INR', 'ANNUAL',
    'Develop document extraction pipelines, NLP entity classifiers, and LLM-assisted recommendation engines.',
    '["Python","FastAPI","NLP","Machine Learning","Docker","PostgreSQL"]',
    '["PyTorch","Hugging Face","LangChain","Transformers","Redis"]',
    NULL, TRUE, NOW(), NOW()
),
(
    '11111111-1111-4111-8111-111111111104', NULL,
    'Frontend React Engineer', 'PixelForge Studio', 'Hyderabad, Telangana', 'REMOTE', 'FULL_TIME', 'MID',
    1100000, 1700000, 'INR', 'ANNUAL',
    'Craft accessible, responsive SaaS interfaces with React, modern state management, and design systems.',
    '["JavaScript","TypeScript","React","HTML5","CSS3","Tailwind CSS"]',
    '["Next.js","Vite","Redux Toolkit","Jest","Cypress"]',
    NULL, TRUE, NOW(), NOW()
),
(
    '11111111-1111-4111-8111-111111111105', NULL,
    'DevOps & Cloud Platform Engineer', 'Stratos Infrastructure', 'Pune, Maharashtra', 'HYBRID', 'FULL_TIME', 'SENIOR',
    2000000, 3000000, 'INR', 'ANNUAL',
    'Automate multi-region Kubernetes clusters, CI/CD pipelines, observability stacks, and infrastructure-as-code.',
    '["Docker","Kubernetes","AWS","Terraform","CI/CD","Linux"]',
    '["GitHub Actions","Prometheus","Grafana","Ansible","Nginx"]',
    NULL, TRUE, NOW(), NOW()
),
(
    '11111111-1111-4111-8111-111111111106', NULL,
    'Junior Software Developer', 'LaunchPad Labs', 'Chennai, Tamil Nadu', 'ONSITE', 'FULL_TIME', 'ENTRY',
    600000, 950000, 'INR', 'ANNUAL',
    'Collaborate with senior engineers on API development, SQL schema design, and frontend bug fixes in an Agile team.',
    '["Java","Python","SQL","Git","REST API"]',
    '["React","Spring Boot","PostgreSQL","Agile","JUnit"]',
    NULL, TRUE, NOW(), NOW()
),
(
    '11111111-1111-4111-8111-111111111107', NULL,
    'Data & ML Platform Intern', 'QuantScale Analytics', 'Gurugram, Haryana', 'HYBRID', 'INTERNSHIP', 'ENTRY',
    400, 600, 'INR', 'HOURLY',
    'Support data ingestion pipelines, exploratory analysis, and automated evaluation suites for ML models.',
    '["Python","SQL","Pandas","NumPy","Git"]',
    '["Scikit-Learn","FastAPI","Docker","PostgreSQL"]',
    NULL, TRUE, NOW(), NOW()
),
(
    '11111111-1111-4111-8111-111111111108', NULL,
    'Node.js & TypeScript API Contractor', 'RelayCommerce', 'Remote', 'REMOTE', 'CONTRACT', 'MID',
    1200, 1800, 'INR', 'HOURLY',
    'Architect high-throughput e-commerce checkout APIs, queue workers, and caching layers.',
    '["TypeScript","Node.js","Express","PostgreSQL","Redis","Docker"]',
    '["GraphQL","AWS","RabbitMQ","Jest","CI/CD"]',
    NULL, TRUE, NOW(), NOW()
);
