package com.example.blackbox.ai;

import java.util.Map;

public class AiResponse {
    private final String actionType;
    private final String toolName;
    private final Map<String, Object> toolArguments;
    private final String content;
    private final boolean simulated;

    public AiResponse(String actionType, String toolName, Map<String, Object> toolArguments, String content, boolean simulated) {
        this.actionType = actionType;
        this.toolName = toolName;
        this.toolArguments = toolArguments;
        this.content = content;
        this.simulated = simulated;
    }

    public static AiResponse toolCall(String toolName, Map<String, Object> args, boolean simulated) {
        return new AiResponse("TOOL_CALL", toolName, args, "Model selected tool: " + toolName, simulated);
    }

    public static AiResponse delegate(String targetAgentRole, String subtaskPrompt, boolean simulated) {
        return new AiResponse("DELEGATE", null, Map.of("targetRole", targetAgentRole, "prompt", subtaskPrompt),
                "Model decided to delegate subtask to: " + targetAgentRole, simulated);
    }

    public static AiResponse finalAnswer(String answer, boolean simulated) {
        return new AiResponse("FINAL_RESPONSE", null, Map.of(), answer, simulated);
    }

    public String getActionType() { return actionType; }
    public String getToolName() { return toolName; }
    public Map<String, Object> getToolArguments() { return toolArguments; }
    public String getContent() { return content; }
    public boolean isSimulated() { return simulated; }
}
