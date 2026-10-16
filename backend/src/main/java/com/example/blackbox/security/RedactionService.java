package com.example.blackbox.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class RedactionService {

    private final List<Pattern> compiledPatterns = new ArrayList<>();

    public RedactionService(@Value("${blackbox.security.redact-patterns:}") List<String> patterns) {
        if (patterns != null && !patterns.isEmpty()) {
            for (String p : patterns) {
                try {
                    compiledPatterns.add(Pattern.compile(p, Pattern.CASE_INSENSITIVE));
                } catch (Exception ignored) {
                }
            }
        }
        // Fallback default patterns for credential safety
        if (compiledPatterns.isEmpty()) {
            compiledPatterns.add(Pattern.compile("(?i)password[=:]\\s*['\"]?([^'\"\\s,;]+)['\"]?", Pattern.CASE_INSENSITIVE));
            compiledPatterns.add(Pattern.compile("(?i)secret[=:]\\s*['\"]?([^'\"\\s,;]+)['\"]?", Pattern.CASE_INSENSITIVE));
            compiledPatterns.add(Pattern.compile("(?i)token[=:]\\s*['\"]?([^'\"\\s,;]+)['\"]?", Pattern.CASE_INSENSITIVE));
            compiledPatterns.add(Pattern.compile("(?i)bearer\\s+[A-Za-z0-9._~+/-]+", Pattern.CASE_INSENSITIVE));
            compiledPatterns.add(Pattern.compile("(?i)api[_-]?key[=:]\\s*['\"]?([^'\"\\s,;]+)['\"]?", Pattern.CASE_INSENSITIVE));
        }
    }

    public String sanitize(String input) {
        if (input == null || input.isBlank()) {
            return input;
        }

        String result = input;
        for (Pattern pattern : compiledPatterns) {
            Matcher matcher = pattern.matcher(result);
            if (matcher.find()) {
                result = matcher.replaceAll(mr -> {
                    String match = mr.group();
                    if (match.toLowerCase().startsWith("bearer ")) {
                        return "Bearer [REDACTED]";
                    }
                    int eqIndex = match.indexOf('=');
                    if (eqIndex == -1) {
                        eqIndex = match.indexOf(':');
                    }
                    if (eqIndex != -1) {
                        return match.substring(0, eqIndex + 1) + "[REDACTED]";
                    }
                    return "[REDACTED]";
                });
            }
        }
        return result;
    }
}
