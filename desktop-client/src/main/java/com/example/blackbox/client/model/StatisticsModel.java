package com.example.blackbox.client.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public class StatisticsModel {
    private long totalAgents;
    private long totalTasks;
    private long totalExecutions;
    private long successfulExecutions;
    private long failedExecutions;
    private long totalEvents;
    private long integrityFailures;
    private double averageDurationMs;
    private Map<String, Long> toolUsageCounts;
    private Map<String, Long> eventTypeCounts;
    private Map<String, Long> policyDecisionCounts;

    public StatisticsModel() {
    }

    public long getTotalAgents() { return totalAgents; }
    public void setTotalAgents(long totalAgents) { this.totalAgents = totalAgents; }
    public long getTotalTasks() { return totalTasks; }
    public void setTotalTasks(long totalTasks) { this.totalTasks = totalTasks; }
    public long getTotalExecutions() { return totalExecutions; }
    public void setTotalExecutions(long totalExecutions) { this.totalExecutions = totalExecutions; }
    public long getSuccessfulExecutions() { return successfulExecutions; }
    public void setSuccessfulExecutions(long successfulExecutions) { this.successfulExecutions = successfulExecutions; }
    public long getFailedExecutions() { return failedExecutions; }
    public void setFailedExecutions(long failedExecutions) { this.failedExecutions = failedExecutions; }
    public long getTotalEvents() { return totalEvents; }
    public void setTotalEvents(long totalEvents) { this.totalEvents = totalEvents; }
    public long getIntegrityFailures() { return integrityFailures; }
    public void setIntegrityFailures(long integrityFailures) { this.integrityFailures = integrityFailures; }
    public double getAverageDurationMs() { return averageDurationMs; }
    public void setAverageDurationMs(double averageDurationMs) { this.averageDurationMs = averageDurationMs; }
    public Map<String, Long> getToolUsageCounts() { return toolUsageCounts; }
    public void setToolUsageCounts(Map<String, Long> toolUsageCounts) { this.toolUsageCounts = toolUsageCounts; }
    public Map<String, Long> getEventTypeCounts() { return eventTypeCounts; }
    public void setEventTypeCounts(Map<String, Long> eventTypeCounts) { this.eventTypeCounts = eventTypeCounts; }
    public Map<String, Long> getPolicyDecisionCounts() { return policyDecisionCounts; }
    public void setPolicyDecisionCounts(Map<String, Long> policyDecisionCounts) { this.policyDecisionCounts = policyDecisionCounts; }
}
