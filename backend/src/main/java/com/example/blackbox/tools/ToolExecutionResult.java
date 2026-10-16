package com.example.blackbox.tools;

public class ToolExecutionResult {
    private final boolean success;
    private final String output;
    private final String errorMessage;
    private final long durationMs;

    public ToolExecutionResult(boolean success, String output, String errorMessage, long durationMs) {
        this.success = success;
        this.output = output;
        this.errorMessage = errorMessage;
        this.durationMs = durationMs;
    }

    public static ToolExecutionResult success(String output, long durationMs) {
        return new ToolExecutionResult(true, output, null, durationMs);
    }

    public static ToolExecutionResult failure(String errorMessage, long durationMs) {
        return new ToolExecutionResult(false, null, errorMessage, durationMs);
    }

    public boolean isSuccess() { return success; }
    public String getOutput() { return output; }
    public String getErrorMessage() { return errorMessage; }
    public long getDurationMs() { return durationMs; }
}
