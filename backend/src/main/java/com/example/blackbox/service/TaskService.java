package com.example.blackbox.service;

import com.example.blackbox.dto.CreateTaskRequest;
import com.example.blackbox.dto.TaskDto;
import com.example.blackbox.entity.TaskEntity;
import com.example.blackbox.exception.ResourceNotFoundException;
import com.example.blackbox.repository.AgentRepository;
import com.example.blackbox.repository.TaskRepository;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class TaskService {

    private static final Logger log = LoggerFactory.getLogger(TaskService.class);

    private final TaskRepository taskRepository;
    private final AgentRepository agentRepository;

    public TaskService(TaskRepository taskRepository, AgentRepository agentRepository) {
        this.taskRepository = taskRepository;
        this.agentRepository = agentRepository;
    }

    @PostConstruct
    public void initDefaultTasks() {
        if (taskRepository.count() == 0) {
            agentRepository.findAll().stream().findFirst().ifPresent(agent -> {
                log.info("Bootstrapping sample task for agent: {}", agent.getName());
                TaskEntity task = new TaskEntity(
                        UUID.randomUUID().toString(),
                        "Investigate Black Box Telemetry Integrity",
                        "Audit autonomous agent decisions, tool invocations, and cryptographic hash chain verification",
                        agent.getId(),
                        "READY",
                        Instant.now()
                );
                taskRepository.save(task);
            });
        }
    }

    @Transactional(readOnly = true)
    public List<TaskDto> getAllTasks() {
        return taskRepository.findAllByOrderByCreatedAtDesc().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public TaskDto getTaskById(String id) {
        return taskRepository.findById(id)
                .map(this::toDto)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found with ID: " + id));
    }

    @Transactional
    public TaskDto createTask(CreateTaskRequest request) {
        if (!agentRepository.existsById(request.getAgentId())) {
            throw new ResourceNotFoundException("Agent not found with ID: " + request.getAgentId());
        }

        String id = UUID.randomUUID().toString();
        TaskEntity entity = new TaskEntity(
                id,
                request.getTitle(),
                request.getDescription(),
                request.getAgentId(),
                "READY",
                Instant.now()
        );
        TaskEntity saved = taskRepository.save(entity);
        return toDto(saved);
    }

    public TaskDto toDto(TaskEntity entity) {
        return new TaskDto(
                entity.getId(),
                entity.getTitle(),
                entity.getDescription(),
                entity.getAgentId(),
                entity.getStatus(),
                entity.getCreatedAt()
        );
    }
}
