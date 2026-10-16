# Full End-to-End Runtime Event Lifecycle

## 1. The 12 Stages of AI Agent Black Box

The Black Box implements the complete 12-stage lifecycle specified in the project blueprint:

```
+----+---------------------+-----------------------------------------------------------------+
| #  | Stage               | What Happens                                                    |
+----+---------------------+-----------------------------------------------------------------+
| 1  | User Request        | User creates and submits a task via the JavaFX Launchpad.       |
| 2  | Start Execution     | Black Box generates execution ID, correlation ID & TASK_STARTED.|
| 3  | Capture Context     | Snapshots agent model, config hash & sanitized task context.    |
| 4  | Action Boundary     | Records agent's planned action (tool selection, delegation).    |
| 5  | Policy Check        | Policy engine evaluates ALLOWED, DENIED, or REQUIRES_APPROVAL.  |
| 6  | Tool Invocation     | Records tool name, sanitized input parameters & dispatch time.  |
| 7  | Tool Result         | Captures return payload, latency, status & content hash.        |
| 8  | State Change        | Logs meaningful task or agent state transitions.                |
| 9  | Delegation          | Spawns child execution linked to parent execution ID.           |
| 10 | Retry & Errors      | Logs individual failure & retry attempts with backoff latency.  |
| 11 | Close Execution     | Seals flight record with final status, duration & final hash.   |
| 12 | Investigation       | Developer inspects chronological timeline, graph & SHA-256 chain|
+----+---------------------+-----------------------------------------------------------------+
```

---

## 2. Event Types & Transitions

### `TASK_STARTED`
Emitted immediately when an execution is initialized. Establishes the Genesis block (sequence #1) with previous hash `0000000000000000000000000000000000000000000000000000000000000000`.

### `CONTEXT_CAPTURED`
Captures model configuration identifier, model version, and initial configuration hash. Sensitive credentials (passwords, bearer tokens) are intercepted and redacted before event sealing.

### `ACTION_BOUNDARY`
Marks the observable boundary between agent thought and external effect. Demonstrates what the agent decided to execute next.

### `POLICY_EVALUATION`
Evaluates the action against deterministic policy rules:
- Prohibited system commands (`rm -rf`, `delete_database`) yield `DENIED`.
- Bulk exports yield `REQUIRES_APPROVAL`.
- Read-only tools yield `ALLOWED`.
*Critical Rule:* If an action is `DENIED`, the external tool is never called; an `ERROR_OCCURRED` event is recorded and execution transitions to halt.

### `TOOL_INVOCATION` & `TOOL_RESULT`
Records the tool name (`InformationSearchTool`, `CalculatorTool`), sanitized inputs, completion status, latency, and sanitized outputs.

### `DELEGATION_STARTED` & `DELEGATION_COMPLETED`
When an orchestrator agent delegates a subtask (e.g. `OrchestratorAgent` delegating synthesis to `SummaryAgent`), a child execution is created with `parentExecutionId` pointing to the parent. The child records its own chronological hash chain. Upon completion, the parent records `DELEGATION_COMPLETED`.

### `RETRY_ATTEMPT` & `ERROR_OCCURRED`
When an external tool or model call encounters a simulated glitch, each attempt is recorded as a separate event in sequence. Older attempts are never overwritten or discarded, preserving the complete trial provenance.

### `TASK_COMPLETED` / `TASK_FAILED`
The closing event of an execution. Emits total duration in milliseconds, final observable result, and seals the final cryptographic hash in the execution record.
