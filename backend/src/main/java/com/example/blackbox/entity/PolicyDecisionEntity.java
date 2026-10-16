package com.example.blackbox.entity;

import com.example.blackbox.enums.PolicyResult;
import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "policy_decisions")
public class PolicyDecisionEntity {

    @Id
    @Column(length = 36)
    private String id;

    @Column(name = "execution_id", nullable = false, length = 36)
    private String executionId;

    @Column(name = "event_id", nullable = false, length = 36)
    private String eventId;

    @Column(name = "action_type", nullable = false, length = 100)
    private String actionType;

    @Column(name = "target_resource", length = 255)
    private String targetResource;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private PolicyResult decision;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String reason;

    @Column(name = "evaluated_at", nullable = false)
    private Instant evaluatedAt;

    public PolicyDecisionEntity() {
    }

    public PolicyDecisionEntity(String id, String executionId, String eventId, String actionType,
                                String targetResource, PolicyResult decision, String reason, Instant evaluatedAt) {
        this.id = id;
        this.executionId = executionId;
        this.eventId = eventId;
        this.actionType = actionType;
        this.targetResource = targetResource;
        this.decision = decision;
        this.reason = reason;
        this.evaluatedAt = evaluatedAt;
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

    public String getActionType() {
        return actionType;
    }

    public void setActionType(String actionType) {
        this.actionType = actionType;
    }

    public String getTargetResource() {
        return targetResource;
    }

    public void setTargetResource(String targetResource) {
        this.targetResource = targetResource;
    }

    public PolicyResult getDecision() {
        return decision;
    }

    public void setDecision(PolicyResult decision) {
        this.decision = decision;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public Instant getEvaluatedAt() {
        return evaluatedAt;
    }

    public void setEvaluatedAt(Instant evaluatedAt) {
        this.evaluatedAt = evaluatedAt;
    }
}
