# Database Schema — AI Agent Black Box

## 1. Schema Overview

The database uses PostgreSQL (with H2 in-memory mode for standalone testing) managed via Flyway database migration scripts (`V1__init_schema.sql`).

```
 +------------------+            +------------------+
 |      agents      |            |      tasks       |
 +------------------+            +------------------+
 | id (PK)          |<---+       | id (PK)          |
 | name             |    +-------| agent_id (FK)    |
 | role             |            | title            |
 | model_identifier |            | description      |
 | config_hash      |            | status           |
 | created_at       |            | created_at       |
 +------------------+            +------------------+
           ^                               ^
           |                               |
           +---------------+       +-------+
                           |       |
                     +--------------------+
                     |    executions      |
                     +--------------------+
                     | id (PK)            |
                     | task_id (FK)       |
                     | agent_id (FK)      |
                     | correlation_id     |
                     | root_execution_id  |
                     | parent_execution_id|---+ (self-reference for child delegation)
                     | status             |   |
                     | started_at         |   |
                     | completed_at       |   |
                     | duration_ms        |   |
                     | event_count        |   |
                     | final_result       |   |
                     | final_hash         |   |
                     | error_message      |   |
                     +--------------------+   |
                               ^              |
                               |              v
           +-------------------+--------------+
           |
     +--------------------+
     |    agent_events    |
     +--------------------+
     | id (PK)            |
     | execution_id (FK)  |
     | task_id            |
     | agent_id           |
     | parent_execution_id|
     | correlation_id     |
     | event_type         |
     | timestamp          |
     | sequence_number    | (UK: execution_id + sequence_number)
     | description        |
     | tool_name          |
     | sanitized_input    |
     | sanitized_output   |
     | status             |
     | duration_ms        |
     | error_details      |
     | policy_result      |
     | policy_reason      |
     | evidence_reference |
     | content_hash       |
     | previous_event_hash| (SHA-256 chain link)
     | current_event_hash | (SHA-256 block digest)
     | trace_id           | (OpenTelemetry W3C)
     | span_id            | (OpenTelemetry W3C)
     | is_tampered        |
     +--------------------+
           ^          ^
           |          |
  +--------+          +--------+
  |                            |
+--------------------+   +---------------------+
|    tool_calls      |   |  policy_decisions   |
+--------------------+   +---------------------+
| id (PK)            |   | id (PK)             |
| execution_id (FK)  |   | execution_id (FK)   |
| event_id (FK)      |   | event_id (FK)       |
| tool_name          |   | action_type         |
| sanitized_input    |   | target_resource     |
| sanitized_output   |   | decision            |
| status             |   | reason              |
| duration_ms        |   | evaluated_at        |
| started_at         |   +---------------------+
| completed_at       |
+--------------------+
```

---

## 2. Table Specifications

### `agents`
Stores registered autonomous agents and their immutable configuration digest.
- `id`: VARCHAR(36) UUID primary key.
- `name`: Human-readable identifier (e.g. `OrchestratorAgent`, `ResearchAgent`).
- `role`: Enum string (`RESEARCH_AGENT`, `SUMMARY_AGENT`, `ORCHESTRATOR_AGENT`, `SECURITY_AGENT`).
- `model_identifier`: Associated LLM engine (e.g. `gpt-4o-mini`, `claude-3-5-sonnet`).
- `configuration_hash`: SHA-256 digest of initial agent parameters.
- `created_at`: Timestamp.

### `tasks`
Stores tasks assigned to agents.
- `id`: VARCHAR(36) UUID primary key.
- `title`: Short task name.
- `description`: Full task instruction.
- `agent_id`: FK to `agents(id)`.
- `status`: Lifecycle state (`READY`, `RUNNING`, `COMPLETED`, `FAILED`).
- `created_at`: Timestamp.

### `executions`
Records individual execution runs ("flight missions").
- `id`: VARCHAR(36) UUID primary key.
- `task_id`: FK to `tasks(id)`.
- `agent_id`: FK to `agents(id)`.
- `correlation_id`: Trace correlation across distributed logs.
- `root_execution_id`: Top-level flight ID for multi-agent delegation trees.
- `parent_execution_id`: Nullable parent execution ID for child delegations.
- `status`: Enum (`RUNNING`, `COMPLETED`, `FAILED`).
- `duration_ms`: Total execution time in milliseconds.
- `event_count`: Number of recorded black box events.
- `final_hash`: Final SHA-256 seal of the execution's event chain.

### `agent_events`
The primary immutable flight recorder audit table.
- `id`: VARCHAR(36) UUID.
- `execution_id`: FK to `executions(id)`.
- `sequence_number`: 1-based monotonically increasing integer scoped to the execution.
- `previous_event_hash`: SHA-256 hash of event `sequence_number - 1` (or 64 zeros for sequence 1).
- `current_event_hash`: SHA-256 digest computed over the canonical representation of this event.
- `trace_id` / `span_id`: OpenTelemetry correlation identifiers.
- `is_tampered`: Flag used by development testing.
- Unique Constraint: `(execution_id, sequence_number)` ensures strictly consecutive ordering without gaps or duplicate sequences.

### `tool_calls`
Detailed metrics and evidence for each tool invocation.

### `policy_decisions`
Records deterministic policy evaluations (`ALLOWED`, `DENIED`, `REQUIRES_APPROVAL`).
