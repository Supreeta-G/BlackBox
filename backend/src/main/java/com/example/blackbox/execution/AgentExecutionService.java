package com.example.blackbox.execution;

import com.example.blackbox.ai.AiResponse;
import com.example.blackbox.ai.AiService;
import com.example.blackbox.entity.AgentEntity;
import com.example.blackbox.entity.ExecutionEntity;
import com.example.blackbox.entity.TaskEntity;
import com.example.blackbox.enums.AgentRole;
import com.example.blackbox.enums.EventType;
import com.example.blackbox.enums.ExecutionStatus;
import com.example.blackbox.enums.PolicyResult;
import com.example.blackbox.exception.ResourceNotFoundException;
import com.example.blackbox.policy.PolicyEvaluationService;
import com.example.blackbox.repository.AgentRepository;
import com.example.blackbox.repository.ExecutionRepository;
import com.example.blackbox.repository.TaskRepository;
import com.example.blackbox.tools.ToolExecutionResult;
import com.example.blackbox.tools.ToolRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.*;

@Service
public class AgentExecutionService {

    private static final Logger log = LoggerFactory.getLogger(AgentExecutionService.class);

    private final TaskRepository taskRepository;
    private final AgentRepository agentRepository;
    private final ExecutionRepository executionRepository;
    private final ExecutionRecorder recorder;
    private final PolicyEvaluationService policyService;
    private final ToolRegistry toolRegistry;
    private final AiService aiService;

    public AgentExecutionService(TaskRepository taskRepository,
                                 AgentRepository agentRepository,
                                 ExecutionRepository executionRepository,
                                 ExecutionRecorder recorder,
                                 PolicyEvaluationService policyService,
                                 ToolRegistry toolRegistry,
                                 AiService aiService) {
        this.taskRepository = taskRepository;
        this.agentRepository = agentRepository;
        this.executionRepository = executionRepository;
        this.recorder = recorder;
        this.policyService = policyService;
        this.toolRegistry = toolRegistry;
        this.aiService = aiService;
    }

    /**
     * Executes a task through the complete 12-stage Black Box lifecycle.
     */
    @Transactional
    public ExecutionEntity runExecution(String taskId, String scenario) {
        TaskEntity task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found with ID: " + taskId));
        AgentEntity agent = agentRepository.findById(task.getAgentId())
                .orElseThrow(() -> new ResourceNotFoundException("Agent not found with ID: " + task.getAgentId()));

        String executionId = UUID.randomUUID().toString();
        String correlationId = "corr-" + UUID.randomUUID().toString().substring(0, 18);
        Instant startedAt = Instant.now();

        ExecutionEntity execution = new ExecutionEntity(
                executionId,
                task.getId(),
                agent.getId(),
                correlationId,
                executionId, // root execution ID defaults to self
                null,        // no parent for top-level execution
                ExecutionStatus.RUNNING,
                startedAt,
                startedAt
        );
        execution = executionRepository.save(execution);

        try {
            // Stage 1 & 2: User Request & Start Execution
            recorder.recordEvent(
                    execution,
                    EventType.TASK_STARTED,
                    "Execution initiated for task: '" + task.getTitle() + "' under agent: " + agent.getName(),
                    null,
                    "taskId=" + task.getId() + ", scenario=" + scenario,
                    "status=RUNNING, correlationId=" + correlationId,
                    "RUNNING",
                    0L,
                    null,
                    null,
                    null,
                    "ref:task/" + task.getId()
            );

            // Stage 3: Capture Context
            String contextMeta = "model=" + agent.getModelIdentifier() + ", configHash=" + agent.getConfigurationHash() +
                    ", agentRole=" + agent.getRole();
            recorder.recordEvent(
                    execution,
                    EventType.CONTEXT_CAPTURED,
                    "Captured initial execution environment, configuration hash, and task parameters",
                    null,
                    contextMeta,
                    "Context snapshot securely recorded and indexed",
                    "SUCCESS",
                    5L,
                    null,
                    null,
                    null,
                    "ref:agent/" + agent.getId()
            );

            // Execute according to scenario
            if ("FAILURE_RETRY".equalsIgnoreCase(scenario)) {
                runFailureRetryScenario(execution, agent, task);
            } else if ("POLICY_DENIED".equalsIgnoreCase(scenario)) {
                runPolicyDeniedScenario(execution, agent, task);
            } else if ("DELEGATION".equalsIgnoreCase(scenario)) {
                runDelegationScenario(execution, agent, task);
            } else {
                runStandardScenario(execution, agent, task);
            }

            // Stage 11: Close Execution (COMPLETED)
            closeExecution(execution, ExecutionStatus.COMPLETED, null);

        } catch (Exception ex) {
            log.error("Execution failed with error: {}", ex.getMessage(), ex);
            closeExecution(execution, ExecutionStatus.FAILED, ex.getMessage());
        }

        return executionRepository.findById(execution.getId()).orElse(execution);
    }

    private void runStandardScenario(ExecutionEntity execution, AgentEntity agent, TaskEntity task) {
        // Stage 4: Action Boundary (Agent decides to call InformationSearchTool)
        recorder.recordEvent(
                execution,
                EventType.ACTION_BOUNDARY,
                "Agent formulated initial operational plan: query domain knowledge base",
                null,
                "prompt=" + task.getDescription(),
                "plan=Invoke InformationSearchTool for background evidence",
                "SUCCESS",
                12L,
                null,
                null,
                null,
                null
        );

        // Stage 5: Policy Check
        PolicyEvaluationService.EvaluationResult policyResult = policyService.evaluate(
                "TOOL_INVOCATION", "InformationSearchTool", Map.of("query", task.getDescription()));
        var polEvt = recorder.recordEvent(
                execution,
                EventType.POLICY_EVALUATION,
                "Evaluated tool authorization policy for InformationSearchTool",
                "InformationSearchTool",
                "resource=knowledge_base",
                "decision=" + policyResult.getDecision() + ", reason=" + policyResult.getReason(),
                policyResult.getDecision().name(),
                4L,
                null,
                policyResult.getDecision(),
                policyResult.getReason(),
                "ref:policy/POL-STD-00"
        );
        policyService.recordDecision(execution.getId(), polEvt.getId(), "TOOL_INVOCATION",
                "InformationSearchTool", policyResult.getDecision(), policyResult.getReason());

        // Stage 6: Tool Invocation
        var callEvt = recorder.recordEvent(
                execution,
                EventType.TOOL_INVOCATION,
                "Invoking InformationSearchTool with task query",
                "InformationSearchTool",
                "query=" + task.getDescription(),
                null,
                "DISPATCHED",
                null,
                null,
                null,
                null,
                null
        );

        // Stage 7: Tool Result
        ToolExecutionResult toolResult = toolRegistry.invokeTool(
                "InformationSearchTool",
                Map.of("query", task.getDescription()),
                execution.getId(),
                callEvt.getId()
        );

        recorder.recordEvent(
                execution,
                EventType.TOOL_RESULT,
                "Received tool output from InformationSearchTool",
                "InformationSearchTool",
                "query=" + task.getDescription(),
                toolResult.getOutput(),
                toolResult.isSuccess() ? "SUCCESS" : "FAILED",
                toolResult.getDurationMs(),
                toolResult.getErrorMessage(),
                null,
                null,
                "ref:artifact/tool-out-" + execution.getId().substring(0, 8)
        );

        // Stage 8: State Change
        recorder.recordEvent(
                execution,
                EventType.STATE_CHANGE,
                "Agent state transitioned: WAITING_FOR_TOOL -> SYNTHESIZING_RESPONSE",
                null,
                "priorState=WAITING_FOR_TOOL",
                "newState=SYNTHESIZING_RESPONSE",
                "SUCCESS",
                2L,
                null,
                null,
                null,
                null
        );

        // Model synthesis
        AiResponse finalAns = aiService.synthesizeResult(task.getTitle(), List.of(toolResult.getOutput()));
        execution.setFinalResult(finalAns.getContent());
    }

    private void runFailureRetryScenario(ExecutionEntity execution, AgentEntity agent, TaskEntity task) {
        // Attempt 1: Call InformationSearchTool with fail_search to simulate timeout
        recorder.recordEvent(
                execution,
                EventType.ACTION_BOUNDARY,
                "Agent dispatched knowledge retrieval attempt 1",
                "InformationSearchTool",
                "query=fail_search",
                "plan=Search external knowledge cluster",
                "STARTED",
                10L,
                null,
                null,
                null,
                null
        );

        var toolEvt1 = recorder.recordEvent(
                execution,
                EventType.TOOL_INVOCATION,
                "Tool call attempt 1 to InformationSearchTool (unstable gateway)",
                "InformationSearchTool",
                "query=fail_search",
                null,
                "DISPATCHED",
                null,
                null,
                null,
                null,
                null
        );

        ToolExecutionResult res1 = toolRegistry.invokeTool(
                "InformationSearchTool",
                Map.of("query", "fail_search"),
                execution.getId(),
                toolEvt1.getId()
        );

        // Stage 7 & 10: Tool Result failure and ERROR_OCCURRED
        recorder.recordEvent(
                execution,
                EventType.TOOL_RESULT,
                "Tool call failed due to simulated cluster timeout",
                "InformationSearchTool",
                "query=fail_search",
                res1.getErrorMessage(),
                "FAILED",
                res1.getDurationMs(),
                res1.getErrorMessage(),
                null,
                null,
                null
        );

        recorder.recordEvent(
                execution,
                EventType.ERROR_OCCURRED,
                "Detected tool execution failure: " + res1.getErrorMessage(),
                "InformationSearchTool",
                "attempt=1",
                "status=FAILED",
                "ERROR",
                1L,
                res1.getErrorMessage(),
                null,
                null,
                null
        );

        // Stage 10: Retry Attempt
        recorder.recordEvent(
                execution,
                EventType.RETRY_ATTEMPT,
                "Initiating automated retry policy (Attempt 2 of 3) with fallback query parameters",
                "InformationSearchTool",
                "retryCount=1, backoffMs=150",
                "status=RETRYING",
                "RETRY",
                150L,
                null,
                null,
                null,
                null
        );

        // Attempt 2: Success
        var toolEvt2 = recorder.recordEvent(
                execution,
                EventType.TOOL_INVOCATION,
                "Tool call attempt 2 to InformationSearchTool (recovered channel)",
                "InformationSearchTool",
                "query=Flight telemetry incident logs and checksum algorithms",
                null,
                "DISPATCHED",
                null,
                null,
                null,
                null,
                null
        );

        ToolExecutionResult res2 = toolRegistry.invokeTool(
                "InformationSearchTool",
                Map.of("query", "Flight telemetry incident logs and checksum algorithms"),
                execution.getId(),
                toolEvt2.getId()
        );

        recorder.recordEvent(
                execution,
                EventType.TOOL_RESULT,
                "Tool call attempt 2 succeeded with verified facts",
                "InformationSearchTool",
                "query=Flight telemetry incident logs and checksum algorithms",
                res2.getOutput(),
                "SUCCESS",
                res2.getDurationMs(),
                null,
                null,
                null,
                "ref:artifact/retry-success"
        );

        AiResponse finalAns = aiService.synthesizeResult(task.getTitle(),
                List.of("[Retry Recovered]: " + res2.getOutput()));
        execution.setFinalResult(finalAns.getContent());
    }

    private void runPolicyDeniedScenario(ExecutionEntity execution, AgentEntity agent, TaskEntity task) {
        recorder.recordEvent(
                execution,
                EventType.ACTION_BOUNDARY,
                "Agent formulated action: purge system records and elevated privilege execution",
                null,
                "action=delete_database, target=system_cluster_core",
                "status=PENDING_POLICY_CHECK",
                "PENDING",
                5L,
                null,
                null,
                null,
                null
        );

        // Stage 5: Policy check returns DENIED
        PolicyEvaluationService.EvaluationResult policyResult = policyService.evaluate(
                "delete_database", "system_cluster_core", Map.of("command", "DROP TABLE users"));

        var polEvt = recorder.recordEvent(
                execution,
                EventType.POLICY_EVALUATION,
                "Policy evaluation DENIED requested destructive action",
                "SecurityInterceptor",
                "action=delete_database",
                "decision=" + policyResult.getDecision() + ", reason=" + policyResult.getReason(),
                policyResult.getDecision().name(),
                2L,
                policyResult.getReason(),
                policyResult.getDecision(),
                policyResult.getReason(),
                "ref:policy/POL-SEC-01"
        );
        policyService.recordDecision(execution.getId(), polEvt.getId(), "delete_database",
                "system_cluster_core", policyResult.getDecision(), policyResult.getReason());

        // Action is aborted! Record ERROR_OCCURRED and STATE_CHANGE
        recorder.recordEvent(
                execution,
                EventType.ERROR_OCCURRED,
                "Action blocked by policy engine. External invocation was safely halted.",
                "SecurityInterceptor",
                "action=delete_database",
                "status=BLOCKED",
                "DENIED",
                1L,
                policyResult.getReason(),
                null,
                null,
                null
        );

        recorder.recordEvent(
                execution,
                EventType.STATE_CHANGE,
                "Agent state transitioned: RUNNING -> HALTED_POLICY_VIOLATION",
                null,
                "state=HALTED",
                "outcome=VIOLATION",
                "HALTED",
                1L,
                null,
                null,
                null,
                null
        );

        execution.setErrorMessage("Execution halted: " + policyResult.getReason());
        execution.setFinalResult("[SECURITY HALT] Requested action was denied by Black Box Policy Engine. No unauthorized actions were executed.");
    }

    private void runDelegationScenario(ExecutionEntity execution, AgentEntity agent, TaskEntity task) {
        // Find or create SummaryAgent
        AgentEntity summaryAgent = agentRepository.findByName("SummaryAgent").orElseGet(() -> {
            AgentEntity newAgent = new AgentEntity(
                    UUID.randomUUID().toString(),
                    "SummaryAgent",
                    AgentRole.SUMMARY_AGENT,
                    "gpt-4o-mini",
                    "cfg-summary-v1",
                    Instant.now()
            );
            return agentRepository.save(newAgent);
        });

        // Step 1: Parent conducts initial research
        recorder.recordEvent(
                execution,
                EventType.ACTION_BOUNDARY,
                "Orchestrator formulating multi-agent delegation plan",
                null,
                "task=Investigate flight recorder telemetry",
                "plan=Query data, then delegate executive summary to SummaryAgent",
                "SUCCESS",
                15L,
                null,
                null,
                null,
                null
        );

        var toolEvt = recorder.recordEvent(
                execution,
                EventType.TOOL_INVOCATION,
                "Invoking InformationSearchTool for raw telemetry",
                "InformationSearchTool",
                "query=telemetry incident logs",
                null,
                "DISPATCHED",
                null,
                null,
                null,
                null,
                null
        );

        ToolExecutionResult researchResult = toolRegistry.invokeTool(
                "InformationSearchTool",
                Map.of("query", "telemetry incident logs"),
                execution.getId(),
                toolEvt.getId()
        );

        recorder.recordEvent(
                execution,
                EventType.TOOL_RESULT,
                "Gathered raw telemetry data for synthesis",
                "InformationSearchTool",
                "query=telemetry incident logs",
                researchResult.getOutput(),
                "SUCCESS",
                researchResult.getDurationMs(),
                null,
                null,
                null,
                "ref:artifact/raw-telemetry"
        );

        // Step 2: Parent initiates delegation
        String childExecId = UUID.randomUUID().toString();
        recorder.recordEvent(
                execution,
                EventType.DELEGATION_STARTED,
                "Delegating synthesis subtask to SummaryAgent (Child Exec: " + childExecId + ")",
                null,
                "targetAgent=SummaryAgent, parentExec=" + execution.getId(),
                "childExecId=" + childExecId,
                "DELEGATING",
                10L,
                null,
                null,
                null,
                "ref:exec/" + childExecId
        );

        // Step 3: Run child execution
        ExecutionEntity childExecution = new ExecutionEntity(
                childExecId,
                task.getId(),
                summaryAgent.getId(),
                execution.getCorrelationId(),
                execution.getRootExecutionId(),
                execution.getId(), // parent link!
                ExecutionStatus.RUNNING,
                Instant.now(),
                Instant.now()
        );
        childExecution = executionRepository.save(childExecution);

        // Child lifecycle events
        recorder.recordEvent(
                childExecution,
                EventType.TASK_STARTED,
                "Child execution spawned for subtask delegation from parent: " + execution.getId(),
                null,
                "parentExecId=" + execution.getId(),
                "status=RUNNING",
                "RUNNING",
                0L,
                null,
                null,
                null,
                "ref:parent/" + execution.getId()
        );

        recorder.recordEvent(
                childExecution,
                EventType.CONTEXT_CAPTURED,
                "Child agent captured delegator context and inherited correlation ID",
                null,
                "inheritedCorrId=" + execution.getCorrelationId(),
                "context=Inherited telemetry data",
                "SUCCESS",
                2L,
                null,
                null,
                null,
                null
        );

        // Child tool invocation: CalculatorTool for summary metrics
        var childToolEvt = recorder.recordEvent(
                childExecution,
                EventType.TOOL_INVOCATION,
                "Child agent invoking CalculatorTool for telemetry metric aggregation",
                "CalculatorTool",
                "expression=4200 + 800",
                null,
                "DISPATCHED",
                null,
                null,
                null,
                null,
                null
        );

        ToolExecutionResult calcRes = toolRegistry.invokeTool(
                "CalculatorTool",
                Map.of("expression", "4200 + 800"),
                childExecution.getId(),
                childToolEvt.getId()
        );

        recorder.recordEvent(
                childExecution,
                EventType.TOOL_RESULT,
                "Calculated telemetry aggregations in child execution",
                "CalculatorTool",
                "expression=4200 + 800",
                calcRes.getOutput(),
                "SUCCESS",
                calcRes.getDurationMs(),
                null,
                null,
                null,
                null
        );

        // Close child execution
        String childSummary = "[EXECUTIVE SUMMARY by SummaryAgent]: Aggregated telemetry confirmed nominal sensor operations (Sum: " +
                calcRes.getOutput() + "). Delegated subtask complete.";
        childExecution.setFinalResult(childSummary);
        closeExecution(childExecution, ExecutionStatus.COMPLETED, null);

        // Step 4: Parent records delegation completed
        recorder.recordEvent(
                execution,
                EventType.DELEGATION_COMPLETED,
                "Child execution " + childExecId + " completed. Received synthesized brief.",
                null,
                "childExecId=" + childExecId,
                childSummary,
                "COMPLETED",
                50L,
                null,
                null,
                null,
                "ref:exec/" + childExecId
        );

        execution.setFinalResult("[ORCHESTRATOR REPORT]: Task fully orchestrated across multi-agent boundary.\n" + childSummary);
    }

    private void closeExecution(ExecutionEntity execution, ExecutionStatus finalStatus, String errorMessage) {
        Instant completedAt = Instant.now();
        long duration = completedAt.toEpochMilli() - execution.getStartedAt().toEpochMilli();
        execution.setStatus(finalStatus);
        execution.setCompletedAt(completedAt);
        execution.setDurationMs(duration);
        execution.setErrorMessage(errorMessage);

        EventType closeEventType = finalStatus == ExecutionStatus.COMPLETED ? EventType.TASK_COMPLETED : EventType.TASK_FAILED;

        var lastEvt = recorder.recordEvent(
                execution,
                closeEventType,
                "Execution finalized with status: " + finalStatus,
                null,
                "finalStatus=" + finalStatus + ", durationMs=" + duration,
                execution.getFinalResult(),
                finalStatus.name(),
                duration,
                errorMessage,
                null,
                null,
                "ref:exec-summary/" + execution.getId()
        );

        execution.setFinalHash(lastEvt.getCurrentEventHash());
        executionRepository.save(execution);
        log.info("Closed execution {} with status {} and duration {}ms", execution.getId(), finalStatus, duration);
    }
}
