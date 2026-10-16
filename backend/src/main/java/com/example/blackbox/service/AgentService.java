package com.example.blackbox.service;

import com.example.blackbox.dto.AgentDto;
import com.example.blackbox.dto.CreateAgentRequest;
import com.example.blackbox.entity.AgentEntity;
import com.example.blackbox.enums.AgentRole;
import com.example.blackbox.exception.ResourceNotFoundException;
import com.example.blackbox.integrity.IntegrityService;
import com.example.blackbox.repository.AgentRepository;
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
public class AgentService {

    private static final Logger log = LoggerFactory.getLogger(AgentService.class);

    private final AgentRepository agentRepository;
    private final IntegrityService integrityService;

    public AgentService(AgentRepository agentRepository, IntegrityService integrityService) {
        this.agentRepository = agentRepository;
        this.integrityService = integrityService;
    }

    @PostConstruct
    public void initDefaultAgents() {
        if (agentRepository.count() == 0) {
            log.info("Bootstrapping default AI agents...");
            createDefaultAgent("OrchestratorAgent", AgentRole.ORCHESTRATOR_AGENT, "gpt-4o-mini");
            createDefaultAgent("ResearchAgent", AgentRole.RESEARCH_AGENT, "gpt-4o");
            createDefaultAgent("SummaryAgent", AgentRole.SUMMARY_AGENT, "gpt-4o-mini");
            createDefaultAgent("SecurityAgent", AgentRole.SECURITY_AGENT, "claude-3-5-sonnet");
        }
    }

    private void createDefaultAgent(String name, AgentRole role, String model) {
        String id = UUID.randomUUID().toString();
        String configHash = integrityService.calculateSha256(name + "|" + role + "|" + model);
        AgentEntity agent = new AgentEntity(id, name, role, model, configHash, Instant.now());
        agentRepository.save(agent);
    }

    @Transactional(readOnly = true)
    public List<AgentDto> getAllAgents() {
        return agentRepository.findAll().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public AgentDto getAgentById(String id) {
        return agentRepository.findById(id)
                .map(this::toDto)
                .orElseThrow(() -> new ResourceNotFoundException("Agent not found with ID: " + id));
    }

    @Transactional
    public AgentDto createAgent(CreateAgentRequest request) {
        String id = UUID.randomUUID().toString();
        String model = request.getModelIdentifier() != null ? request.getModelIdentifier() : "gpt-4o-mini";
        String configHash = integrityService.calculateSha256(request.getName() + "|" + request.getRole() + "|" + model);

        AgentEntity entity = new AgentEntity(id, request.getName(), request.getRole(), model, configHash, Instant.now());
        AgentEntity saved = agentRepository.save(entity);
        return toDto(saved);
    }

    public AgentDto toDto(AgentEntity entity) {
        return new AgentDto(
                entity.getId(),
                entity.getName(),
                entity.getRole(),
                entity.getModelIdentifier(),
                entity.getConfigurationHash(),
                entity.getCreatedAt()
        );
    }
}
