package com.example.blackbox.tools;

import com.example.blackbox.entity.ToolCallEntity;
import com.example.blackbox.repository.ToolCallRepository;
import com.example.blackbox.security.RedactionService;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.*;

@Service
public class ToolRegistry {

    private final Map<String, AgentTool> tools = new HashMap<>();
    private final ToolCallRepository toolCallRepository;
    private final RedactionService redactionService;

    public ToolRegistry(List<AgentTool> agentTools, ToolCallRepository toolCallRepository, RedactionService redactionService) {
        for (AgentTool tool : agentTools) {
            tools.put(tool.getName().toLowerCase(), tool);
        }
        this.toolCallRepository = toolCallRepository;
        this.redactionService = redactionService;
    }

    public AgentTool getTool(String name) {
        if (name == null) return null;
        return tools.get(name.toLowerCase());
    }

    public List<AgentTool> getAllTools() {
        return new ArrayList<>(tools.values());
    }

    public ToolExecutionResult invokeTool(String toolName, Map<String, Object> params,
                                          String executionId, String eventId) {
        AgentTool tool = getTool(toolName);
        if (tool == null) {
            return ToolExecutionResult.failure("Unknown tool: " + toolName, 0);
        }

        Instant startedAt = Instant.now();
        ToolExecutionResult result = tool.execute(params);
        Instant completedAt = Instant.now();

        // Record tool call entity
        String sanitizedInput = redactionService.sanitize(params != null ? params.toString() : "{}");
        String sanitizedOutput = redactionService.sanitize(result.isSuccess() ? result.getOutput() : result.getErrorMessage());

        ToolCallEntity entity = new ToolCallEntity(
                UUID.randomUUID().toString(),
                executionId,
                eventId,
                tool.getName(),
                sanitizedInput,
                sanitizedOutput,
                result.isSuccess() ? "SUCCESS" : "FAILED",
                result.getDurationMs(),
                startedAt,
                completedAt
        );
        toolCallRepository.save(entity);

        return result;
    }
}
