package com.example.blackbox.controller;

import com.example.blackbox.dto.CreateTaskRequest;
import com.example.blackbox.dto.ExecutionDto;
import com.example.blackbox.dto.RunExecutionRequest;
import com.example.blackbox.dto.TaskDto;
import com.example.blackbox.entity.ExecutionEntity;
import com.example.blackbox.execution.AgentExecutionService;
import com.example.blackbox.service.ExecutionService;
import com.example.blackbox.service.TaskService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tasks")
@CrossOrigin(origins = "*")
public class TaskController {

    private final TaskService taskService;
    private final AgentExecutionService agentExecutionService;
    private final ExecutionService executionService;

    public TaskController(TaskService taskService,
                          AgentExecutionService agentExecutionService,
                          ExecutionService executionService) {
        this.taskService = taskService;
        this.agentExecutionService = agentExecutionService;
        this.executionService = executionService;
    }

    @GetMapping
    public ResponseEntity<List<TaskDto>> getAllTasks() {
        return ResponseEntity.ok(taskService.getAllTasks());
    }

    @GetMapping("/{id}")
    public ResponseEntity<TaskDto> getTaskById(@PathVariable("id") String id) {
        return ResponseEntity.ok(taskService.getTaskById(id));
    }

    @PostMapping
    public ResponseEntity<TaskDto> createTask(@Valid @RequestBody CreateTaskRequest request) {
        TaskDto created = taskService.createTask(request);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    @PostMapping("/{id}/execute")
    public ResponseEntity<ExecutionDto> executeTask(
            @PathVariable("id") String id,
            @RequestBody(required = false) RunExecutionRequest request) {
        String scenario = request != null && request.getScenario() != null ? request.getScenario() : "STANDARD";
        ExecutionEntity execution = agentExecutionService.runExecution(id, scenario);
        return ResponseEntity.ok(executionService.toExecutionDto(execution));
    }
}
