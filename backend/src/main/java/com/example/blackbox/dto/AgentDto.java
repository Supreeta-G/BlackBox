package com.example.blackbox.dto;

import com.example.blackbox.enums.AgentRole;
import java.time.Instant;

public class AgentDto {
    private String id;
    private String name;
    private AgentRole role;
    private String modelIdentifier;
    private String configurationHash;
    private Instant createdAt;

    public AgentDto() {
    }

    public AgentDto(String id, String name, AgentRole role, String modelIdentifier, String configurationHash, Instant createdAt) {
        this.id = id;
        this.name = name;
        this.role = role;
        this.modelIdentifier = modelIdentifier;
        this.configurationHash = configurationHash;
        this.createdAt = createdAt;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public AgentRole getRole() { return role; }
    public void setRole(AgentRole role) { this.role = role; }
    public String getModelIdentifier() { return modelIdentifier; }
    public void setModelIdentifier(String modelIdentifier) { this.modelIdentifier = modelIdentifier; }
    public String getConfigurationHash() { return configurationHash; }
    public void setConfigurationHash(String configurationHash) { this.configurationHash = configurationHash; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
