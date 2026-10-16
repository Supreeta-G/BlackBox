package com.example.blackbox;

import com.example.blackbox.dto.IntegrityVerificationResult;
import com.example.blackbox.entity.AgentEventEntity;
import com.example.blackbox.enums.EventType;
import com.example.blackbox.enums.IntegrityStatus;
import com.example.blackbox.integrity.IntegrityService;
import com.example.blackbox.repository.AgentEventRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class IntegrityServiceTest {

    @Mock
    private AgentEventRepository eventRepository;

    private IntegrityService integrityService;

    @BeforeEach
    void setUp() {
        integrityService = new IntegrityService(eventRepository);
    }

    @Test
    @DisplayName("Should generate deterministic SHA-256 lowercase 64-char hex string")
    void testSha256Determinism() {
        String input = "AI-AGENT-BLACK-BOX-DATA";
        String hash1 = integrityService.calculateSha256(input);
        String hash2 = integrityService.calculateSha256(input);

        assertNotNull(hash1);
        assertEquals(64, hash1.length());
        assertEquals(hash1, hash2, "SHA-256 calculation must be deterministic");
        assertEquals(hash1, hash1.toLowerCase(), "Hash must be lowercase hex");
    }

    @Test
    @DisplayName("Canonical representation should prevent ambiguous concatenation")
    void testCanonicalizationNoCollision() {
        String canon1 = integrityService.buildCanonicalString("exec1", 1, "id1", "TYPE", "2026-10-11T00:00:00Z", "ab", "c", "", "", "OK", "", "0000");
        String canon2 = integrityService.buildCanonicalString("exec1", 1, "id1", "TYPE", "2026-10-11T00:00:00Z", "a", "bc", "", "", "OK", "", "0000");

        assertNotEquals(canon1, canon2, "Different fields must not produce same canonical string");
    }

    @Test
    @DisplayName("Valid event chain should verify successfully with INTEGRITY_VERIFIED")
    void testValidEventChainVerification() {
        String execId = "exec-test-100";
        Instant ts = Instant.parse("2026-10-11T10:00:00Z");

        // Event 1 (Genesis block)
        AgentEventEntity e1 = new AgentEventEntity();
        e1.setId("evt-1");
        e1.setExecutionId(execId);
        e1.setSequenceNumber(1);
        e1.setEventType(EventType.TASK_STARTED);
        e1.setTimestamp(ts);
        e1.setDescription("Task Started");
        e1.setStatus("SUCCESS");
        e1.setPreviousEventHash(IntegrityService.GENESIS_HASH);
        e1.setCurrentEventHash(integrityService.calculateEventHash(e1));

        // Event 2
        AgentEventEntity e2 = new AgentEventEntity();
        e2.setId("evt-2");
        e2.setExecutionId(execId);
        e2.setSequenceNumber(2);
        e2.setEventType(EventType.TOOL_INVOCATION);
        e2.setTimestamp(ts.plusSeconds(1));
        e2.setDescription("Tool Invoked");
        e2.setStatus("SUCCESS");
        e2.setPreviousEventHash(e1.getCurrentEventHash());
        e2.setCurrentEventHash(integrityService.calculateEventHash(e2));

        // Event 3
        AgentEventEntity e3 = new AgentEventEntity();
        e3.setId("evt-3");
        e3.setExecutionId(execId);
        e3.setSequenceNumber(3);
        e3.setEventType(EventType.TASK_COMPLETED);
        e3.setTimestamp(ts.plusSeconds(2));
        e3.setDescription("Task Completed");
        e3.setStatus("SUCCESS");
        e3.setPreviousEventHash(e2.getCurrentEventHash());
        e3.setCurrentEventHash(integrityService.calculateEventHash(e3));

        when(eventRepository.findByExecutionIdOrderBySequenceNumberAsc(execId))
                .thenReturn(List.of(e1, e2, e3));

        IntegrityVerificationResult result = integrityService.verifyExecutionIntegrity(execId);

        assertEquals(IntegrityStatus.INTEGRITY_VERIFIED, result.getStatus());
        assertEquals(3, result.getTotalEventsVerified());
        assertNull(result.getFirstInvalidSequenceNumber());
        assertEquals(0, result.getSubsequentInvalidEvents());
    }

    @Test
    @DisplayName("Tampered event content must cause verification failure with INTEGRITY_COMPROMISED")
    void testTamperedEventDetected() {
        String execId = "exec-tamper-200";
        Instant ts = Instant.parse("2026-10-11T10:00:00Z");

        AgentEventEntity e1 = new AgentEventEntity();
        e1.setId("evt-1");
        e1.setExecutionId(execId);
        e1.setSequenceNumber(1);
        e1.setEventType(EventType.TASK_STARTED);
        e1.setTimestamp(ts);
        e1.setDescription("Original Task 1");
        e1.setStatus("SUCCESS");
        e1.setPreviousEventHash(IntegrityService.GENESIS_HASH);
        e1.setCurrentEventHash(integrityService.calculateEventHash(e1));

        AgentEventEntity e2 = new AgentEventEntity();
        e2.setId("evt-2");
        e2.setExecutionId(execId);
        e2.setSequenceNumber(2);
        e2.setEventType(EventType.TOOL_INVOCATION);
        e2.setTimestamp(ts.plusSeconds(1));
        e2.setDescription("Original Task 2");
        e2.setStatus("SUCCESS");
        e2.setPreviousEventHash(e1.getCurrentEventHash());
        e2.setCurrentEventHash(integrityService.calculateEventHash(e2));

        AgentEventEntity e3 = new AgentEventEntity();
        e3.setId("evt-3");
        e3.setExecutionId(execId);
        e3.setSequenceNumber(3);
        e3.setEventType(EventType.TASK_COMPLETED);
        e3.setTimestamp(ts.plusSeconds(2));
        e3.setDescription("Original Task 3");
        e3.setStatus("SUCCESS");
        e3.setPreviousEventHash(e2.getCurrentEventHash());
        e3.setCurrentEventHash(integrityService.calculateEventHash(e3));

        // TAMPER WITH EVENT 2 (e.g. malicious actor modifies description)
        e2.setDescription("[MALICIOUS INJECTION] Altered instructions");

        when(eventRepository.findByExecutionIdOrderBySequenceNumberAsc(execId))
                .thenReturn(List.of(e1, e2, e3));

        IntegrityVerificationResult result = integrityService.verifyExecutionIntegrity(execId);

        assertEquals(IntegrityStatus.INTEGRITY_COMPROMISED, result.getStatus());
        assertEquals(2, result.getFirstInvalidSequenceNumber());
        assertEquals("evt-2", result.getFirstInvalidEventId());
        assertEquals(2, result.getSubsequentInvalidEvents(), "Downstream events 2 and 3 should be invalidated");
    }

    @Test
    @DisplayName("Broken sequence order must be detected as integrity violation")
    void testSequenceGapDetected() {
        String execId = "exec-gap-300";
        Instant ts = Instant.now();

        AgentEventEntity e1 = new AgentEventEntity();
        e1.setId("evt-1");
        e1.setExecutionId(execId);
        e1.setSequenceNumber(1);
        e1.setTimestamp(ts);
        e1.setPreviousEventHash(IntegrityService.GENESIS_HASH);
        e1.setCurrentEventHash(integrityService.calculateEventHash(e1));

        // Event with skipped sequence number 3 instead of 2
        AgentEventEntity e3 = new AgentEventEntity();
        e3.setId("evt-3");
        e3.setExecutionId(execId);
        e3.setSequenceNumber(3);
        e3.setTimestamp(ts);
        e3.setPreviousEventHash(e1.getCurrentEventHash());
        e3.setCurrentEventHash(integrityService.calculateEventHash(e3));

        when(eventRepository.findByExecutionIdOrderBySequenceNumberAsc(execId))
                .thenReturn(List.of(e1, e3));

        IntegrityVerificationResult result = integrityService.verifyExecutionIntegrity(execId);

        assertEquals(IntegrityStatus.INTEGRITY_COMPROMISED, result.getStatus());
        assertEquals(3, result.getFirstInvalidSequenceNumber());
    }
}
