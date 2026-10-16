package com.example.blackbox.client.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ExecutionComparisonModel {
    private ExecutionModel execution1;
    private ExecutionModel execution2;
    private long durationDifferenceMs;
    private int eventCountDifference;
    private boolean sameStatus;
    private List<String> toolsUsedExecution1;
    private List<String> toolsUsedExecution2;
    private List<String> errorsExecution1;
    private List<String> errorsExecution2;
    private int policyDecisionsExecution1;
    private int policyDecisionsExecution2;
    private IntegrityResultModel integrityExecution1;
    private IntegrityResultModel integrityExecution2;

    public ExecutionComparisonModel() {
    }

    public ExecutionModel getExecution1() { return execution1; }
    public void setExecution1(ExecutionModel execution1) { this.execution1 = execution1; }
    public ExecutionModel getExecution2() { return execution2; }
    public void setExecution2(ExecutionModel execution2) { this.execution2 = execution2; }
    public long getDurationDifferenceMs() { return durationDifferenceMs; }
    public void setDurationDifferenceMs(long durationDifferenceMs) { this.durationDifferenceMs = durationDifferenceMs; }
    public int getEventCountDifference() { return eventCountDifference; }
    public void setEventCountDifference(int eventCountDifference) { this.eventCountDifference = eventCountDifference; }
    public boolean isSameStatus() { return sameStatus; }
    public void setSameStatus(boolean sameStatus) { this.sameStatus = sameStatus; }
    public List<String> getToolsUsedExecution1() { return toolsUsedExecution1; }
    public void setToolsUsedExecution1(List<String> toolsUsedExecution1) { this.toolsUsedExecution1 = toolsUsedExecution1; }
    public List<String> getToolsUsedExecution2() { return toolsUsedExecution2; }
    public void setToolsUsedExecution2(List<String> toolsUsedExecution2) { this.toolsUsedExecution2 = toolsUsedExecution2; }
    public List<String> getErrorsExecution1() { return errorsExecution1; }
    public void setErrorsExecution1(List<String> errorsExecution1) { this.errorsExecution1 = errorsExecution1; }
    public List<String> getErrorsExecution2() { return errorsExecution2; }
    public void setErrorsExecution2(List<String> errorsExecution2) { this.errorsExecution2 = errorsExecution2; }
    public int getPolicyDecisionsExecution1() { return policyDecisionsExecution1; }
    public void setPolicyDecisionsExecution1(int policyDecisionsExecution1) { this.policyDecisionsExecution1 = policyDecisionsExecution1; }
    public int getPolicyDecisionsExecution2() { return policyDecisionsExecution2; }
    public void setPolicyDecisionsExecution2(int policyDecisionsExecution2) { this.policyDecisionsExecution2 = policyDecisionsExecution2; }
    public IntegrityResultModel getIntegrityExecution1() { return integrityExecution1; }
    public void setIntegrityExecution1(IntegrityResultModel integrityExecution1) { this.integrityExecution1 = integrityExecution1; }
    public IntegrityResultModel getIntegrityExecution2() { return integrityExecution2; }
    public void setIntegrityExecution2(IntegrityResultModel integrityExecution2) { this.integrityExecution2 = integrityExecution2; }
}
