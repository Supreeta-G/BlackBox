package com.example.blackbox.ai;

import java.util.List;

public interface AiService {
    AiResponse planNextAction(String prompt, String context, List<String> availableTools, int stepNumber);
    AiResponse synthesizeResult(String originalTask, List<String> intermediateResults);
}
