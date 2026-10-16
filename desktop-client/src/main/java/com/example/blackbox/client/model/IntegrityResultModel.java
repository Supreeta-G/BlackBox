package com.example.blackbox.client.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class IntegrityResultModel {
    private String status;
    private String executionId;
    private int totalEventsVerified;
    private Integer firstInvalidSequenceNumber;
    private String firstInvalidEventId;
    private String expectedHash;
    private String actualHash;
    private String reason;
    private String details;
    private int subsequentInvalidEvents;

    public IntegrityResultModel() {
    }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getExecutionId() { return executionId; }
    public void setExecutionId(String executionId) { this.executionId = executionId; }
    public int getTotalEventsVerified() { return totalEventsVerified; }
    public void setTotalEventsVerified(int totalEventsVerified) { this.totalEventsVerified = totalEventsVerified; }
    public Integer getFirstInvalidSequenceNumber() { return firstInvalidSequenceNumber; }
    public void setFirstInvalidSequenceNumber(Integer firstInvalidSequenceNumber) { this.firstInvalidSequenceNumber = firstInvalidSequenceNumber; }
    public String getFirstInvalidEventId() { return firstInvalidEventId; }
    public void setFirstInvalidEventId(String firstInvalidEventId) { this.firstInvalidEventId = firstInvalidEventId; }
    public String getExpectedHash() { return expectedHash; }
    public void setExpectedHash(String expectedHash) { this.expectedHash = expectedHash; }
    public String getActualHash() { return actualHash; }
    public void setActualHash(String actualHash) { this.actualHash = actualHash; }
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
    public String getDetails() { return details; }
    public void setDetails(String details) { this.details = details; }
    public int getSubsequentInvalidEvents() { return subsequentInvalidEvents; }
    public void setSubsequentInvalidEvents(int subsequentInvalidEvents) { this.subsequentInvalidEvents = subsequentInvalidEvents; }
}
