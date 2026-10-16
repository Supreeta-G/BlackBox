package com.example.blackbox.service;

import com.example.blackbox.dto.*;
import com.example.blackbox.entity.AgentEventEntity;
import com.example.blackbox.entity.ExecutionEntity;
import com.example.blackbox.entity.PolicyDecisionEntity;
import com.example.blackbox.entity.ToolCallEntity;
import com.example.blackbox.enums.EventType;
import com.example.blackbox.exception.ResourceNotFoundException;
import com.example.blackbox.integrity.IntegrityService;
import com.example.blackbox.repository.AgentEventRepository;
import com.example.blackbox.repository.ExecutionRepository;
import com.example.blackbox.repository.PolicyDecisionRepository;
import com.example.blackbox.repository.ToolCallRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class ExecutionService {

    private static final Logger log = LoggerFactory.getLogger(ExecutionService.class);

    private final ExecutionRepository executionRepository;
    private final AgentEventRepository eventRepository;
    private final ToolCallRepository toolCallRepository;
    private final PolicyDecisionRepository policyDecisionRepository;
    private final IntegrityService integrityService;

    public ExecutionService(ExecutionRepository executionRepository,
                            AgentEventRepository eventRepository,
                            ToolCallRepository toolCallRepository,
                            PolicyDecisionRepository policyDecisionRepository,
                            IntegrityService integrityService) {
        this.executionRepository = executionRepository;
        this.eventRepository = eventRepository;
        this.toolCallRepository = toolCallRepository;
        this.policyDecisionRepository = policyDecisionRepository;
        this.integrityService = integrityService;
    }

    @Transactional(readOnly = true)
    public List<ExecutionDto> getAllExecutions() {
        return executionRepository.findAllByOrderByStartedAtDesc().stream()
                .map(this::toExecutionDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ExecutionDto getExecutionById(String id) {
        return executionRepository.findById(id)
                .map(this::toExecutionDto)
                .orElseThrow(() -> new ResourceNotFoundException("Execution not found with ID: " + id));
    }

    @Transactional(readOnly = true)
    public List<ExecutionDto> getChildExecutions(String parentExecutionId) {
        return executionRepository.findByParentExecutionIdOrderByStartedAtAsc(parentExecutionId).stream()
                .map(this::toExecutionDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<AgentEventDto> getExecutionEvents(String executionId, EventType eventType, String status, String toolName) {
        if (!executionRepository.existsById(executionId)) {
            throw new ResourceNotFoundException("Execution not found with ID: " + executionId);
        }
        return eventRepository.findFilteredEvents(executionId, eventType, status, toolName).stream()
                .map(this::toEventDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<AgentEventDto> getExecutionTimeline(String executionId) {
        if (!executionRepository.existsById(executionId)) {
            throw new ResourceNotFoundException("Execution not found with ID: " + executionId);
        }
        return eventRepository.findByExecutionIdOrderBySequenceNumberAsc(executionId).stream()
                .map(this::toEventDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public IntegrityVerificationResult verifyExecutionIntegrity(String executionId) {
        if (!executionRepository.existsById(executionId)) {
            throw new ResourceNotFoundException("Execution not found with ID: " + executionId);
        }
        return integrityService.verifyExecutionIntegrity(executionId);
    }

    @Transactional(readOnly = true)
    public List<ToolCallEntity> getToolCalls(String executionId) {
        return toolCallRepository.findByExecutionIdOrderByStartedAtAsc(executionId);
    }

    @Transactional(readOnly = true)
    public List<PolicyDecisionEntity> getPolicyDecisions(String executionId) {
        return policyDecisionRepository.findByExecutionIdOrderByEvaluatedAtAsc(executionId);
    }

    @Transactional(readOnly = true)
    public ExecutionComparisonDto compareExecutions(String id1, String id2) {
        ExecutionDto e1 = getExecutionById(id1);
        ExecutionDto e2 = getExecutionById(id2);

        List<AgentEventEntity> events1 = eventRepository.findByExecutionIdOrderBySequenceNumberAsc(id1);
        List<AgentEventEntity> events2 = eventRepository.findByExecutionIdOrderBySequenceNumberAsc(id2);

        List<String> tools1 = events1.stream()
                .map(AgentEventEntity::getToolName)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());

        List<String> tools2 = events2.stream()
                .map(AgentEventEntity::getToolName)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());

        List<String> errs1 = events1.stream()
                .map(AgentEventEntity::getErrorDetails)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        List<String> errs2 = events2.stream()
                .map(AgentEventEntity::getErrorDetails)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        int pol1 = policyDecisionRepository.findByExecutionIdOrderByEvaluatedAtAsc(id1).size();
        int pol2 = policyDecisionRepository.findByExecutionIdOrderByEvaluatedAtAsc(id2).size();

        long dur1 = e1.getDurationMs() != null ? e1.getDurationMs() : 0L;
        long dur2 = e2.getDurationMs() != null ? e2.getDurationMs() : 0L;

        ExecutionComparisonDto comp = new ExecutionComparisonDto();
        comp.setExecution1(e1);
        comp.setExecution2(e2);
        comp.setDurationDifferenceMs(Math.abs(dur1 - dur2));
        comp.setEventCountDifference(Math.abs((e1.getEventCount() != null ? e1.getEventCount() : 0) -
                (e2.getEventCount() != null ? e2.getEventCount() : 0)));
        comp.setSameStatus(e1.getStatus() == e2.getStatus());
        comp.setToolsUsedExecution1(tools1);
        comp.setToolsUsedExecution2(tools2);
        comp.setErrorsExecution1(errs1);
        comp.setErrorsExecution2(errs2);
        comp.setPolicyDecisionsExecution1(pol1);
        comp.setPolicyDecisionsExecution2(pol2);
        comp.setIntegrityExecution1(integrityService.verifyExecutionIntegrity(id1));
        comp.setIntegrityExecution2(integrityService.verifyExecutionIntegrity(id2));

        return comp;
    }

    /**
     * Development-Only Tampering Demonstration:
     * Intentionally modifies a recorded event in the database without recomputing the SHA-256 chain.
     */
    @Transactional
    public void tamperEventForDemo(String executionId, TamperEventRequest request) {
        int targetSeq = request != null && request.getSequenceNumber() != null ? request.getSequenceNumber() : 2;

        AgentEventEntity event = eventRepository.findByExecutionIdAndSequenceNumber(executionId, targetSeq)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found for execution " + executionId + " at sequence #" + targetSeq));

        String newDesc = (request != null && request.getModifiedDescription() != null)
                ? request.getModifiedDescription()
                : "[TAMPERED VIA DEV CONSOLE] Maliciously altered action parameters to bypass security policy";

        log.warn("TAMPER DEMO EXECUTED: Modifying event {} at sequence #{} in execution {}", event.getId(), targetSeq, executionId);

        event.setDescription(newDesc);
        if (request != null && request.getModifiedInput() != null) {
            event.setSanitizedInput(request.getModifiedInput());
        }
        event.setIsTampered(true);
        eventRepository.save(event);
    }

    public ExecutionDto toExecutionDto(ExecutionEntity e) {
        return new ExecutionDto(
                e.getId(),
                e.getTaskId(),
                e.getAgentId(),
                e.getCorrelationId(),
                e.getRootExecutionId(),
                e.getParentExecutionId(),
                e.getStatus(),
                e.getStartedAt(),
                e.getCompletedAt(),
                e.getDurationMs(),
                e.getEventCount(),
                e.getFinalResult(),
                e.getFinalHash(),
                e.getErrorMessage(),
                e.getCreatedAt()
        );
    }

    public AgentEventDto toEventDto(AgentEventEntity e) {
        AgentEventDto dto = new AgentEventDto();
        dto.setId(e.getId());
        dto.setExecutionId(e.getExecutionId());
        dto.setTaskId(e.getTaskId());
        dto.setAgentId(e.getAgentId());
        dto.setParentExecutionId(e.getParentExecutionId());
        dto.setCorrelationId(e.getCorrelationId());
        dto.setEventType(e.getEventType());
        dto.setTimestamp(e.getTimestamp());
        dto.setSequenceNumber(e.getSequenceNumber());
        dto.setDescription(e.getDescription());
        dto.setToolName(e.getToolName());
        dto.setSanitizedInput(e.getSanitizedInput());
        dto.setSanitizedOutput(e.getSanitizedOutput());
        dto.setStatus(e.getStatus());
        dto.setDurationMs(e.getDurationMs());
        dto.setErrorDetails(e.getErrorDetails());
        dto.setPolicyResult(e.getPolicyResult());
        dto.setPolicyReason(e.getPolicyReason());
        dto.setEvidenceReference(e.getEvidenceReference());
        dto.setContentHash(e.getContentHash());
        dto.setPreviousEventHash(e.getPreviousEventHash());
        dto.setCurrentEventHash(e.getCurrentEventHash());
        dto.setTraceId(e.getTraceId());
        dto.setSpanId(e.getSpanId());
        dto.setIsTampered(e.getIsTampered());
        return dto;
    }
}
