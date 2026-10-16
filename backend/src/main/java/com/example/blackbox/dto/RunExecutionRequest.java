package com.example.blackbox.dto;

public class RunExecutionRequest {
    private String scenario; // STANDARD, FAILURE_RETRY, DELEGATION, POLICY_DENIED
    private String customPrompt;

    public RunExecutionRequest() {
        this.scenario = "STANDARD";
    }

    public RunExecutionRequest(String scenario, String customPrompt) {
        this.scenario = scenario;
        this.customPrompt = customPrompt;
    }

    public String getScenario() { return scenario; }
    public void setScenario(String scenario) { this.scenario = scenario; }
    public String getCustomPrompt() { return customPrompt; }
    public void setCustomPrompt(String customPrompt) { this.customPrompt = customPrompt; }
}
