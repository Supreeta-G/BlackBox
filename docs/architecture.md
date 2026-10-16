# System Architecture — AI Agent Black Box

## 1. Architectural Overview

**AI Agent Black Box** is an observable flight recorder and cryptographic audit verification system for autonomous software agents. It bridges the critical provenance gap in AI agent operations by providing immutable, chronologically sequenced, tamper-evident records of agent inputs, policy checks, tool invocations, retries, multi-agent delegations, and observable outcomes.

```
+-----------------------------------------------------------------------------------+
|                           JavaFX Desktop Client (UI)                             |
|  [Dashboard] [Timeline] [Graph] [Evidence Inspector] [Integrity] [Comparison]     |
+------------------------------------------+----------------------------------------+
                                           | HTTP / JSON REST APIs
                                           v
+-----------------------------------------------------------------------------------+
|                        Spring Boot 3.3 Backend Services                           |
|                                                                                   |
|  +--------------------+  +----------------------+  +---------------------------+  |
|  | Agent Execution    |  | Execution Recorder   |  | SHA-256 Integrity         |  |
|  | Engine             |  | (Canonical Sequencer)|  | Service (Hash Chain)      |  |
|  +--------------------+  +----------------------+  +---------------------------+  |
|  +--------------------+  +----------------------+  +---------------------------+  |
|  | Policy Evaluation  |  | Tool Registry        |  | OpenTelemetry Tracing     |  |
|  | Engine (POL-Rules) |  | (Search, Calculator) |  | (TraceId, SpanId)         |  |
|  +--------------------+  +----------------------+  +---------------------------+  |
|  +--------------------+  +----------------------+  +---------------------------+  |
|  | Redaction Service  |  | Spring AI Abstraction|  | Event Dispatcher          |  |
|  | (Regex Sanitizer)  |  | (Demo AI Simulator)  |  | (Kafka / Direct Dual-Mode)|  |
|  +--------------------+  +----------------------+  +---------------------------+  |
+------------------------------------------+----------------------------------------+
                                           | Event Publishing (JSON)
                                           v
+-----------------------------------------------------------------------------------+
|                   Apache Kafka Message Broker (Topic: ai-agent-events)            |
|       - Key-based partitioning (by executionId for total in-order delivery)      |
+------------------------------------------+----------------------------------------+
                                           | Consumer Group: blackbox-event-group
                                           v
+-----------------------------------------------------------------------------------+
|                        Kafka Consumer & Deduplication Engine                      |
|       - Idempotent check: existsById(eventId) prevents duplicate recording        |
+------------------------------------------+----------------------------------------+
                                           | SQL / JPA
                                           v
+-----------------------------------------------------------------------------------+
|                      PostgreSQL 16 Relational Storage                             |
|  Tables: agents | tasks | executions | agent_events | tool_calls | policy_decisions |
|  Managed by: Flyway Schema Migrations                                              |
+-----------------------------------------------------------------------------------+
```

---

## 2. Component Breakdown

### A. JavaFX Desktop Client
The presentation layer is built completely in Java using JavaFX 21, FXML, and a custom CSS developer theme (`dark-theme.css`). It communicates with the backend exclusively through HTTP REST APIs using Java's asynchronous `java.net.http.HttpClient` and Jackson JSON serialization. It never connects directly to PostgreSQL or Kafka, maintaining strict boundary decoupling.

### B. Agent Execution Service
Orchestrates the 12-stage execution flow. Coordinates plan formulation, policy evaluation, tool invocation, retries, and multi-agent child delegations.

### C. Execution Recorder
Intercepts runtime events, calculates sequence numbers, sanitizes sensitive data via `RedactionService`, generates content hashes, links previous-event hashes, and seals the block with a SHA-256 digest.

### D. SHA-256 Integrity Service
Provides cryptographic canonicalization and verification. Enforces the Genesis Block rule (64 zeros) and verifies that every event's stored hash equals `SHA-256(canonicalString(event))` and that `event[i].previousEventHash == event[i-1].currentEventHash`.

### E. Policy Evaluation Engine
Deterministic security boundary that evaluates action types and target resources before execution:
- `ALLOWED`: Standard operations (safe search queries, arithmetic calculations).
- `DENIED`: Prohibited destructive operations (`delete_database`, `rm -rf`, system command injection).
- `REQUIRES_APPROVAL`: Sensitive operations (bulk export, permission escalation).

### F. Dual-Mode Event Dispatcher & Apache Kafka
In Docker/production environments, events are published to Kafka topic `ai-agent-events` partitioned by `executionId` for guaranteed sequential delivery per execution. The consumer verifies idempotency (`existsById`) before writing to PostgreSQL. In standalone local mode without Docker, the dispatcher routes directly through `EventPersistenceService` to ensure 100% test reliability and zero-friction execution.

### G. OpenTelemetry Observability
Instruments spans for execution lifecycles, tool calls, policy evaluations, and hash verification, generating standard W3C 32-hex `traceId` and 16-hex `spanId` values injected into each audit event.
