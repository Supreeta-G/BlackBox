package com.example.blackbox.tools;

import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class InformationSearchTool implements AgentTool {

    @Override
    public String getName() {
        return "InformationSearchTool";
    }

    @Override
    public String getDescription() {
        return "Searches domain knowledge repositories, black box incident archives, and technical specifications.";
    }

    @Override
    public ToolExecutionResult execute(Map<String, Object> params) {
        long start = System.currentTimeMillis();
        String query = params != null && params.get("query") != null ? params.get("query").toString() : "";

        // Simulated network delay
        try {
            Thread.sleep(40);
        } catch (InterruptedException ignored) {}

        if (query.toLowerCase().contains("fail_search")) {
            return ToolExecutionResult.failure("External Knowledge Graph API timed out after 3000ms", System.currentTimeMillis() - start);
        }

        String result;
        if (query.toLowerCase().contains("hash") || query.toLowerCase().contains("integrity")) {
            result = "Found 3 references: 1) SHA-256 canonical hashing ensures tamper-evidence; 2) Genesis block starts with 64 zero characters; 3) Chain pointers link sequential blocks.";
        } else if (query.toLowerCase().contains("flight") || query.toLowerCase().contains("black box")) {
            result = "Found 2 records: Standard FAA/ICAO flight recorders store cockpit voice and FDR telemetry in crash-survivable enclosures with continuous integrity logging.";
        } else {
            result = "Search results for '" + query + "': Verified 4 factual sources matching operational context. All integrity assertions confirmed.";
        }

        return ToolExecutionResult.success(result, System.currentTimeMillis() - start);
    }
}
