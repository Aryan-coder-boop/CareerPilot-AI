-- CareerPilot AI: Phase 2 Resume Storage & Skills Schema
-- PostgreSQL 16 + pgvector Migration

CREATE TABLE IF NOT EXISTS resumes (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    file_name VARCHAR(255) NOT NULL,
    file_path VARCHAR(500) NOT NULL,
    raw_text TEXT,
    parsed_data JSONB DEFAULT '{}'::jsonb,
    processing_status VARCHAR(30) NOT NULL DEFAULT 'PENDING',
    quality_score INT DEFAULT 0,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_resumes_user_id ON resumes(user_id);
CREATE INDEX IF NOT EXISTS idx_resumes_created_at ON resumes(created_at);

CREATE TABLE IF NOT EXISTS resume_skills (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    resume_id UUID NOT NULL REFERENCES resumes(id) ON DELETE CASCADE,
    skill_name VARCHAR(100) NOT NULL,
    category VARCHAR(50) DEFAULT 'TECHNICAL',
    proficiency VARCHAR(30) DEFAULT 'UNSPECIFIED'
);

CREATE INDEX IF NOT EXISTS idx_resume_skills_name ON resume_skills(skill_name);
CREATE INDEX IF NOT EXISTS idx_resume_skills_resume ON resume_skills(resume_id);

