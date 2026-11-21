-- V1: Core Tables for Hostel Complaint Management System

-- 1. Users Table
CREATE TABLE IF NOT EXISTS users (
    id BIGSERIAL PRIMARY KEY,
    email VARCHAR(150) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    full_name VARCHAR(100) NOT NULL,
    phone_number VARCHAR(20),
    role VARCHAR(30) NOT NULL, -- 'STUDENT', 'STAFF', 'ADMIN'
    hostel_block VARCHAR(50),  -- e.g. 'Block A', 'Block B'
    room_number VARCHAR(20),   -- e.g. '304'
    trade_specialty VARCHAR(50), -- e.g. 'ELECTRICIAN', 'PLUMBER', 'CARPENTER', 'NETWORK'
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Index for authentication and role lookup
CREATE INDEX IF NOT EXISTS idx_users_email ON users(email);
CREATE INDEX IF NOT EXISTS idx_users_role ON users(role);

-- 2. Complaints Table
CREATE TABLE IF NOT EXISTS complaints (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(200) NOT NULL,
    description TEXT NOT NULL,
    category VARCHAR(50) NOT NULL, -- 'ELECTRICAL', 'PLUMBING', 'CARPENTRY', 'CIVIL', 'NETWORK', 'OTHER'
    hostel_block VARCHAR(50) NOT NULL,
    room_or_area VARCHAR(100) NOT NULL,
    priority VARCHAR(20) NOT NULL DEFAULT 'MEDIUM', -- 'LOW', 'MEDIUM', 'HIGH', 'URGENT'
    status VARCHAR(30) NOT NULL DEFAULT 'OPEN', -- 'OPEN', 'ASSIGNED', 'IN_PROGRESS', 'RESOLVED', 'CLOSED', 'CANCELLED', 'REOPENED'
    photo_url VARCHAR(500),
    resolution_notes TEXT,
    resolution_photo_url VARCHAR(500),
    created_by_id BIGINT NOT NULL REFERENCES users(id),
    assigned_to_id BIGINT REFERENCES users(id),
    upvote_count INT NOT NULL DEFAULT 0,
    version BIGINT NOT NULL DEFAULT 0, -- Optimistic Locking version
    last_status_change_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    resolved_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Performance Indexes for Dashboard Queries
CREATE INDEX IF NOT EXISTS idx_complaints_block_status_priority ON complaints(hostel_block, status, priority);
CREATE INDEX IF NOT EXISTS idx_complaints_assigned_staff ON complaints(assigned_to_id, status);
CREATE INDEX IF NOT EXISTS idx_complaints_created_by ON complaints(created_by_id);
CREATE INDEX IF NOT EXISTS idx_complaints_escalation ON complaints(status, last_status_change_at);

-- 3. Upvotes Table (Unique constraint enforces 1 upvote per student per complaint)
CREATE TABLE IF NOT EXISTS complaint_upvotes (
    id BIGSERIAL PRIMARY KEY,
    complaint_id BIGINT NOT NULL REFERENCES complaints(id) ON DELETE CASCADE,
    student_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_complaint_student_upvote UNIQUE (complaint_id, student_id)
);

CREATE INDEX IF NOT EXISTS idx_upvotes_complaint_id ON complaint_upvotes(complaint_id);

-- 4. Audit Trail Table
CREATE TABLE IF NOT EXISTS complaint_audit_logs (
    id BIGSERIAL PRIMARY KEY,
    complaint_id BIGINT NOT NULL REFERENCES complaints(id) ON DELETE CASCADE,
    changed_by_user_id BIGINT REFERENCES users(id),
    from_status VARCHAR(30),
    to_status VARCHAR(30) NOT NULL,
    action VARCHAR(50) NOT NULL, -- 'CREATED', 'ASSIGNED', 'STATUS_CHANGE', 'ESCALATED', 'REOPENED', 'CANCELLED'
    comment TEXT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_audit_complaint ON complaint_audit_logs(complaint_id);
CREATE INDEX IF NOT EXISTS idx_audit_timestamp ON complaint_audit_logs(created_at);
