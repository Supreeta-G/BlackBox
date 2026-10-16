package com.example.blackbox.ai;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class DemoAiService implements AiService {

    private static final Logger log = LoggerFactory.getLogger(DemoAiService.class);

    @Override
    public AiResponse planNextAction(String prompt, String context, List<String> availableTools, int stepNumber) {
        log.info("Demo AI evaluating action for step {} with prompt: {}", stepNumber, prompt);
        String p = prompt != null ? prompt.toLowerCase() : "";

        // If step is 1 and delegation is requested
        if (p.contains("delegate") || p.contains("multi-agent") || p.contains("summarize research")) {
            if (stepNumber == 1) {
                return AiResponse.toolCall("InformationSearchTool",
                        Map.of("query", "Flight telemetry incident logs and checksum algorithms"), true);
            } else if (stepNumber == 2) {
                return AiResponse.delegate("SUMMARY_AGENT", "Synthesize findings into executive audit brief", true);
            }
        }

        // Tool selection based on prompt keywords
        if (p.contains("calc") || p.contains("math") || p.contains("+") || p.contains("*")) {
            Map<String, Object> args = new HashMap<>();
            if (p.contains("fail_calc")) {
                args.put("expression", "100 / 0");
            } else {
                args.put("expression", "1250 * 4");
            }
            return AiResponse.toolCall("CalculatorTool", args, true);
        }

        if (p.contains("fail_search")) {
            return AiResponse.toolCall("InformationSearchTool", Map.of("query", "fail_search"), true);
        }

        if (stepNumber == 1) {
            Map<String, Object> args = new HashMap<>();
            args.put("query", prompt);
            return AiResponse.toolCall("InformationSearchTool", args, true);
        }

        return AiResponse.finalAnswer("[DEMO AI SIMULATION] Task execution verified successfully. All observable stages recorded in Black Box timeline.", true);
    }

    @Override
    public AiResponse synthesizeResult(String originalTask, List<String> intermediateResults) {
        StringBuilder sb = new StringBuilder();
        sb.append("[DEMO AI OBSERVABLE RESULT]\n");
        sb.append("Analysis completed for task: '").append(originalTask).append("'.\n");
        sb.append("Evidence gathered from ").append(intermediateResults.size()).append(" observable tool/action outputs:\n");
        for (int i = 0; i < intermediateResults.size(); i++) {
            sb.append("  [Evidence #").append(i + 1).append("]: ").append(intermediateResults.get(i)).append("\n");
        }
        sb.append("Integrity status: Cryptographically chained and sealed in Black Box flight recorder.");
        return AiResponse.finalAnswer(sb.toString(), true);
    }
}
