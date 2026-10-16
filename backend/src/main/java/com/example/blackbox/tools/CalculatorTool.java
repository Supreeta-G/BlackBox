package com.example.blackbox.tools;

import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class CalculatorTool implements AgentTool {

    @Override
    public String getName() {
        return "CalculatorTool";
    }

    @Override
    public String getDescription() {
        return "Performs mathematical arithmetic, cryptographic latency calculations, and statistical aggregations.";
    }

    @Override
    public ToolExecutionResult execute(Map<String, Object> params) {
        long start = System.currentTimeMillis();
        String expression = params != null && params.get("expression") != null ? params.get("expression").toString() : "";

        try {
            Thread.sleep(25);
        } catch (InterruptedException ignored) {}

        if (expression.toLowerCase().contains("fail_calc") || expression.contains("/ 0")) {
            return ToolExecutionResult.failure("ArithmeticException: Division by zero or invalid math token", System.currentTimeMillis() - start);
        }

        try {
            String trimmed = expression.replaceAll("\\s+", "");
            double result;
            if (trimmed.contains("+")) {
                String[] parts = trimmed.split("\\+");
                result = Double.parseDouble(parts[0]) + Double.parseDouble(parts[1]);
            } else if (trimmed.contains("-")) {
                String[] parts = trimmed.split("-");
                result = Double.parseDouble(parts[0]) - Double.parseDouble(parts[1]);
            } else if (trimmed.contains("*")) {
                String[] parts = trimmed.split("\\*");
                result = Double.parseDouble(parts[0]) * Double.parseDouble(parts[1]);
            } else if (trimmed.contains("/")) {
                String[] parts = trimmed.split("/");
                double divisor = Double.parseDouble(parts[1]);
                if (divisor == 0) throw new ArithmeticException("Division by zero");
                result = Double.parseDouble(parts[0]) / divisor;
            } else {
                result = 42.0; // default benchmark value
            }
            return ToolExecutionResult.success("Calculated result: " + result, System.currentTimeMillis() - start);
        } catch (Exception e) {
            return ToolExecutionResult.failure("Calculation error: " + e.getMessage(), System.currentTimeMillis() - start);
        }
    }
}
