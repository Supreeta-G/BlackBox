package com.example.blackbox.entity;

import com.example.blackbox.enums.AgentRole;
import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "agents")
public class AgentEntity {

    @Id
    @Column(length = 36)
    private String id;

    @Column(nullable = false, length = 100)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private AgentRole role;

    @Column(name = "model_identifier", nullable = false, length = 100)
    private String modelIdentifier;

    @Column(name = "configuration_hash", nullable = false, length = 64)
    private String configurationHash;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    public AgentEntity() {
    }

    public AgentEntity(String id, String name, AgentRole role, String modelIdentifier, String configurationHash, Instant createdAt) {
        this.id = id;
        this.name = name;
        this.role = role;
        this.modelIdentifier = modelIdentifier;
        this.configurationHash = configurationHash;
        this.createdAt = createdAt;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public AgentRole getRole() {
        return role;
    }

    public void setRole(AgentRole role) {
        this.role = role;
    }

    public String getModelIdentifier() {
        return modelIdentifier;
    }

    public void setModelIdentifier(String modelIdentifier) {
        this.modelIdentifier = modelIdentifier;
    }

    public String getConfigurationHash() {
        return configurationHash;
    }

    public void setConfigurationHash(String configurationHash) {
        this.configurationHash = configurationHash;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}
