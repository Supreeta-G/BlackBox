package com.example.blackbox.dto;

import com.example.blackbox.enums.AgentRole;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class CreateAgentRequest {

    @NotBlank(message = "Agent name is required")
    private String name;

    @NotNull(message = "Agent role is required")
    private AgentRole role;

    private String modelIdentifier;

    public CreateAgentRequest() {
    }

    public CreateAgentRequest(String name, AgentRole role, String modelIdentifier) {
        this.name = name;
        this.role = role;
        this.modelIdentifier = modelIdentifier;
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public AgentRole getRole() { return role; }
    public void setRole(AgentRole role) { this.role = role; }
    public String getModelIdentifier() { return modelIdentifier; }
    public void setModelIdentifier(String modelIdentifier) { this.modelIdentifier = modelIdentifier; }
}
