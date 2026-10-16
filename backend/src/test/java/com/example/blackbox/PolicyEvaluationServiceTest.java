package com.example.blackbox;

import com.example.blackbox.enums.PolicyResult;
import com.example.blackbox.policy.PolicyEvaluationService;
import com.example.blackbox.repository.PolicyDecisionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(MockitoExtension.class)
class PolicyEvaluationServiceTest {

    @Mock
    private PolicyDecisionRepository policyDecisionRepository;

    private PolicyEvaluationService policyService;

    @BeforeEach
    void setUp() {
        policyService = new PolicyEvaluationService(policyDecisionRepository);
    }

    @Test
    @DisplayName("Safe operations must be ALLOWED")
    void testSafeOperationAllowed() {
        var res = policyService.evaluate("TOOL_INVOCATION", "CalculatorTool", Map.of("expr", "2+2"));
        assertEquals(PolicyResult.ALLOWED, res.getDecision());
        assertTrue(res.getReason().contains("POL-STD-00"));
    }

    @Test
    @DisplayName("Destructive system operations must be DENIED")
    void testDestructiveActionDenied() {
        var res1 = policyService.evaluate("system_exec", "rm -rf /", Map.of());
        assertEquals(PolicyResult.DENIED, res1.getDecision());

        var res2 = policyService.evaluate("delete_database", "postgres_core", Map.of());
        assertEquals(PolicyResult.DENIED, res2.getDecision());

        var res3 = policyService.evaluate("query", "DROP TABLE users", Map.of());
        assertEquals(PolicyResult.DENIED, res3.getDecision());
    }

    @Test
    @DisplayName("Bulk export operations must require approval")
    void testBulkExportRequiresApproval() {
        var res = policyService.evaluate("export_bulk_data", "all_user_records", Map.of());
        assertEquals(PolicyResult.REQUIRES_APPROVAL, res.getDecision());
    }
}
