package com.example.blackbox.client;

import com.example.blackbox.client.model.AgentModel;
import com.example.blackbox.client.model.ExecutionModel;
import com.example.blackbox.client.model.TaskModel;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ApiClientTest {

    private final ObjectMapper mapper = new ObjectMapper();

    @Test
    @DisplayName("Should deserialize AgentModel properly from JSON")
    void testAgentModelDeserialization() throws Exception {
        String json = "{\"id\":\"agent-123\",\"name\":\"ResearchAgent\",\"role\":\"RESEARCH_AGENT\",\"modelIdentifier\":\"gpt-4o\",\"configurationHash\":\"abc123hash\",\"createdAt\":\"2026-10-11T00:00:00Z\"}";
        AgentModel agent = mapper.readValue(json, AgentModel.class);

        assertEquals("agent-123", agent.getId());
        assertEquals("ResearchAgent", agent.getName());
        assertEquals("RESEARCH_AGENT", agent.getRole());
        assertEquals("gpt-4o", agent.getModelIdentifier());
    }

    @Test
    @DisplayName("Should deserialize TaskModel properly from JSON")
    void testTaskModelDeserialization() throws Exception {
        String json = "{\"id\":\"task-456\",\"title\":\"Investigate Anomaly\",\"description\":\"Audit telemetry logs\",\"agentId\":\"agent-123\",\"status\":\"READY\",\"createdAt\":\"2026-10-11T00:00:00Z\"}";
        TaskModel task = mapper.readValue(json, TaskModel.class);

        assertEquals("task-456", task.getId());
        assertEquals("Investigate Anomaly", task.getTitle());
        assertEquals("READY", task.getStatus());
    }

    @Test
    @DisplayName("Should deserialize ExecutionModel properly from JSON")
    void testExecutionModelDeserialization() throws Exception {
        String json = "{\"id\":\"exec-789\",\"taskId\":\"task-456\",\"agentId\":\"agent-123\",\"correlationId\":\"corr-999\",\"status\":\"COMPLETED\",\"durationMs\":150,\"eventCount\":8,\"finalHash\":\"hash789\"}";
        ExecutionModel exec = mapper.readValue(json, ExecutionModel.class);

        assertEquals("exec-789", exec.getId());
        assertEquals("COMPLETED", exec.getStatus());
        assertEquals(150L, exec.getDurationMs());
        assertEquals(8, exec.getEventCount());
    }
}
