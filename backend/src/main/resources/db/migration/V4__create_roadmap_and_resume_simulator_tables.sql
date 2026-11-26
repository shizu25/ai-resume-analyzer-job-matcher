-- Migration: V4__create_roadmap_and_resume_simulator_tables.sql
-- Description: Create skill_roadmaps, roadmap_items, resume_versions, resume_improvement_suggestions, and resume_simulations tables

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

CREATE TABLE roadmap_items (
    id UUID PRIMARY KEY,
    roadmap_id UUID NOT NULL REFERENCES skill_roadmaps(id) ON DELETE CASCADE,
    skill_name VARCHAR(100) NOT NULL,
    status VARCHAR(50) NOT NULL,
    priority VARCHAR(20) NOT NULL,
    priority_reason TEXT NOT NULL,
    why_required TEXT NOT NULL,
    learning_objectives TEXT NOT NULL,
    learning_sequence TEXT NOT NULL,
    practical_tasks TEXT NOT NULL,
    suggested_project TEXT NOT NULL,
    estimated_duration VARCHAR(100) NOT NULL,
    completion_criteria TEXT NOT NULL,
    progress_status VARCHAR(30) NOT NULL DEFAULT 'NOT_STARTED',
    completed_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT chk_roadmap_items_status CHECK (status IN ('DEMONSTRATED', 'MENTIONED', 'MISSING', 'RELATED')),
    CONSTRAINT chk_roadmap_items_priority CHECK (priority IN ('HIGH', 'MEDIUM', 'LOW')),
    CONSTRAINT chk_roadmap_items_progress CHECK (progress_status IN ('NOT_STARTED', 'IN_PROGRESS', 'COMPLETED'))
);

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
    CONSTRAINT uq_resume_versions_parent_number UNIQUE (parent_resume_id, version_number),
    CONSTRAINT chk_resume_versions_number CHECK (version_number >= 1)
);

CREATE TABLE resume_improvement_suggestions (
    id UUID PRIMARY KEY,
    version_id UUID NOT NULL REFERENCES resume_versions(id) ON DELETE CASCADE,
    job_id UUID REFERENCES jobs(id) ON DELETE CASCADE,
    category VARCHAR(100) NOT NULL,
    original_text TEXT NOT NULL,
    suggested_text TEXT NOT NULL,
    issue TEXT NOT NULL,
    reason TEXT NOT NULL,
    related_requirement VARCHAR(255),
    expected_benefit TEXT NOT NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'PENDING',
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT chk_resume_suggestions_category CHECK (category IN (
        'MISSING_RELEVANT_INFO', 'WEAK_PROJECT_DESCRIPTION', 'UNCLEAR_CONTRIBUTION',
        'MISSING_SKILL_EVIDENCE', 'POORLY_STRUCTURED', 'IRRELEVANT_INFO', 'GRAMMAR_READABILITY'
    )),
    CONSTRAINT chk_resume_suggestions_status CHECK (status IN ('PENDING', 'ACCEPTED', 'REJECTED'))
);

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
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT chk_sim_base_score CHECK (base_score >= 0.0 AND base_score <= 100.0),
    CONSTRAINT chk_sim_improved_score CHECK (improved_score >= 0.0 AND improved_score <= 100.0)
);

-- Indexes for performance
CREATE INDEX idx_skill_roadmaps_user_id ON skill_roadmaps(user_id);
CREATE INDEX idx_skill_roadmaps_job_id ON skill_roadmaps(job_id);
CREATE INDEX idx_skill_roadmaps_resume_id ON skill_roadmaps(resume_id);

CREATE INDEX idx_roadmap_items_roadmap_id ON roadmap_items(roadmap_id);
CREATE INDEX idx_roadmap_items_priority ON roadmap_items(priority);
CREATE INDEX idx_roadmap_items_progress ON roadmap_items(progress_status);

CREATE INDEX idx_resume_versions_user_id ON resume_versions(user_id);
CREATE INDEX idx_resume_versions_parent ON resume_versions(parent_resume_id);
CREATE INDEX idx_resume_versions_target_job ON resume_versions(target_job_id);

CREATE INDEX idx_resume_suggestions_version_id ON resume_improvement_suggestions(version_id);
CREATE INDEX idx_resume_suggestions_job_id ON resume_improvement_suggestions(job_id);
CREATE INDEX idx_resume_suggestions_status ON resume_improvement_suggestions(status);

CREATE INDEX idx_resume_simulations_user_id ON resume_simulations(user_id);
CREATE INDEX idx_resume_simulations_job_id ON resume_simulations(job_id);
CREATE INDEX idx_resume_simulations_base ON resume_simulations(base_version_id);
CREATE INDEX idx_resume_simulations_improved ON resume_simulations(improved_version_id);
