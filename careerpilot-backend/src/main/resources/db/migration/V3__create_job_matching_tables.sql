-- CareerPilot AI: Phase 3 Job Matching and Skill Gap Diagnostics Schema
-- PostgreSQL 16 Migration

CREATE TABLE IF NOT EXISTS job_descriptions (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    job_title VARCHAR(255) NOT NULL,
    company_name VARCHAR(255),
    raw_text TEXT NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_jobs_user_id ON job_descriptions(user_id);
CREATE INDEX IF NOT EXISTS idx_jobs_created_at ON job_descriptions(created_at);

CREATE TABLE IF NOT EXISTS job_match_reports (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    resume_id UUID NOT NULL REFERENCES resumes(id) ON DELETE CASCADE,
    job_id UUID NOT NULL REFERENCES job_descriptions(id) ON DELETE CASCADE,
    match_score INT NOT NULL,
    skills_score INT NOT NULL,
    experience_score INT NOT NULL,
    match_summary TEXT,
    matched_skills JSONB DEFAULT '[]'::jsonb,
    missing_skills JSONB DEFAULT '[]'::jsonb,
    recommendations JSONB DEFAULT '[]'::jsonb,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_job_match_user ON job_match_reports(user_id);
CREATE INDEX IF NOT EXISTS idx_job_match_resume ON job_match_reports(resume_id);
CREATE INDEX IF NOT EXISTS idx_job_match_job ON job_match_reports(job_id);
CREATE INDEX IF NOT EXISTS idx_job_match_created_at ON job_match_reports(created_at);

