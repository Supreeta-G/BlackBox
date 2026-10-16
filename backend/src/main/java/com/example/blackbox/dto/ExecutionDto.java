package com.example.blackbox.dto;

import com.example.blackbox.enums.ExecutionStatus;
import java.time.Instant;

public class ExecutionDto {
    private String id;
    private String taskId;
    private String agentId;
    private String correlationId;
    private String rootExecutionId;
    private String parentExecutionId;
    private ExecutionStatus status;
    private Instant startedAt;
    private Instant completedAt;
    private Long durationMs;
    private Integer eventCount;
    private String finalResult;
    private String finalHash;
    private String errorMessage;
    private Instant createdAt;

    public ExecutionDto() {
    }

    public ExecutionDto(String id, String taskId, String agentId, String correlationId, String rootExecutionId,
                        String parentExecutionId, ExecutionStatus status, Instant startedAt, Instant completedAt,
                        Long durationMs, Integer eventCount, String finalResult, String finalHash,
                        String errorMessage, Instant createdAt) {
        this.id = id;
        this.taskId = taskId;
        this.agentId = agentId;
        this.correlationId = correlationId;
        this.rootExecutionId = rootExecutionId;
        this.parentExecutionId = parentExecutionId;
        this.status = status;
        this.startedAt = startedAt;
        this.completedAt = completedAt;
        this.durationMs = durationMs;
        this.eventCount = eventCount;
        this.finalResult = finalResult;
        this.finalHash = finalHash;
        this.errorMessage = errorMessage;
        this.createdAt = createdAt;
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
    public ExecutionStatus getStatus() { return status; }
    public void setStatus(ExecutionStatus status) { this.status = status; }
    public Instant getStartedAt() { return startedAt; }
    public void setStartedAt(Instant startedAt) { this.startedAt = startedAt; }
    public Instant getCompletedAt() { return completedAt; }
    public void setCompletedAt(Instant completedAt) { this.completedAt = completedAt; }
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
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
