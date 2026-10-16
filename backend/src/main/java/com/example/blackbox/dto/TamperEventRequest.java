package com.example.blackbox.dto;

public class TamperEventRequest {
    private Integer sequenceNumber;
    private String modifiedDescription;
    private String modifiedInput;

    public TamperEventRequest() {
    }

    public TamperEventRequest(Integer sequenceNumber, String modifiedDescription, String modifiedInput) {
        this.sequenceNumber = sequenceNumber;
        this.modifiedDescription = modifiedDescription;
        this.modifiedInput = modifiedInput;
    }

    public Integer getSequenceNumber() { return sequenceNumber; }
    public void setSequenceNumber(Integer sequenceNumber) { this.sequenceNumber = sequenceNumber; }
    public String getModifiedDescription() { return modifiedDescription; }
    public void setModifiedDescription(String modifiedDescription) { this.modifiedDescription = modifiedDescription; }
    public String getModifiedInput() { return modifiedInput; }
    public void setModifiedInput(String modifiedInput) { this.modifiedInput = modifiedInput; }
}
