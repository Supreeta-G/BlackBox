-- ==========================================================
-- AI Agent Black Box - Initial Database Schema Migration
-- ==========================================================

-- 1. Agents table
CREATE TABLE IF NOT EXISTS agents (
    id VARCHAR(36) PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    role VARCHAR(50) NOT NULL,
    model_identifier VARCHAR(100) NOT NULL,
    configuration_hash VARCHAR(64) NOT NULL,
    created_at TIMESTAMP NOT NULL
);

-- 2. Tasks table
CREATE TABLE IF NOT EXISTS tasks (
    id VARCHAR(36) PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    description TEXT NOT NULL,
    agent_id VARCHAR(36) NOT NULL REFERENCES agents(id),
    status VARCHAR(50) NOT NULL,
    created_at TIMESTAMP NOT NULL
);

-- 3. Executions table
CREATE TABLE IF NOT EXISTS executions (
    id VARCHAR(36) PRIMARY KEY,
    task_id VARCHAR(36) NOT NULL REFERENCES tasks(id),
    agent_id VARCHAR(36) NOT NULL REFERENCES agents(id),
    correlation_id VARCHAR(64) NOT NULL,
    root_execution_id VARCHAR(36) NOT NULL,
    parent_execution_id VARCHAR(36),
    status VARCHAR(50) NOT NULL,
    started_at TIMESTAMP NOT NULL,
    completed_at TIMESTAMP,
    duration_ms BIGINT,
    event_count INTEGER DEFAULT 0,
    final_result TEXT,
    final_hash VARCHAR(64),
    error_message TEXT,
    created_at TIMESTAMP NOT NULL
);

-- 4. Agent Events table
CREATE TABLE IF NOT EXISTS agent_events (
    id VARCHAR(36) PRIMARY KEY,
    execution_id VARCHAR(36) NOT NULL REFERENCES executions(id),
    task_id VARCHAR(36) NOT NULL,
    agent_id VARCHAR(36) NOT NULL,
    parent_execution_id VARCHAR(36),
    correlation_id VARCHAR(64) NOT NULL,
    event_type VARCHAR(50) NOT NULL,
    timestamp TIMESTAMP NOT NULL,
    sequence_number INTEGER NOT NULL,
    description TEXT NOT NULL,
    tool_name VARCHAR(100),
    sanitized_input TEXT,
    sanitized_output TEXT,
    status VARCHAR(50) NOT NULL,
    duration_ms BIGINT,
    error_details TEXT,
    policy_result VARCHAR(50),
    policy_reason TEXT,
    evidence_reference VARCHAR(255),
    content_hash VARCHAR(64),
    previous_event_hash VARCHAR(64) NOT NULL,
    current_event_hash VARCHAR(64) NOT NULL,
    trace_id VARCHAR(64),
    span_id VARCHAR(64),
    is_tampered BOOLEAN DEFAULT FALSE,
    CONSTRAINT uk_exec_seq UNIQUE (execution_id, sequence_number)
);

-- 5. Tool Calls table
CREATE TABLE IF NOT EXISTS tool_calls (
    id VARCHAR(36) PRIMARY KEY,
    execution_id VARCHAR(36) NOT NULL REFERENCES executions(id),
    event_id VARCHAR(36) NOT NULL REFERENCES agent_events(id),
    tool_name VARCHAR(100) NOT NULL,
    sanitized_input TEXT,
    sanitized_output TEXT,
    status VARCHAR(50) NOT NULL,
    duration_ms BIGINT,
    started_at TIMESTAMP NOT NULL,
    completed_at TIMESTAMP
);

-- 6. Policy Decisions table
CREATE TABLE IF NOT EXISTS policy_decisions (
    id VARCHAR(36) PRIMARY KEY,
    execution_id VARCHAR(36) NOT NULL REFERENCES executions(id),
    event_id VARCHAR(36) NOT NULL REFERENCES agent_events(id),
    action_type VARCHAR(100) NOT NULL,
    target_resource VARCHAR(255),
    decision VARCHAR(50) NOT NULL,
    reason TEXT NOT NULL,
    evaluated_at TIMESTAMP NOT NULL
);

-- Indexes for high-performance timeline queries, child lookups, and sequence ordering
CREATE INDEX IF NOT EXISTS idx_executions_task ON executions(task_id);
CREATE INDEX IF NOT EXISTS idx_executions_parent ON executions(parent_execution_id);
CREATE INDEX IF NOT EXISTS idx_executions_root ON executions(root_execution_id);
CREATE INDEX IF NOT EXISTS idx_events_execution_seq ON agent_events(execution_id, sequence_number);
CREATE INDEX IF NOT EXISTS idx_events_type ON agent_events(event_type);
CREATE INDEX IF NOT EXISTS idx_events_timestamp ON agent_events(timestamp);
CREATE INDEX IF NOT EXISTS idx_tool_calls_exec ON tool_calls(execution_id);
CREATE INDEX IF NOT EXISTS idx_policy_decisions_exec ON policy_decisions(execution_id);
