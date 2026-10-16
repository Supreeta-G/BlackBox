package com.example.blackbox.controller;

import com.example.blackbox.dto.AgentEventDto;
import com.example.blackbox.dto.ExecutionComparisonDto;
import com.example.blackbox.dto.ExecutionDto;
import com.example.blackbox.dto.IntegrityVerificationResult;
import com.example.blackbox.entity.PolicyDecisionEntity;
import com.example.blackbox.entity.ToolCallEntity;
import com.example.blackbox.enums.EventType;
import com.example.blackbox.service.ExecutionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/executions")
@CrossOrigin(origins = "*")
public class ExecutionController {

    private final ExecutionService executionService;

    public ExecutionController(ExecutionService executionService) {
        this.executionService = executionService;
    }

    @GetMapping
    public ResponseEntity<List<ExecutionDto>> getAllExecutions() {
        return ResponseEntity.ok(executionService.getAllExecutions());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ExecutionDto> getExecutionById(@PathVariable("id") String id) {
        return ResponseEntity.ok(executionService.getExecutionById(id));
    }

    @GetMapping("/{id}/children")
    public ResponseEntity<List<ExecutionDto>> getChildExecutions(@PathVariable("id") String id) {
        return ResponseEntity.ok(executionService.getChildExecutions(id));
    }

    @GetMapping("/{id}/events")
    public ResponseEntity<List<AgentEventDto>> getExecutionEvents(
            @PathVariable("id") String id,
            @RequestParam(name = "eventType", required = false) EventType eventType,
            @RequestParam(name = "status", required = false) String status,
            @RequestParam(name = "toolName", required = false) String toolName) {
        return ResponseEntity.ok(executionService.getExecutionEvents(id, eventType, status, toolName));
    }

    @GetMapping("/{id}/timeline")
    public ResponseEntity<List<AgentEventDto>> getExecutionTimeline(@PathVariable("id") String id) {
        return ResponseEntity.ok(executionService.getExecutionTimeline(id));
    }

    @GetMapping("/{id}/verify-integrity")
    public ResponseEntity<IntegrityVerificationResult> verifyIntegrity(@PathVariable("id") String id) {
        return ResponseEntity.ok(executionService.verifyExecutionIntegrity(id));
    }

    @GetMapping("/{id}/tool-calls")
    public ResponseEntity<List<ToolCallEntity>> getToolCalls(@PathVariable("id") String id) {
        return ResponseEntity.ok(executionService.getToolCalls(id));
    }

    @GetMapping("/{id}/policy-decisions")
    public ResponseEntity<List<PolicyDecisionEntity>> getPolicyDecisions(@PathVariable("id") String id) {
        return ResponseEntity.ok(executionService.getPolicyDecisions(id));
    }

    @GetMapping("/{id}/evidence")
    public ResponseEntity<Map<String, Object>> getEvidenceSummary(@PathVariable("id") String id) {
        ExecutionDto execution = executionService.getExecutionById(id);
        List<AgentEventDto> timeline = executionService.getExecutionTimeline(id);
        List<ToolCallEntity> toolCalls = executionService.getToolCalls(id);
        List<PolicyDecisionEntity> policyDecisions = executionService.getPolicyDecisions(id);
        IntegrityVerificationResult integrity = executionService.verifyExecutionIntegrity(id);

        return ResponseEntity.ok(Map.of(
                "execution", execution,
                "timeline", timeline,
                "toolCalls", toolCalls,
                "policyDecisions", policyDecisions,
                "integrity", integrity
        ));
    }

    @GetMapping("/compare")
    public ResponseEntity<ExecutionComparisonDto> compareExecutions(
            @RequestParam("exec1") String exec1,
            @RequestParam("exec2") String exec2) {
        return ResponseEntity.ok(executionService.compareExecutions(exec1, exec2));
    }
}
