package com.example.blackbox;

import com.example.blackbox.dto.IntegrityVerificationResult;
import com.example.blackbox.entity.AgentEntity;
import com.example.blackbox.entity.ExecutionEntity;
import com.example.blackbox.entity.TaskEntity;
import com.example.blackbox.enums.AgentRole;
import com.example.blackbox.enums.ExecutionStatus;
import com.example.blackbox.enums.IntegrityStatus;
import com.example.blackbox.execution.AgentExecutionService;
import com.example.blackbox.repository.AgentRepository;
import com.example.blackbox.repository.ExecutionRepository;
import com.example.blackbox.repository.TaskRepository;
import com.example.blackbox.service.ExecutionService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class AgentExecutionServiceTest {

    @Autowired
    private AgentExecutionService agentExecutionService;

    @Autowired
    private ExecutionService executionService;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private AgentRepository agentRepository;

    @Autowired
    private ExecutionRepository executionRepository;

    @Test
    @DisplayName("Standard execution completes all lifecycle stages and verifies hash chain")
    void testStandardExecutionLifecycle() {
        AgentEntity agent = agentRepository.findAll().get(0);
        TaskEntity task = new TaskEntity(UUID.randomUUID().toString(), "Lifecycle Test Task", "Verify 12 stages", agent.getId(), "READY", Instant.now());
        taskRepository.save(task);

        ExecutionEntity exec = agentExecutionService.runExecution(task.getId(), "STANDARD");

        assertNotNull(exec);
        assertEquals(ExecutionStatus.COMPLETED, exec.getStatus());
        assertTrue(exec.getEventCount() >= 5, "Must record at least 5 lifecycle events");
        assertNotNull(exec.getFinalHash());

        // Verify SHA-256 chain integrity
        IntegrityVerificationResult integrity = executionService.verifyExecutionIntegrity(exec.getId());
        assertEquals(IntegrityStatus.INTEGRITY_VERIFIED, integrity.getStatus());
        assertEquals(exec.getEventCount().intValue(), integrity.getTotalEventsVerified());
    }

    @Test
    @DisplayName("Failure and retry scenario records distinct error and retry events")
    void testFailureRetryScenario() {
        AgentEntity agent = agentRepository.findAll().get(0);
        TaskEntity task = new TaskEntity(UUID.randomUUID().toString(), "Retry Test Task", "fail_search retry", agent.getId(), "READY", Instant.now());
        taskRepository.save(task);

        ExecutionEntity exec = agentExecutionService.runExecution(task.getId(), "FAILURE_RETRY");

        assertNotNull(exec);
        assertEquals(ExecutionStatus.COMPLETED, exec.getStatus());

        var events = executionService.getExecutionTimeline(exec.getId());
        boolean hasErrorEvent = events.stream().anyMatch(e -> "ERROR_OCCURRED".equals(e.getEventType().name()));
        boolean hasRetryEvent = events.stream().anyMatch(e -> "RETRY_ATTEMPT".equals(e.getEventType().name()));

        assertTrue(hasErrorEvent, "Must record individual ERROR_OCCURRED event");
        assertTrue(hasRetryEvent, "Must record individual RETRY_ATTEMPT event");

        IntegrityVerificationResult integrity = executionService.verifyExecutionIntegrity(exec.getId());
        assertEquals(IntegrityStatus.INTEGRITY_VERIFIED, integrity.getStatus());
    }

    @Test
    @DisplayName("Policy denied scenario halts execution without unauthorized tool calls")
    void testPolicyDeniedScenario() {
        AgentEntity agent = agentRepository.findAll().get(0);
        TaskEntity task = new TaskEntity(UUID.randomUUID().toString(), "Policy Denial Test", "delete_database attempt", agent.getId(), "READY", Instant.now());
        taskRepository.save(task);

        ExecutionEntity exec = agentExecutionService.runExecution(task.getId(), "POLICY_DENIED");

        assertNotNull(exec);
        var decisions = executionService.getPolicyDecisions(exec.getId());
        assertFalse(decisions.isEmpty(), "Must record policy decision entity");
        assertEquals("DENIED", decisions.get(0).getDecision().name());

        var events = executionService.getExecutionTimeline(exec.getId());
        boolean hasDeniedEvent = events.stream().anyMatch(e -> e.getPolicyResult() != null && "DENIED".equals(e.getPolicyResult().name()));
        assertTrue(hasDeniedEvent, "Must record POLICY_EVALUATION DENIED event");
    }

    @Test
    @DisplayName("Delegation scenario links child execution to parent execution")
    void testDelegationScenario() {
        AgentEntity agent = agentRepository.findAll().get(0);
        TaskEntity task = new TaskEntity(UUID.randomUUID().toString(), "Delegation Test Task", "Multi-agent flight audit", agent.getId(), "READY", Instant.now());
        taskRepository.save(task);

        ExecutionEntity parentExec = agentExecutionService.runExecution(task.getId(), "DELEGATION");

        assertNotNull(parentExec);
        List<ExecutionEntity> children = executionRepository.findByParentExecutionIdOrderByStartedAtAsc(parentExec.getId());
        assertFalse(children.isEmpty(), "Must spawn child execution linked to parent");

        ExecutionEntity child = children.get(0);
        assertEquals(parentExec.getId(), child.getParentExecutionId());
        assertEquals(parentExec.getRootExecutionId(), child.getRootExecutionId());

        // Both executions verify integrity
        assertEquals(IntegrityStatus.INTEGRITY_VERIFIED, executionService.verifyExecutionIntegrity(parentExec.getId()).getStatus());
        assertEquals(IntegrityStatus.INTEGRITY_VERIFIED, executionService.verifyExecutionIntegrity(child.getId()).getStatus());
    }
}
