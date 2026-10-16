package com.example.blackbox.client.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ExecutionModel {
    private String id;
    private String taskId;
    private String agentId;
    private String correlationId;
    private String rootExecutionId;
    private String parentExecutionId;
    private String status;
    private String startedAt;
    private String completedAt;
    private Long durationMs;
    private Integer eventCount;
    private String finalResult;
    private String finalHash;
    private String errorMessage;
    private String createdAt;

    public ExecutionModel() {
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getTaskId() { return taskId; }
    public void setTaskId(String taskId) { this.taskId = taskId; }
    public String getAgentId() { return agentId; }
    public void setAgentId(String agentId) { this.agentId = agentId; }
    public String getCorrelationId() { return correlationId; }
    public void setCorrelationId(String correlationId) { this.correlationId = correlationId; }
    public String getRootExecutionId() { return rootExecutionId; }
    public void setRootExecutionId(String rootExecutionId) { this.rootExecutionId = rootExecutionId; }
    public String getParentExecutionId() { return parentExecutionId; }
    public void setParentExecutionId(String parentExecutionId) { this.parentExecutionId = parentExecutionId; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getStartedAt() { return startedAt; }
    public void setStartedAt(String startedAt) { this.startedAt = startedAt; }
    public String getCompletedAt() { return completedAt; }
    public void setCompletedAt(String completedAt) { this.completedAt = completedAt; }
    public Long getDurationMs() { return durationMs; }
    public void setDurationMs(Long durationMs) { this.durationMs = durationMs; }
    public Integer getEventCount() { return eventCount; }
    public void setEventCount(Integer eventCount) { this.eventCount = eventCount; }
    public String getFinalResult() { return finalResult; }
    public void setFinalResult(String finalResult) { this.finalResult = finalResult; }
    public String getFinalHash() { return finalHash; }
    public void setFinalHash(String finalHash) { this.finalHash = finalHash; }
    public String getErrorMessage() { return errorMessage; }
    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

    @Override
    public String toString() {
        return id + " [" + status + "]";
    }
}
