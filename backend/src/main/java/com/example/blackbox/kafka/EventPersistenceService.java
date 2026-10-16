package com.example.blackbox.kafka;

import com.example.blackbox.dto.AgentEventMessage;
import com.example.blackbox.entity.AgentEventEntity;
import com.example.blackbox.entity.ExecutionEntity;
import com.example.blackbox.repository.AgentEventRepository;
import com.example.blackbox.repository.ExecutionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class EventPersistenceService {

    private static final Logger log = LoggerFactory.getLogger(EventPersistenceService.class);

    private final AgentEventRepository eventRepository;
    private final ExecutionRepository executionRepository;

    public EventPersistenceService(AgentEventRepository eventRepository, ExecutionRepository executionRepository) {
        this.eventRepository = eventRepository;
        this.executionRepository = executionRepository;
    }

    @Transactional
    public AgentEventEntity persistEvent(AgentEventMessage message) {
        if (eventRepository.existsById(message.getId())) {
            log.info("Idempotent deduplication: event {} already persisted, skipping.", message.getId());
            return eventRepository.findById(message.getId()).orElse(null);
        }

        AgentEventEntity entity = new AgentEventEntity();
        entity.setId(message.getId());
        entity.setExecutionId(message.getExecutionId());
        entity.setTaskId(message.getTaskId());
        entity.setAgentId(message.getAgentId());
        entity.setParentExecutionId(message.getParentExecutionId());
        entity.setCorrelationId(message.getCorrelationId());
        entity.setEventType(message.getEventType());
        entity.setTimestamp(message.getTimestamp());
        entity.setSequenceNumber(message.getSequenceNumber());
        entity.setDescription(message.getDescription());
        entity.setToolName(message.getToolName());
        entity.setSanitizedInput(message.getSanitizedInput());
        entity.setSanitizedOutput(message.getSanitizedOutput());
        entity.setStatus(message.getStatus());
        entity.setDurationMs(message.getDurationMs());
        entity.setErrorDetails(message.getErrorDetails());
        entity.setPolicyResult(message.getPolicyResult());
        entity.setPolicyReason(message.getPolicyReason());
        entity.setEvidenceReference(message.getEvidenceReference());
        entity.setContentHash(message.getContentHash());
        entity.setPreviousEventHash(message.getPreviousEventHash());
        entity.setCurrentEventHash(message.getCurrentEventHash());
        entity.setTraceId(message.getTraceId());
        entity.setSpanId(message.getSpanId());
        entity.setIsTampered(false);

        AgentEventEntity saved = eventRepository.save(entity);

        // Update event count on execution entity
        executionRepository.findById(message.getExecutionId()).ifPresent(exec -> {
            exec.setEventCount((exec.getEventCount() == null ? 0 : exec.getEventCount()) + 1);
            exec.setFinalHash(message.getCurrentEventHash());
            executionRepository.save(exec);
        });

        log.debug("Persisted event {} seq #{} for execution {}", saved.getId(), saved.getSequenceNumber(), saved.getExecutionId());
        return saved;
    }
}
