package com.example.blackbox.dto;

import com.example.blackbox.enums.IntegrityStatus;

public class IntegrityVerificationResult {
    private IntegrityStatus status;
    private String executionId;
    private int totalEventsVerified;
    private Integer firstInvalidSequenceNumber;
    private String firstInvalidEventId;
    private String expectedHash;
    private String actualHash;
    private String reason;
    private String details;
    private int subsequentInvalidEvents;

    public IntegrityVerificationResult() {
    }

    public static IntegrityVerificationResult verified(String executionId, int totalEvents, String finalHash) {
        IntegrityVerificationResult result = new IntegrityVerificationResult();
        result.setStatus(IntegrityStatus.INTEGRITY_VERIFIED);
        result.setExecutionId(executionId);
        result.setTotalEventsVerified(totalEvents);
        result.setReason("All " + totalEvents + " events in sequence successfully verified against canonical SHA-256 hash chain.");
        result.setDetails("Genesis block verified. All previous-hash pointers match and recalculated SHA-256 digests equal stored hashes.");
        result.setActualHash(finalHash);
        result.setExpectedHash(finalHash);
        result.setSubsequentInvalidEvents(0);
        return result;
    }

    public static IntegrityVerificationResult compromised(String executionId, int totalEvents, int invalidSeq,
                                                          String invalidEventId, String expectedHash, String actualHash,
                                                          String reason, int subsequentInvalid) {
        IntegrityVerificationResult result = new IntegrityVerificationResult();
        result.setStatus(IntegrityStatus.INTEGRITY_COMPROMISED);
        result.setExecutionId(executionId);
        result.setTotalEventsVerified(totalEvents);
        result.setFirstInvalidSequenceNumber(invalidSeq);
        result.setFirstInvalidEventId(invalidEventId);
        result.setExpectedHash(expectedHash);
        result.setActualHash(actualHash);
        result.setReason(reason);
        result.setSubsequentInvalidEvents(subsequentInvalid);
        result.setDetails("Integrity violation detected at sequence #" + invalidSeq + " (Event ID: " + invalidEventId + "). " +
                "Tampering alters cryptographic payload canonicalization, invalidating this and all " + subsequentInvalid + " downstream block hashes.");
        return result;
    }

    public IntegrityStatus getStatus() { return status; }
    public void setStatus(IntegrityStatus status) { this.status = status; }
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
