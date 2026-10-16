package com.example.blackbox.controller;

import com.example.blackbox.dto.ExecutionDto;
import com.example.blackbox.dto.RunExecutionRequest;
import com.example.blackbox.dto.TamperEventRequest;
import com.example.blackbox.entity.AgentEntity;
import com.example.blackbox.entity.ExecutionEntity;
import com.example.blackbox.enums.AgentRole;
import com.example.blackbox.execution.AgentExecutionService;
import com.example.blackbox.repository.AgentRepository;
import com.example.blackbox.service.ExecutionService;
import com.example.blackbox.service.TaskService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@RestController
@CrossOrigin(origins = "*")
public class DemoController {

    private final ExecutionService executionService;
    private final AgentExecutionService agentExecutionService;
    private final TaskService taskService;
    private final AgentRepository agentRepository;

    public DemoController(ExecutionService executionService,
                          AgentExecutionService agentExecutionService,
                          TaskService taskService,
                          AgentRepository agentRepository) {
        this.executionService = executionService;
        this.agentExecutionService = agentExecutionService;
        this.taskService = taskService;
        this.agentRepository = agentRepository;
    }

    /**
     * Development-Only Tampering Demonstration
     */
    @PostMapping("/api/dev/executions/{id}/tamper")
    public ResponseEntity<Map<String, String>> tamperEvent(
            @PathVariable("id") String id,
            @RequestBody(required = false) TamperEventRequest request) {
        executionService.tamperEventForDemo(id, request);
        return ResponseEntity.ok(Map.of(
                "status", "TAMPERED",
                "message", "Event successfully altered in database. Integrity chain broken at target sequence.",
                "executionId", id
        ));
    }

    /**
     * Run a demo scenario end-to-end.
     */
    @PostMapping("/api/demo/run-scenario")
    public ResponseEntity<ExecutionDto> runScenario(@RequestBody(required = false) RunExecutionRequest request) {
        String scenario = (request != null && request.getScenario() != null) ? request.getScenario() : "STANDARD";

        AgentEntity orchestrator = agentRepository.findByName("OrchestratorAgent").orElseGet(() -> {
            AgentEntity agent = new AgentEntity(
                    UUID.randomUUID().toString(),
                    "OrchestratorAgent",
                    AgentRole.ORCHESTRATOR_AGENT,
                    "gpt-4o-mini",
                    "cfg-orch-01",
                    Instant.now()
            );
            return agentRepository.save(agent);
        });

        String taskTitle;
        String taskDesc;
        switch (scenario.toUpperCase()) {
            case "FAILURE_RETRY":
                taskTitle = "Simulate Tool Failure and Auto-Retry Handling";
                taskDesc = "Query unstable cluster with retry backoff and error logging";
                break;
            case "POLICY_DENIED":
                taskTitle = "Simulate Prohibited Administrative Action";
                taskDesc = "Attempt destructive drop database command to test policy interception";
                break;
            case "DELEGATION":
                taskTitle = "Execute Multi-Agent Delegated Flight Investigation";
                taskDesc = "Gather telemetry metrics and delegate synthesis brief to SummaryAgent";
                break;
            default:
                taskTitle = "Autonomous Knowledge Retrieval and Flight Telemetry Audit";
                taskDesc = "Investigate flight telemetry integrity and verify cryptographic black box recorder";
                break;
        }

        var task = taskService.createTask(new com.example.blackbox.dto.CreateTaskRequest(
                taskTitle, taskDesc, orchestrator.getId()
        ));

        ExecutionEntity execution = agentExecutionService.runExecution(task.getId(), scenario);
        return ResponseEntity.ok(executionService.toExecutionDto(execution));
    }
}
