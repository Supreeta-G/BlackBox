package com.example.blackbox.client.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class AgentModel {
    private String id;
    private String name;
    private String role;
    private String modelIdentifier;
    private String configurationHash;
    private String createdAt;

    public AgentModel() {
    }

    public AgentModel(String id, String name, String role, String modelIdentifier, String configurationHash, String createdAt) {
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
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
    public String getModelIdentifier() { return modelIdentifier; }
    public void setModelIdentifier(String modelIdentifier) { this.modelIdentifier = modelIdentifier; }
    public String getConfigurationHash() { return configurationHash; }
    public void setConfigurationHash(String configurationHash) { this.configurationHash = configurationHash; }
    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

    @Override
    public String toString() {
        return name + " (" + role + ")";
    }
}
