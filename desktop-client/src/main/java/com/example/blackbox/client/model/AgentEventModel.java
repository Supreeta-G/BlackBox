package com.example.blackbox.client.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class AgentEventModel {
    private String id;
    private String executionId;
    private String taskId;
    private String agentId;
    private String parentExecutionId;
    private String correlationId;
    private String eventType;
    private String timestamp;
    private Integer sequenceNumber;
    private String description;
    private String toolName;
    private String sanitizedInput;
    private String sanitizedOutput;
    private String status;
    private Long durationMs;
    private String errorDetails;
    private String policyResult;
    private String policyReason;
    private String evidenceReference;
    private String contentHash;
    private String previousEventHash;
    private String currentEventHash;
    private String traceId;
    private String spanId;
    private Boolean isTampered;

    public AgentEventModel() {
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getExecutionId() { return executionId; }
    public void setExecutionId(String executionId) { this.executionId = executionId; }
    public String getTaskId() { return taskId; }
    public void setTaskId(String taskId) { this.taskId = taskId; }
    public String getAgentId() { return agentId; }
    public void setAgentId(String agentId) { this.agentId = agentId; }
    public String getParentExecutionId() { return parentExecutionId; }
    public void setParentExecutionId(String parentExecutionId) { this.parentExecutionId = parentExecutionId; }
    public String getCorrelationId() { return correlationId; }
    public void setCorrelationId(String correlationId) { this.correlationId = correlationId; }
    public String getEventType() { return eventType; }
    public void setEventType(String eventType) { this.eventType = eventType; }
    public String getTimestamp() { return timestamp; }
    public void setTimestamp(String timestamp) { this.timestamp = timestamp; }
    public Integer getSequenceNumber() { return sequenceNumber; }
    public void setSequenceNumber(Integer sequenceNumber) { this.sequenceNumber = sequenceNumber; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getToolName() { return toolName; }
    public void setToolName(String toolName) { this.toolName = toolName; }
    public String getSanitizedInput() { return sanitizedInput; }
    public void setSanitizedInput(String sanitizedInput) { this.sanitizedInput = sanitizedInput; }
    public String getSanitizedOutput() { return sanitizedOutput; }
    public void setSanitizedOutput(String sanitizedOutput) { this.sanitizedOutput = sanitizedOutput; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Long getDurationMs() { return durationMs; }
    public void setDurationMs(Long durationMs) { this.durationMs = durationMs; }
    public String getErrorDetails() { return errorDetails; }
    public void setErrorDetails(String errorDetails) { this.errorDetails = errorDetails; }
    public String getPolicyResult() { return policyResult; }
    public void setPolicyResult(String policyResult) { this.policyResult = policyResult; }
    public String getPolicyReason() { return policyReason; }
    public void setPolicyReason(String policyReason) { this.policyReason = policyReason; }
    public String getEvidenceReference() { return evidenceReference; }
    public void setEvidenceReference(String evidenceReference) { this.evidenceReference = evidenceReference; }
    public String getContentHash() { return contentHash; }
    public void setContentHash(String contentHash) { this.contentHash = contentHash; }
    public String getPreviousEventHash() { return previousEventHash; }
    public void setPreviousEventHash(String previousEventHash) { this.previousEventHash = previousEventHash; }
    public String getCurrentEventHash() { return currentEventHash; }
    public void setCurrentEventHash(String currentEventHash) { this.currentEventHash = currentEventHash; }
    public String getTraceId() { return traceId; }
    public void setTraceId(String traceId) { this.traceId = traceId; }
    public String getSpanId() { return spanId; }
    public void setSpanId(String spanId) { this.spanId = spanId; }
    public Boolean getIsTampered() { return isTampered; }
    public void setIsTampered(Boolean isTampered) { this.isTampered = isTampered; }
}
