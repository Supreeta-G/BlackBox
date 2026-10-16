package com.example.blackbox.tools;

import java.util.Map;

public interface AgentTool {
    String getName();
    String getDescription();
    ToolExecutionResult execute(Map<String, Object> params);
}
