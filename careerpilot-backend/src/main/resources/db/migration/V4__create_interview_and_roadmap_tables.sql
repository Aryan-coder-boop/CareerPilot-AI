-- CareerPilot AI: Phase 4 & Phase 5 Interview and Roadmap Schema
-- PostgreSQL 16 Migration

-- 1. Interview Sessions
CREATE TABLE IF NOT EXISTS interview_sessions (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    job_id UUID REFERENCES job_descriptions(id) ON DELETE SET NULL,
    target_role VARCHAR(150) NOT NULL,
    difficulty VARCHAR(30) NOT NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'IN_PROGRESS',
    total_questions INT NOT NULL DEFAULT 5,
    overall_score INT DEFAULT NULL,
    overall_feedback TEXT DEFAULT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    completed_at TIMESTAMP WITH TIME ZONE
);
CREATE INDEX IF NOT EXISTS idx_interview_sessions_user ON interview_sessions(user_id);
CREATE INDEX IF NOT EXISTS idx_interview_sessions_created ON interview_sessions(created_at);

-- 2. Interview Exchanges (Questions and Answers)
CREATE TABLE IF NOT EXISTS interview_exchanges (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    session_id UUID NOT NULL REFERENCES interview_sessions(id) ON DELETE CASCADE,
    question_index INT NOT NULL,
    question_text TEXT NOT NULL,
    expected_concepts JSONB DEFAULT '[]'::jsonb,
    user_answer TEXT,
    score INT,
    feedback TEXT,
    model_answer TEXT,
    technical_accuracy INT,
    clarity_score INT,
    answered_at TIMESTAMP WITH TIME ZONE
);
CREATE INDEX IF NOT EXISTS idx_interview_exchanges_session ON interview_exchanges(session_id);

-- 3. Learning Roadmaps
CREATE TABLE IF NOT EXISTS learning_roadmaps (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    job_id UUID REFERENCES job_descriptions(id) ON DELETE SET NULL,
    target_role VARCHAR(150) NOT NULL,
    estimated_weeks INT NOT NULL,
    status VARCHAR(30) DEFAULT 'ACTIVE',
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX IF NOT EXISTS idx_roadmaps_user ON learning_roadmaps(user_id);
CREATE INDEX IF NOT EXISTS idx_roadmaps_created ON learning_roadmaps(created_at);

-- 4. Roadmap Milestones
CREATE TABLE IF NOT EXISTS roadmap_milestones (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    roadmap_id UUID NOT NULL REFERENCES learning_roadmaps(id) ON DELETE CASCADE,
    week_number INT NOT NULL,
    title VARCHAR(200) NOT NULL,
    description TEXT NOT NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'PENDING',
    order_index INT NOT NULL
);
CREATE INDEX IF NOT EXISTS idx_milestones_roadmap ON roadmap_milestones(roadmap_id);

-- 5. Roadmap Resources
CREATE TABLE IF NOT EXISTS roadmap_resources (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    milestone_id UUID NOT NULL REFERENCES roadmap_milestones(id) ON DELETE CASCADE,
    title VARCHAR(255) NOT NULL,
    url VARCHAR(1000) NOT NULL,
    resource_type VARCHAR(50) NOT NULL
);
CREATE INDEX IF NOT EXISTS idx_resources_milestone ON roadmap_resources(milestone_id);

