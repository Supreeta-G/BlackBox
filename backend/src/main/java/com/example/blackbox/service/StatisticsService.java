package com.example.blackbox.service;

import com.example.blackbox.dto.StatisticsDto;
import com.example.blackbox.enums.ExecutionStatus;
import com.example.blackbox.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

@Service
public class StatisticsService {

    private final AgentRepository agentRepository;
    private final TaskRepository taskRepository;
    private final ExecutionRepository executionRepository;
    private final AgentEventRepository eventRepository;
    private final ToolCallRepository toolCallRepository;
    private final PolicyDecisionRepository policyDecisionRepository;

    public StatisticsService(AgentRepository agentRepository,
                             TaskRepository taskRepository,
                             ExecutionRepository executionRepository,
                             AgentEventRepository eventRepository,
                             ToolCallRepository toolCallRepository,
                             PolicyDecisionRepository policyDecisionRepository) {
        this.agentRepository = agentRepository;
        this.taskRepository = taskRepository;
        this.executionRepository = executionRepository;
        this.eventRepository = eventRepository;
        this.toolCallRepository = toolCallRepository;
        this.policyDecisionRepository = policyDecisionRepository;
    }

    @Transactional(readOnly = true)
    public StatisticsDto getStatistics() {
        StatisticsDto stats = new StatisticsDto();

        long totalAgents = agentRepository.count();
        long totalTasks = taskRepository.count();
        long totalExecutions = executionRepository.count();
        long successful = executionRepository.countByStatus(ExecutionStatus.COMPLETED);
        long failed = executionRepository.countByStatus(ExecutionStatus.FAILED);
        long totalEvents = eventRepository.count();
        long tampered = eventRepository.countByIsTamperedTrue();

        stats.setTotalAgents(totalAgents);
        stats.setTotalTasks(totalTasks);
        stats.setTotalExecutions(totalExecutions);
        stats.setSuccessfulExecutions(successful);
        stats.setFailedExecutions(failed);
        stats.setTotalEvents(totalEvents);
        stats.setIntegrityFailures(tampered);

        // Average duration
        double avgDuration = executionRepository.findAll().stream()
                .filter(e -> e.getDurationMs() != null)
                .mapToLong(e -> e.getDurationMs())
                .average()
                .orElse(0.0);
        stats.setAverageDurationMs(Math.round(avgDuration * 100.0) / 100.0);

        // Tool usage counts
        Map<String, Long> toolCounts = new HashMap<>();
        toolCallRepository.findAll().forEach(tc -> {
            toolCounts.merge(tc.getToolName(), 1L, Long::sum);
        });
        stats.setToolUsageCounts(toolCounts);

        // Event type distribution
        Map<String, Long> eventCounts = new HashMap<>();
        eventRepository.findAll().forEach(ev -> {
            eventCounts.merge(ev.getEventType().name(), 1L, Long::sum);
        });
        stats.setEventTypeCounts(eventCounts);

        // Policy decision counts
        Map<String, Long> polCounts = new HashMap<>();
        policyDecisionRepository.findAll().forEach(pd -> {
            polCounts.merge(pd.getDecision().name(), 1L, Long::sum);
        });
        stats.setPolicyDecisionCounts(polCounts);

        return stats;
    }
}
