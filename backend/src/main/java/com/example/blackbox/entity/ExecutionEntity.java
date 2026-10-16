package com.example.blackbox.entity;

import com.example.blackbox.enums.ExecutionStatus;
import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "executions")
public class ExecutionEntity {

    @Id
    @Column(length = 36)
    private String id;

    @Column(name = "task_id", nullable = false, length = 36)
    private String taskId;

    @Column(name = "agent_id", nullable = false, length = 36)
    private String agentId;

    @Column(name = "correlation_id", nullable = false, length = 64)
    private String correlationId;

    @Column(name = "root_execution_id", nullable = false, length = 36)
    private String rootExecutionId;

    @Column(name = "parent_execution_id", length = 36)
    private String parentExecutionId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private ExecutionStatus status;

    @Column(name = "started_at", nullable = false)
    private Instant startedAt;

    @Column(name = "completed_at")
    private Instant completedAt;

    @Column(name = "duration_ms")
    private Long durationMs;

    @Column(name = "event_count")
    private Integer eventCount = 0;

    @Column(name = "final_result", columnDefinition = "TEXT")
    private String finalResult;

    @Column(name = "final_hash", length = 64)
    private String finalHash;

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    public ExecutionEntity() {
    }

    public ExecutionEntity(String id, String taskId, String agentId, String correlationId, String rootExecutionId,
                           String parentExecutionId, ExecutionStatus status, Instant startedAt, Instant createdAt) {
        this.id = id;
        this.taskId = taskId;
        this.agentId = agentId;
        this.correlationId = correlationId;
        this.rootExecutionId = rootExecutionId;
        this.parentExecutionId = parentExecutionId;
        this.status = status;
        this.startedAt = startedAt;
        this.createdAt = createdAt;
        this.eventCount = 0;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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

    public String getCorrelationId() {
        return correlationId;
    }

    public void setCorrelationId(String correlationId) {
        this.correlationId = correlationId;
    }

    public String getRootExecutionId() {
        return rootExecutionId;
    }

    public void setRootExecutionId(String rootExecutionId) {
        this.rootExecutionId = rootExecutionId;
    }

    public String getParentExecutionId() {
        return parentExecutionId;
    }

    public void setParentExecutionId(String parentExecutionId) {
        this.parentExecutionId = parentExecutionId;
    }

    public ExecutionStatus getStatus() {
        return status;
    }

    public void setStatus(ExecutionStatus status) {
        this.status = status;
    }

    public Instant getStartedAt() {
        return startedAt;
    }

    public void setStartedAt(Instant startedAt) {
        this.startedAt = startedAt;
    }

    public Instant getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(Instant completedAt) {
        this.completedAt = completedAt;
    }

    public Long getDurationMs() {
        return durationMs;
    }

    public void setDurationMs(Long durationMs) {
        this.durationMs = durationMs;
    }

    public Integer getEventCount() {
        return eventCount;
    }

    public void setEventCount(Integer eventCount) {
        this.eventCount = eventCount;
    }

    public String getFinalResult() {
        return finalResult;
    }

    public void setFinalResult(String finalResult) {
        this.finalResult = finalResult;
    }

    public String getFinalHash() {
        return finalHash;
    }

    public void setFinalHash(String finalHash) {
        this.finalHash = finalHash;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}
