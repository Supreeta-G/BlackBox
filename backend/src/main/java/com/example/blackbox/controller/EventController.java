package com.example.blackbox.controller;

import com.example.blackbox.dto.AgentEventDto;
import com.example.blackbox.exception.ResourceNotFoundException;
import com.example.blackbox.repository.AgentEventRepository;
import com.example.blackbox.service.ExecutionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/events")
@CrossOrigin(origins = "*")
public class EventController {

    private final AgentEventRepository eventRepository;
    private final ExecutionService executionService;

    public EventController(AgentEventRepository eventRepository, ExecutionService executionService) {
        this.eventRepository = eventRepository;
        this.executionService = executionService;
    }

    @GetMapping("/{id}")
    public ResponseEntity<AgentEventDto> getEventById(@PathVariable("id") String id) {
        return eventRepository.findById(id)
                .map(executionService::toEventDto)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found with ID: " + id));
    }
}
