# SHA-256 Tamper-Evident Hash Chain Design

## 1. Cryptographic Concept

AI Agent Black Box implements a cryptographic hash chain scoped per execution. Every event record contains:
1. `sequence_number`: 1-indexed strictly consecutive integer.
2. `previous_event_hash`: The SHA-256 digest of the immediately preceding event.
3. `current_event_hash`: The SHA-256 digest of the current event's canonical representation.

```
+---------------------------+        +---------------------------+        +---------------------------+
| Event 1 (Genesis Block)   |        | Event 2 (Context Snapshot)|        | Event 3 (Tool Invocation) |
| Seq: 1                    |        | Seq: 2                    |        | Seq: 3                    |
| Prev: 000000000000... (0) |---+    | Prev: HASH_1              |---+    | Prev: HASH_2              |
| Payload: [Task Started]   |   +--->| Payload: [Config Captured]|   +--->| Payload: [Call Tool]      |
| Hash: HASH_1              |        | Hash: HASH_2              |        | Hash: HASH_3              |
+---------------------------+        +---------------------------+        +---------------------------+
```

---

## 2. Canonical Serialization Scheme

To prevent hash collisions and ambiguous concatenation attacks, fields are serialized deterministically with unambiguous tagged delimiters:

```java
canonicalString = 
  "[EXEC:" + executionId + "]" +
  "[SEQ:" + sequenceNumber + "]" +
  "[ID:" + eventId + "]" +
  "[TYPE:" + eventType + "]" +
  "[TS:" + timestampIso + "]" +
  "[DESC:" + description + "]" +
  "[TOOL:" + toolName + "]" +
  "[IN:" + sanitizedInput + "]" +
  "[OUT:" + sanitizedOutput + "]" +
  "[STAT:" + status + "]" +
  "[POL:" + policyResult + "]" +
  "[PREV:" + previousEventHash + "]";
```

### Determinism Rules:
1. **UTF-8 Character Encoding**: Strings are converted to bytes strictly using `StandardCharsets.UTF_8`.
2. **ISO-8601 Millisecond Truncation**: Timestamps are truncated to milliseconds (`ChronoUnit.MILLIS`) so that in-memory objects match database-persisted SQL timestamps exactly.
3. **Null-Safety**: Null string fields are normalized to `""`.
4. **Lowercase Hex Output**: SHA-256 digests are rendered as 64 lowercase hexadecimal characters.

---

## 3. Verification Algorithm

When a developer clicks **VERIFY INTEGRITY** in the JavaFX client:
1. All events for the execution are retrieved ordered by `sequence_number ASC`.
2. For each event `i`:
   - Verify `sequence_number == i + 1`. If not, flag sequence gap anomaly.
   - For `i == 0` (Genesis): verify `previous_event_hash == "0000...0000"` (64 zeros).
   - For `i > 0`: verify `event[i].previous_event_hash == event[i-1].current_event_hash`.
   - Reconstruct canonical string for `event[i]`.
   - Recompute `recalculatedHash = SHA-256(canonicalString)`.
   - Compare `recalculatedHash == event[i].current_event_hash`.
3. If all checks pass: Return `INTEGRITY_VERIFIED`.
4. If any check fails: Immediately report `INTEGRITY_COMPROMISED`, pinpointing:
   - First invalid sequence number.
   - First invalid event ID.
   - Expected hash vs actual stored hash.
   - Total downstream broken blocks invalidated by the tamper.

---

## 4. Threat Model & Security Boundaries

> [!NOTE]
> **What This Protects Against:**
> - Detects tampering by rogue queries or attackers modifying past events (e.g. changing input arguments to hide security violations).
> - Detects event deletion or event insertion (breaks sequential ordering and hash pointers).
> - Provides non-repudiation between agent outputs and database records.

> [!WARNING]
> **What This Does NOT Protect Against (Limitations):**
> - A plain hash chain without asymmetric digital signatures does not prevent a database superuser who rewrites the *entire* database from recalculating all hashes from scratch. In enterprise production, the root hash should be anchored in an append-only transparency log, external HSM, or blockchain ledger.
