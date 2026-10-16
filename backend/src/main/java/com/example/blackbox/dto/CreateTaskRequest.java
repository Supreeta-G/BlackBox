package com.example.blackbox.dto;

import jakarta.validation.constraints.NotBlank;

public class CreateTaskRequest {

    @NotBlank(message = "Task title is required")
    private String title;

    @NotBlank(message = "Task description is required")
    private String description;

    @NotBlank(message = "Agent ID is required")
    private String agentId;

    public CreateTaskRequest() {
    }

    public CreateTaskRequest(String title, String description, String agentId) {
        this.title = title;
        this.description = description;
        this.agentId = agentId;
    }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getAgentId() { return agentId; }
    public void setAgentId(String agentId) { this.agentId = agentId; }
}
