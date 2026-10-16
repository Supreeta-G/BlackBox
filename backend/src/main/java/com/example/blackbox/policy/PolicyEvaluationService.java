package com.example.blackbox.policy;

import com.example.blackbox.entity.PolicyDecisionEntity;
import com.example.blackbox.enums.PolicyResult;
import com.example.blackbox.repository.PolicyDecisionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Service
public class PolicyEvaluationService {

    private static final Logger log = LoggerFactory.getLogger(PolicyEvaluationService.class);
    private final PolicyDecisionRepository policyDecisionRepository;

    public PolicyEvaluationService(PolicyDecisionRepository policyDecisionRepository) {
        this.policyDecisionRepository = policyDecisionRepository;
    }

    public static class EvaluationResult {
        private final PolicyResult decision;
        private final String reason;

        public EvaluationResult(PolicyResult decision, String reason) {
            this.decision = decision;
            this.reason = reason;
        }

        public PolicyResult getDecision() { return decision; }
        public String getReason() { return reason; }
    }

    /**
     * Deterministic policy evaluation.
     */
    public EvaluationResult evaluate(String actionType, String targetResource, Map<String, Object> parameters) {
        String actionLower = actionType != null ? actionType.toLowerCase() : "";
        String resourceLower = targetResource != null ? targetResource.toLowerCase() : "";

        // Rule 1: Prohibited destructive or administrative commands
        if (actionLower.contains("system_exec") ||
            actionLower.contains("delete_database") ||
            resourceLower.contains("prohibited_") ||
            resourceLower.contains("drop table") ||
            resourceLower.contains("rm -rf")) {
            return new EvaluationResult(
                    PolicyResult.DENIED,
                    "Security Rule POL-SEC-01: Execution of destructive commands and unauthorized resource access is strictly prohibited."
            );
        }

        // Rule 2: Operations requiring manual human authorization
        if (actionLower.contains("export_bulk_data") ||
            actionLower.contains("elevate_privilege") ||
            resourceLower.contains("confidential_export")) {
            return new EvaluationResult(
                    PolicyResult.REQUIRES_APPROVAL,
                    "Compliance Rule POL-CMP-02: Sensitive bulk data export requires dual-operator cryptographic approval."
            );
        }

        // Rule 3: Standard safe operations
        return new EvaluationResult(
                PolicyResult.ALLOWED,
                "Standard Rule POL-STD-00: Tool invocation authorized under standard security boundary."
        );
    }

    /**
     * Records a policy decision linked to an execution and event.
     */
    public PolicyDecisionEntity recordDecision(String executionId, String eventId, String actionType,
                                                String targetResource, PolicyResult decision, String reason) {
        PolicyDecisionEntity entity = new PolicyDecisionEntity(
                UUID.randomUUID().toString(),
                executionId,
                eventId,
                actionType,
                targetResource,
                decision,
                reason,
                Instant.now()
        );
        return policyDecisionRepository.save(entity);
    }
}
