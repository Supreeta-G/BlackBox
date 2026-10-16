package com.example.blackbox.entity;

import com.example.blackbox.enums.EventType;
import com.example.blackbox.enums.PolicyResult;
import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "agent_events", uniqueConstraints = {
    @UniqueConstraint(name = "uk_exec_seq", columnNames = {"execution_id", "sequence_number"})
})
public class AgentEventEntity {

    @Id
    @Column(length = 36)
    private String id;

    @Column(name = "execution_id", nullable = false, length = 36)
    private String executionId;

    @Column(name = "task_id", nullable = false, length = 36)
    private String taskId;

    @Column(name = "agent_id", nullable = false, length = 36)
    private String agentId;

    @Column(name = "parent_execution_id", length = 36)
    private String parentExecutionId;

    @Column(name = "correlation_id", nullable = false, length = 64)
    private String correlationId;

    @Enumerated(EnumType.STRING)
    @Column(name = "event_type", nullable = false, length = 50)
    private EventType eventType;

    @Column(nullable = false)
    private Instant timestamp;

    @Column(name = "sequence_number", nullable = false)
    private Integer sequenceNumber;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    @Column(name = "tool_name", length = 100)
    private String toolName;

    @Column(name = "sanitized_input", columnDefinition = "TEXT")
    private String sanitizedInput;

    @Column(name = "sanitized_output", columnDefinition = "TEXT")
    private String sanitizedOutput;

    @Column(nullable = false, length = 50)
    private String status;

    @Column(name = "duration_ms")
    private Long durationMs;

    @Column(name = "error_details", columnDefinition = "TEXT")
    private String errorDetails;

    @Enumerated(EnumType.STRING)
    @Column(name = "policy_result", length = 50)
    private PolicyResult policyResult;

    @Column(name = "policy_reason", columnDefinition = "TEXT")
    private String policyReason;

    @Column(name = "evidence_reference", length = 255)
    private String evidenceReference;

    @Column(name = "content_hash", length = 64)
    private String contentHash;

    @Column(name = "previous_event_hash", nullable = false, length = 64)
    private String previousEventHash;

    @Column(name = "current_event_hash", nullable = false, length = 64)
    private String currentEventHash;

    @Column(name = "trace_id", length = 64)
    private String traceId;

    @Column(name = "span_id", length = 64)
    private String spanId;

    @Column(name = "is_tampered")
    private Boolean isTampered = false;

    public AgentEventEntity() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getExecutionId() {
        return executionId;
    }

    public void setExecutionId(String executionId) {
        this.executionId = executionId;
    }

    public String getTaskId() {
        return taskId;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }

    public String getAgentId() {
        return agentId;
    }

    public void setAgentId(String agentId) {
        this.agentId = agentId;
    }

    public String getParentExecutionId() {
        return parentExecutionId;
    }

    public void setParentExecutionId(String parentExecutionId) {
        this.parentExecutionId = parentExecutionId;
    }

    public String getCorrelationId() {
        return correlationId;
    }

    public void setCorrelationId(String correlationId) {
        this.correlationId = correlationId;
    }

    public EventType getEventType() {
        return eventType;
    }

    public void setEventType(EventType eventType) {
        this.eventType = eventType;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }

    public Integer getSequenceNumber() {
        return sequenceNumber;
    }

    public void setSequenceNumber(Integer sequenceNumber) {
        this.sequenceNumber = sequenceNumber;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getToolName() {
        return toolName;
    }

    public void setToolName(String toolName) {
        this.toolName = toolName;
    }

    public String getSanitizedInput() {
        return sanitizedInput;
    }

    public void setSanitizedInput(String sanitizedInput) {
        this.sanitizedInput = sanitizedInput;
    }

    public String getSanitizedOutput() {
        return sanitizedOutput;
    }

    public void setSanitizedOutput(String sanitizedOutput) {
        this.sanitizedOutput = sanitizedOutput;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Long getDurationMs() {
        return durationMs;
    }

    public void setDurationMs(Long durationMs) {
        this.durationMs = durationMs;
    }

    public String getErrorDetails() {
        return errorDetails;
    }

    public void setErrorDetails(String errorDetails) {
        this.errorDetails = errorDetails;
    }

    public PolicyResult getPolicyResult() {
        return policyResult;
    }

    public void setPolicyResult(PolicyResult policyResult) {
        this.policyResult = policyResult;
    }

    public String getPolicyReason() {
        return policyReason;
    }

    public void setPolicyReason(String policyReason) {
        this.policyReason = policyReason;
    }

    public String getEvidenceReference() {
        return evidenceReference;
    }

    public void setEvidenceReference(String evidenceReference) {
        this.evidenceReference = evidenceReference;
    }

    public String getContentHash() {
        return contentHash;
    }

    public void setContentHash(String contentHash) {
        this.contentHash = contentHash;
    }

    public String getPreviousEventHash() {
        return previousEventHash;
    }

    public void setPreviousEventHash(String previousEventHash) {
        this.previousEventHash = previousEventHash;
    }

    public String getCurrentEventHash() {
        return currentEventHash;
    }

    public void setCurrentEventHash(String currentEventHash) {
        this.currentEventHash = currentEventHash;
    }

    public String getTraceId() {
        return traceId;
    }

    public void setTraceId(String traceId) {
        this.traceId = traceId;
    }

    public String getSpanId() {
        return spanId;
    }

    public void setSpanId(String spanId) {
        this.spanId = spanId;
    }

    public Boolean getIsTampered() {
        return isTampered;
    }

    public void setIsTampered(Boolean isTampered) {
        this.isTampered = isTampered;
    }
}
