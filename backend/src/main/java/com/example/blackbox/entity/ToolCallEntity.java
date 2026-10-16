package com.example.blackbox.entity;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "tool_calls")
public class ToolCallEntity {

    @Id
    @Column(length = 36)
    private String id;

    @Column(name = "execution_id", nullable = false, length = 36)
    private String executionId;

    @Column(name = "event_id", nullable = false, length = 36)
    private String eventId;

    @Column(name = "tool_name", nullable = false, length = 100)
    private String toolName;

    @Column(name = "sanitized_input", columnDefinition = "TEXT")
    private String sanitizedInput;

    @Column(name = "sanitized_output", columnDefinition = "TEXT")
    private String sanitizedOutput;

    @Column(nullable = false, length = 50)
    private String status;

    @Column(name = "duration_ms")
    private Long durationMs;

    @Column(name = "started_at", nullable = false)
    private Instant startedAt;

    @Column(name = "completed_at")
    private Instant completedAt;

    public ToolCallEntity() {
    }

    public ToolCallEntity(String id, String executionId, String eventId, String toolName, String sanitizedInput,
                          String sanitizedOutput, String status, Long durationMs, Instant startedAt, Instant completedAt) {
        this.id = id;
        this.executionId = executionId;
        this.eventId = eventId;
        this.toolName = toolName;
        this.sanitizedInput = sanitizedInput;
        this.sanitizedOutput = sanitizedOutput;
        this.status = status;
        this.durationMs = durationMs;
        this.startedAt = startedAt;
        this.completedAt = completedAt;
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

    public String getEventId() {
        return eventId;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
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
}
