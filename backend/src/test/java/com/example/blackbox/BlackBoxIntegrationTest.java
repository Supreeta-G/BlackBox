package com.example.blackbox;

import com.example.blackbox.dto.CreateAgentRequest;
import com.example.blackbox.dto.CreateTaskRequest;
import com.example.blackbox.enums.AgentRole;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class BlackBoxIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("REST API: Retrieve agents list")
    void testGetAgents() throws Exception {
        mockMvc.perform(get("/api/agents"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()", greaterThanOrEqualTo(1)));
    }

    @Test
    @DisplayName("REST API: Create agent and create task")
    void testCreateAgentAndTask() throws Exception {
        CreateAgentRequest agentReq = new CreateAgentRequest("AuditorAgent", AgentRole.RESEARCH_AGENT, "gpt-4o");
        String agentRes = mockMvc.perform(post("/api/agents")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(agentReq)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("AuditorAgent"))
                .andReturn().getResponse().getContentAsString();

        String agentId = objectMapper.readTree(agentRes).get("id").asText();

        CreateTaskRequest taskReq = new CreateTaskRequest("Flight Log Audit", "Inspect sensor checksums", agentId);
        mockMvc.perform(post("/api/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(taskReq)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("Flight Log Audit"));
    }

    @Test
    @DisplayName("REST API: Retrieve statistics")
    void testGetStatistics() throws Exception {
        mockMvc.perform(get("/api/statistics"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalAgents", greaterThanOrEqualTo(1)));
    }

    @Test
    @DisplayName("REST API: Trigger Demo Scenario and Tampering Demo")
    void testDemoScenarioAndTampering() throws Exception {
        // Trigger standard demo scenario
        String execRes = mockMvc.perform(post("/api/demo/run-scenario")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"scenario\":\"STANDARD\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("COMPLETED"))
                .andReturn().getResponse().getContentAsString();

        String execId = objectMapper.readTree(execRes).get("id").asText();

        // Verify initial integrity
        mockMvc.perform(get("/api/executions/" + execId + "/verify-integrity"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("INTEGRITY_VERIFIED"));

        // Tamper with event at sequence 2 via dev endpoint
        mockMvc.perform(post("/api/dev/executions/" + execId + "/tamper")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"sequenceNumber\": 2, \"modifiedDescription\": \"TAMPERED_IN_TEST\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("TAMPERED"));

        // Verify integrity is now COMPROMISED!
        mockMvc.perform(get("/api/executions/" + execId + "/verify-integrity"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("INTEGRITY_COMPROMISED"))
                .andExpect(jsonPath("$.firstInvalidSequenceNumber").value(2));
    }
}
