package com.example.blackbox.controller;

import com.example.blackbox.dto.AgentDto;
import com.example.blackbox.dto.CreateAgentRequest;
import com.example.blackbox.service.AgentService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/agents")
@CrossOrigin(origins = "*")
public class AgentController {

    private final AgentService agentService;

    public AgentController(AgentService agentService) {
        this.agentService = agentService;
    }

    @GetMapping
    public ResponseEntity<List<AgentDto>> getAllAgents() {
        return ResponseEntity.ok(agentService.getAllAgents());
    }

    @GetMapping("/{id}")
    public ResponseEntity<AgentDto> getAgentById(@PathVariable("id") String id) {
        return ResponseEntity.ok(agentService.getAgentById(id));
    }

    @PostMapping
    public ResponseEntity<AgentDto> createAgent(@Valid @RequestBody CreateAgentRequest request) {
        AgentDto created = agentService.createAgent(request);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }
}
