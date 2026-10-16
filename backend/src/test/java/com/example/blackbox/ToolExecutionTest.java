package com.example.blackbox;

import com.example.blackbox.tools.CalculatorTool;
import com.example.blackbox.tools.InformationSearchTool;
import com.example.blackbox.tools.ToolExecutionResult;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class ToolExecutionTest {

    @Test
    @DisplayName("CalculatorTool evaluates arithmetic expressions correctly")
    void testCalculatorTool() {
        CalculatorTool calculator = new CalculatorTool();

        ToolExecutionResult resAdd = calculator.execute(Map.of("expression", "25 + 75"));
        assertTrue(resAdd.isSuccess());
        assertTrue(resAdd.getOutput().contains("100.0"));

        ToolExecutionResult resDivZero = calculator.execute(Map.of("expression", "50 / 0"));
        assertFalse(resDivZero.isSuccess());
        assertNotNull(resDivZero.getErrorMessage());
    }

    @Test
    @DisplayName("InformationSearchTool returns domain results and supports simulated failures")
    void testInformationSearchTool() {
        InformationSearchTool searchTool = new InformationSearchTool();

        ToolExecutionResult resSuccess = searchTool.execute(Map.of("query", "SHA-256 integrity"));
        assertTrue(resSuccess.isSuccess());
        assertTrue(resSuccess.getOutput().contains("SHA-256"));

        ToolExecutionResult resFail = searchTool.execute(Map.of("query", "fail_search"));
        assertFalse(resFail.isSuccess());
        assertTrue(resFail.getErrorMessage().contains("timed out"));
    }
}
