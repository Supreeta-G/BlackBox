# REST API Reference — AI Agent Black Box

Base URL: `http://localhost:8080`

---

## 1. Agents API

### `GET /api/agents`
Retrieves all registered AI agents.
**Response (200 OK):**
```json
[
  {
    "id": "11111111-2222-3333-4444-555555555555",
    "name": "OrchestratorAgent",
    "role": "ORCHESTRATOR_AGENT",
    "modelIdentifier": "gpt-4o-mini",
    "configurationHash": "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855",
    "createdAt": "2026-10-11T08:27:30.000Z"
  }
]
```

### `GET /api/agents/{id}`
Retrieves a specific agent by UUID.

### `POST /api/agents`
Registers a new autonomous agent.
**Request Body:**
```json
{
  "name": "SecurityAuditAgent",
  "role": "SECURITY_AGENT",
  "modelIdentifier": "claude-3-5-sonnet"
}
```

---

## 2. Tasks API

### `GET /api/tasks`
Lists all tasks ordered by creation timestamp descending.

### `GET /api/tasks/{id}`
Retrieves a specific task.

### `POST /api/tasks`
Creates a task assigned to an agent.
**Request Body:**
```json
{
  "title": "Investigate Telemetry Discrepancy",
  "description": "Analyze black box logs for sensor anomaly at altitude 32,000 ft",
  "agentId": "11111111-2222-3333-4444-555555555555"
}
```

### `POST /api/tasks/{id}/execute`
Launches execution of the specified task through the Black Box recorder.
**Request Body (optional):**
```json
{
  "scenario": "STANDARD" // Options: "STANDARD", "FAILURE_RETRY", "POLICY_DENIED", "DELEGATION"
}
```

---

## 3. Executions API

### `GET /api/executions`
Retrieves all recorded flight executions.

### `GET /api/executions/{id}`
Retrieves execution summary, status, duration, and final hash seal.

### `GET /api/executions/{id}/timeline`
Retrieves all chronologically ordered events for the execution.

### `GET /api/executions/{id}/events`
Retrieves events with optional query parameter filters:
- `?eventType=TOOL_INVOCATION`
- `?status=SUCCESS`
- `?toolName=CalculatorTool`

### `GET /api/executions/{id}/children`
Retrieves all child executions spawned via multi-agent delegation.

### `GET /api/executions/{id}/verify-integrity`
Executes cryptographic SHA-256 chain verification.
**Response (200 OK — Verified):**
```json
{
  "status": "INTEGRITY_VERIFIED",
  "executionId": "exec-uuid",
  "totalEventsVerified": 8,
  "firstInvalidSequenceNumber": null,
  "firstInvalidEventId": null,
  "expectedHash": "65e3c8dc07a9...",
  "actualHash": "65e3c8dc07a9...",
  "reason": "All 8 events in sequence successfully verified against canonical SHA-256 hash chain.",
  "details": "Genesis block verified. All previous-hash pointers match.",
  "subsequentInvalidEvents": 0
}
```

### `GET /api/executions/{id}/evidence`
Aggregates timeline, tool calls, policy decisions, and verification status in a single forensic bundle.

### `GET /api/executions/compare?exec1={id1}&exec2={id2}`
Compares two executions side-by-side (duration, events, tool calls, policy decisions, integrity).

---

## 4. Single Event API

### `GET /api/events/{id}`
Retrieves full details of an individual event.

---

## 5. Development & Demo API

### `POST /api/dev/executions/{id}/tamper`
Development-only tampering fixture that alters an event in the database to demonstrate tamper detection.
**Request Body (optional):**
```json
{
  "sequenceNumber": 2,
  "modifiedDescription": "Malicious payload injection"
}
```

### `POST /api/demo/run-scenario`
Triggers an automated end-to-end demo scenario:
- `{"scenario": "STANDARD"}`
- `{"scenario": "FAILURE_RETRY"}`
- `{"scenario": "POLICY_DENIED"}`
- `{"scenario": "DELEGATION"}`

---

## 6. Statistics API

### `GET /api/statistics`
Returns system metrics: total executions, success/failure counts, total events, integrity violation count, average duration, and tool usage frequencies.
