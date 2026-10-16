package com.example.blackbox;

import com.example.blackbox.security.RedactionService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RedactionServiceTest {

    private final RedactionService redactionService = new RedactionService(List.of());

    @Test
    @DisplayName("Should redact passwords, API keys, and bearer tokens")
    void testSensitiveDataRedaction() {
        String raw = "Connecting to host with password=SuperSecretPassword123 and token=xyz987654321; Authorization: Bearer eyJhbGciOiJIUzI1Ni";
        String sanitized = redactionService.sanitize(raw);

        assertFalse(sanitized.contains("SuperSecretPassword123"), "Cleartext password must not be preserved");
        assertFalse(sanitized.contains("xyz987654321"), "Cleartext token must not be preserved");
        assertTrue(sanitized.contains("[REDACTED]"), "Redacted placeholder must be present");
        assertTrue(sanitized.contains("Bearer [REDACTED]"), "Bearer tokens must be redacted");
    }
}
