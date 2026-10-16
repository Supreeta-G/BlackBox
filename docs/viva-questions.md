# Viva Preparation Guide — AI Agent Black Box

**25+ Comprehensive Examination & Viva Questions with Clear, Expert Answers**

---

### Q1: What is the primary purpose of the AI Agent Black Box?
**Answer:** The AI Agent Black Box is a flight recorder for autonomous AI agents. Just as an aircraft black box records flight telemetry and cockpit data to enable post-incident analysis without flying the plane, the AI Agent Black Box records observable agent actions, tool calls, policy evaluations, delegations, retries, and outputs so engineers can reconstruct and audit why an agent performed an action.

---

### Q2: What is the "Critical Boundary" in the project specification?
**Answer:** The Black Box does not claim to capture hidden, unverifiable model "chain-of-thought" or internal weights. It records observable events, inputs, outputs, external actions, and policy evaluations. Any textual explanation produced by an AI model is explicitly labelled as a *model-provided explanation*, not guaranteed physical proof of causality.

---

### Q3: Why is standard logging (e.g. Logback or console logs) insufficient for AI agents?
**Answer:** Standard logs are unstructured, unindexed, scattered across multiple microservices, and lack cryptographic integrity. They do not correlate parent-child multi-agent delegations, cannot detect post-facto log file tampering, and lack deterministic sequencing across distributed tool executions.

---

### Q4: Explain the 12 stages of the execution lifecycle implemented in the system.
**Answer:**
1. **User Request:** Captures task metadata and initial prompt.
2. **Start Execution:** Generates execution ID, correlation ID, and TASK_STARTED event (Genesis block).
3. **Capture Context:** Records agent configuration hash and environment snapshot.
4. **Action Boundary:** Marks the planned action before invocation.
5. **Policy Check:** Deterministic security evaluation (ALLOWED, DENIED, REQUIRES_APPROVAL).
6. **Tool Invocation:** Records sanitized tool parameters and dispatch time.
7. **Tool Result:** Captures tool output, latency, and status.
8. **State Change:** Logs workflow transitions.
9. **Delegation:** Spawns and links child executions to parent executions.
10. **Retry & Errors:** Records each failure and retry attempt individually with backoff.
11. **Close Execution:** Emits completion status, total duration, and seals the final SHA-256 hash.
12. **Investigation:** Developer forensics via timeline, graph, and integrity verification.

---

### Q5: How does the SHA-256 hash chain work in this project?
**Answer:** Each event in an execution contains a 1-based sequence number, a `previous_event_hash`, and a `current_event_hash`. For sequence #1 (Genesis block), the previous hash is explicitly set to 64 zero characters (`0000...0000`). For every subsequent event, `current_event_hash = SHA-256(canonicalString(event))`, where `previous_event_hash` points to the immediately preceding event's hash.

---

### Q6: What is deterministic canonicalization and why is it essential?
**Answer:** Canonicalization formats event fields into a standardized, unambiguous string before hashing. If fields were concatenated arbitrarily (e.g., `fieldA + fieldB`), `("ab", "c")` and `("a", "bc")` would produce the same string ("abc"), causing hash collision vulnerabilities. The system uses tagged delimiters: `[EXEC:..][SEQ:..][TYPE:..][PREV:..]` and truncates timestamps to millisecond precision to guarantee byte-for-byte reproducibility across database and JVM environments.

---

### Q7: What happens during integrity verification when an event is tampered with?
**Answer:** The system recalculates the canonical string and SHA-256 digest for each event in sequence order. If an attacker modifies even a single character in a recorded description, tool input, or status, the recalculated hash will not match the stored `current_event_hash`. Furthermore, all subsequent events in the chain will fail because their `previous_event_hash` pointers no longer match, reporting `INTEGRITY_COMPROMISED` and pinpointing the exact broken sequence number.

---

### Q8: What are the security limitations of a plain SHA-256 hash chain?
**Answer:** While a hash chain immediately reveals unauthorized modifications by standard users, SQL injectors, or rogue queries, it is not a digital signature. A database superuser with full administrative access could theoretically rewrite the entire table and recompute all hashes from block 1 to N. To guard against superusers, the final hash must be anchored in an append-only transparency log, external HSM, or digital signature authority.

---

### Q9: Why is Apache Kafka used in this architecture?
**Answer:** Kafka acts as an asynchronous, high-throughput event streaming backbone between the Agent Execution Service and database persistence. By partitioning the `ai-agent-events` topic using the `executionId` as the partition key, Kafka guarantees total in-order delivery of events for that specific execution across distributed worker nodes.

---

### Q10: How does the system handle Kafka duplicate event delivery?
**Answer:** Kafka provides at-least-once delivery by default. The Black Box implements idempotent persistence: the `KafkaEventConsumer` and `EventPersistenceService` check `eventRepository.existsById(message.getId())` before inserting. If an event ID has already been persisted, it is safely acknowledged and discarded without creating duplicates.

---

### Q11: How does the system handle environments where Kafka or Docker is unavailable?
**Answer:** The system implements a dual-mode `EventDispatcherService`. If Kafka is enabled and reachable, events are dispatched via `KafkaTemplate`. If Kafka is disabled or Docker is absent, the dispatcher routes events directly to `EventPersistenceService`. This allows the application and all unit tests to execute reliably anywhere with zero configuration friction.

---

### Q12: How does the Policy Engine operate, and what are its possible outcomes?
**Answer:** The Policy Engine is a deterministic rule evaluator that checks the action type and target resource:
- `ALLOWED`: Standard operations (e.g., math, read-only search).
- `DENIED`: Prohibited actions (e.g., `delete_database`, `rm -rf`, system command injection).
- `REQUIRES_APPROVAL`: High-risk operations (e.g., `export_bulk_data`).
If an action is DENIED, execution is halted immediately and the external tool is never invoked.

---

### Q13: How does the Black Box record tool failures and retries?
**Answer:** Unlike conventional systems that overwrite failed attempts with the final successful retry, Black Box logs each failure as an individual `ERROR_OCCURRED` event, records the subsequent retry as a `RETRY_ATTEMPT` event with backoff latency, and records the recovery as a separate `TOOL_RESULT` event. This preserves complete historical evidence of network or service instability.

---

### Q14: How does multi-agent delegation work in the Black Box?
**Answer:** When an orchestrator agent delegates a subtask to a specialized agent (e.g., `OrchestratorAgent` delegating synthesis to `SummaryAgent`), the parent logs a `DELEGATION_STARTED` event with a generated child execution ID. The child execution runs with `parentExecutionId` set to the parent's ID. Once the child finishes, the parent logs `DELEGATION_COMPLETED` containing the child's observable findings.

---

### Q15: How does the JavaFX UI visualize multi-agent delegations?
**Answer:** The **Execution Graph** screen uses a hierarchical `TreeView` control that renders the root execution at the top level and nests all delegated child executions beneath their respective parent nodes, displaying execution status, duration, and output provenance.

---

### Q16: How is sensitive data protected from being stored in the Black Box?
**Answer:** The `RedactionService` intercepts all raw inputs, outputs, and errors before event creation. It uses configurable regular expressions to detect passwords (`password=***`), API keys (`api-key=***`), secret tokens (`token=***`), and authorization headers (`Bearer ***`), replacing sensitive fragments with `[REDACTED]`.

---

### Q17: What role does OpenTelemetry play in this project?
**Answer:** OpenTelemetry generates distributed tracing spans for each phase of execution (lifecycle, tool calls, policy evaluations, Kafka dispatch). Standard W3C 32-hex `traceId` and 16-hex `spanId` values are injected into each `AgentEventEntity`, connecting application performance monitoring (APM) with the permanent audit record in PostgreSQL.

---

### Q18: What is the difference between OpenTelemetry traces and Black Box events?
**Answer:** OpenTelemetry traces are transient observability metrics typically sent to a short-retention collector (Jaeger/Zipkin/Prometheus) for latency profiling and debugging. Black Box events are permanent, cryptographically sealed forensic audit records stored in relational storage for regulatory compliance, security verification, and legal accountability.

---

### Q19: How is the application tested, and what guarantees do the tests provide?
**Answer:** The test suite uses JUnit 5, Mockito, and Spring Boot Test with 23 automated tests. The tests verify:
1. SHA-256 calculation determinism and canonical string collision prevention.
2. Complete 12-stage execution lifecycle and successful hash chain verification.
3. Tamper detection: modifying an event causes verification to fail and identifies the exact sequence.
4. Sequence gap detection: missing sequence numbers fail verification.
5. Idempotent deduplication: duplicate event IDs are not inserted twice.
6. Safe policy enforcement: denied actions halt execution without calling external tools.
7. Multi-agent delegation: parent and child executions are correctly linked.
8. Credential redaction: secrets are masked with `[REDACTED]`.

---

### Q20: What is Demo Mode and why is it important?
**Answer:** Demo mode (`DemoAiService`) allows the full application to run deterministically without requiring an external paid LLM API key (such as OpenAI or Anthropic). It simulates model tool selection, generates structured evidence, evaluates policies, runs retries, and spawns delegated child agents, clearly labelling output as `[DEMO SIMULATION]`.

---

### Q21: Why was JavaFX chosen over a web frontend?
**Answer:** JavaFX allows building a self-contained, high-performance desktop application entirely in modern Java with native OS windowing, hardware-accelerated rendering, strong typing, and direct HTTP/JSON communication with the Spring Boot backend without requiring Node.js, npm, JavaScript bundlers, or web browsers.

---

### Q22: How does the JavaFX client prevent UI freezing during long-running API operations?
**Answer:** All HTTP requests in `ApiClient` use Java's asynchronous `java.net.http.HttpClient` returning `CompletableFuture`. Network I/O runs on background worker threads, and UI updates are dispatched back to the JavaFX Application Thread using `Platform.runLater()`.

---

### Q23: What database migrations framework is used and why?
**Answer:** Flyway (`V1__init_schema.sql`) is used for database version control. It executes SQL migrations automatically on application startup, guaranteeing consistent schema creation across development, testing, and Docker Compose environments.

---

### Q24: What are the primary tables in the database schema?
**Answer:**
1. `agents`: Agent metadata, role, model, and configuration hash.
2. `tasks`: User tasks assigned to agents.
3. `executions`: Individual execution runs, status, duration, and final hash seal.
4. `agent_events`: Chronological audit events with previous/current SHA-256 hashes.
5. `tool_calls`: Tool invocation parameters, status, and execution latency.
6. `policy_decisions`: Recorded authorization results (ALLOWED, DENIED, REQUIRES_APPROVAL).

---

### Q25: How does the Execution Comparison feature assist developers?
**Answer:** The **Execution Comparison** view allows developers to select any two executions and compare them side-by-side. It highlights duration differences, event count discrepancies, tool usage variances, errors encountered, policy decisions, and cryptographic integrity status to quickly identify regressions or anomalies between agent runs.

---

### Q26: How can an examiner test the tampering feature during a live demonstration?
**Answer:**
1. Run any execution or click **Run Standard Demo Agent**.
2. Navigate to **Integrity Verification** and click **VERIFY INTEGRITY** -> Observer green banner: `INTEGRITY_VERIFIED`.
3. Click **Tamper Event (Dev Test)** -> Modifies event #2 in the database without updating the hash chain.
4. Click **VERIFY INTEGRITY** again -> Instantly shows red banner: `INTEGRITY_COMPROMISED`, highlighting Sequence #2 as the invalid block, comparing the expected hash with the actual hash, and displaying the downstream broken blocks!
