package com.example.blackbox.integrity;

import com.example.blackbox.dto.IntegrityVerificationResult;
import com.example.blackbox.entity.AgentEventEntity;
import com.example.blackbox.repository.AgentEventRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
public class IntegrityService {

    private static final Logger log = LoggerFactory.getLogger(IntegrityService.class);
    public static final String GENESIS_HASH = "0000000000000000000000000000000000000000000000000000000000000000";

    private final AgentEventRepository eventRepository;

    public IntegrityService(AgentEventRepository eventRepository) {
        this.eventRepository = eventRepository;
    }

    /**
     * Compute SHA-256 hash using Java standard security APIs.
     */
    public String calculateSha256(String data) {
        if (data == null) {
            return GENESIS_HASH;
        }
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(data.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(64);
            for (byte b : hashBytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not available in Java runtime", e);
        }
    }

    /**
     * Generates a strictly unambiguous, deterministic canonical representation of the event fields.
     */
    public String buildCanonicalString(String executionId,
                                       int sequenceNumber,
                                       String eventId,
                                       String eventType,
                                       String timestampIso,
                                       String description,
                                       String toolName,
                                       String sanitizedInput,
                                       String sanitizedOutput,
                                       String status,
                                       String policyResult,
                                       String previousEventHash) {
        return "[EXEC:" + safeString(executionId) + "]" +
               "[SEQ:" + sequenceNumber + "]" +
               "[ID:" + safeString(eventId) + "]" +
               "[TYPE:" + safeString(eventType) + "]" +
               "[TS:" + safeString(timestampIso) + "]" +
               "[DESC:" + safeString(description) + "]" +
               "[TOOL:" + safeString(toolName) + "]" +
               "[IN:" + safeString(sanitizedInput) + "]" +
               "[OUT:" + safeString(sanitizedOutput) + "]" +
               "[STAT:" + safeString(status) + "]" +
               "[POL:" + safeString(policyResult) + "]" +
               "[PREV:" + safeString(previousEventHash) + "]";
    }

    public String canonicalizeEvent(AgentEventEntity event) {
        String tsString = "";
        if (event.getTimestamp() != null) {
            tsString = event.getTimestamp().truncatedTo(ChronoUnit.MILLIS).toString();
        }
        return buildCanonicalString(
                event.getExecutionId(),
                event.getSequenceNumber(),
                event.getId(),
                event.getEventType() != null ? event.getEventType().name() : "",
                tsString,
                event.getDescription(),
                event.getToolName(),
                event.getSanitizedInput(),
                event.getSanitizedOutput(),
                event.getStatus(),
                event.getPolicyResult() != null ? event.getPolicyResult().name() : "",
                event.getPreviousEventHash()
        );
    }

    public String calculateEventHash(AgentEventEntity event) {
        String canonical = canonicalizeEvent(event);
        return calculateSha256(canonical);
    }

    /**
     * Verifies the cryptographic event chain for an entire execution.
     */
    public IntegrityVerificationResult verifyExecutionIntegrity(String executionId) {
        List<AgentEventEntity> events = eventRepository.findByExecutionIdOrderBySequenceNumberAsc(executionId);

        if (events.isEmpty()) {
            return IntegrityVerificationResult.verified(executionId, 0, GENESIS_HASH);
        }

        String priorHash = GENESIS_HASH;
        int expectedSeq = 1;

        for (int i = 0; i < events.size(); i++) {
            AgentEventEntity event = events.get(i);
            int currentSeq = event.getSequenceNumber();

            // 1. Validate sequence ordering
            if (currentSeq != expectedSeq) {
                int subsequentBroken = events.size() - i;
                log.warn("Sequence break detected in execution {} at index {}, expected seq {} but found {}",
                        executionId, i, expectedSeq, currentSeq);
                return IntegrityVerificationResult.compromised(
                        executionId,
                        events.size(),
                        currentSeq,
                        event.getId(),
                        "Sequence #" + expectedSeq,
                        "Sequence #" + currentSeq,
                        "Broken sequence order in event chain. Sequence jumps or gaps detected.",
                        subsequentBroken
                );
            }

            // 2. Validate previous event hash pointer
            if (!priorHash.equalsIgnoreCase(event.getPreviousEventHash())) {
                int subsequentBroken = events.size() - i;
                log.warn("Previous hash mismatch in execution {} at seq {}. Stored prev: {}, Prior actual current: {}",
                        executionId, currentSeq, event.getPreviousEventHash(), priorHash);
                return IntegrityVerificationResult.compromised(
                        executionId,
                        events.size(),
                        currentSeq,
                        event.getId(),
                        priorHash,
                        event.getPreviousEventHash(),
                        "Previous hash link mismatch. Chain pointer broken between sequence #" + (currentSeq - 1) + " and #" + currentSeq + ".",
                        subsequentBroken
                );
            }

            // 3. Recalculate event hash and compare with stored hash
            String canonical = canonicalizeEvent(event);
            String recalculatedHash = calculateSha256(canonical);

            if (!recalculatedHash.equalsIgnoreCase(event.getCurrentEventHash())) {
                int subsequentBroken = events.size() - i;
                log.warn("Payload tamper detected in execution {} at seq {}. Recalculated: {}, Stored: {}",
                        executionId, currentSeq, recalculatedHash, event.getCurrentEventHash());
                return IntegrityVerificationResult.compromised(
                        executionId,
                        events.size(),
                        currentSeq,
                        event.getId(),
                        recalculatedHash,
                        event.getCurrentEventHash(),
                        "Cryptographic hash mismatch at sequence #" + currentSeq + ". Content was tampered with or modified after generation.",
                        subsequentBroken
                );
            }

            priorHash = event.getCurrentEventHash();
            expectedSeq++;
        }

        AgentEventEntity lastEvent = events.get(events.size() - 1);
        return IntegrityVerificationResult.verified(executionId, events.size(), lastEvent.getCurrentEventHash());
    }

    private String safeString(String str) {
        return str == null ? "" : str;
    }
}
