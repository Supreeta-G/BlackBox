package com.example.blackbox.execution;

import com.example.blackbox.dto.AgentEventMessage;
import com.example.blackbox.entity.AgentEventEntity;
import com.example.blackbox.entity.ExecutionEntity;
import com.example.blackbox.enums.EventType;
import com.example.blackbox.enums.PolicyResult;
import com.example.blackbox.integrity.IntegrityService;
import com.example.blackbox.kafka.EventDispatcherService;
import com.example.blackbox.repository.AgentEventRepository;
import com.example.blackbox.security.RedactionService;
import com.example.blackbox.telemetry.TelemetryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.UUID;

@Service
public class ExecutionRecorder {

    private static final Logger log = LoggerFactory.getLogger(ExecutionRecorder.class);

    private final AgentEventRepository eventRepository;
    private final IntegrityService integrityService;
    private final RedactionService redactionService;
    private final TelemetryService telemetryService;
    private final EventDispatcherService eventDispatcher;

    public ExecutionRecorder(AgentEventRepository eventRepository,
                             IntegrityService integrityService,
                             RedactionService redactionService,
                             TelemetryService telemetryService,
                             EventDispatcherService eventDispatcher) {
        this.eventRepository = eventRepository;
        this.integrityService = integrityService;
        this.redactionService = redactionService;
        this.telemetryService = telemetryService;
        this.eventDispatcher = eventDispatcher;
    }

    public synchronized AgentEventEntity recordEvent(ExecutionEntity execution,
                                                     EventType eventType,
                                                     String description,
                                                     String toolName,
                                                     String rawInput,
                                                     String rawOutput,
                                                     String status,
                                                     Long durationMs,
                                                     String errorDetails,
                                                     PolicyResult policyResult,
                                                     String policyReason,
                                                     String evidenceReference) {

        String eventId = UUID.randomUUID().toString();
        // Truncate to millis for exact SQL timestamp compatibility
        Instant now = Instant.now().truncatedTo(ChronoUnit.MILLIS);

        // 1. Determine next sequence number and previous event hash
        Optional<AgentEventEntity> lastEventOpt = eventRepository.findTopByExecutionIdOrderBySequenceNumberDesc(execution.getId());
        int nextSeq = lastEventOpt.map(e -> e.getSequenceNumber() + 1).orElse(1);
        String previousHash = lastEventOpt.map(AgentEventEntity::getCurrentEventHash).orElse(IntegrityService.GENESIS_HASH);

        // 2. Sanitize sensitive parameters
        String sanitizedInput = redactionService.sanitize(rawInput);
        String sanitizedOutput = redactionService.sanitize(rawOutput);
        String sanitizedError = redactionService.sanitize(errorDetails);

        // 3. Compute content hash
        String contentToHash = (description != null ? description : "") +
                (sanitizedInput != null ? sanitizedInput : "") +
                (sanitizedOutput != null ? sanitizedOutput : "");
        String contentHash = integrityService.calculateSha256(contentToHash);

        // 4. Construct entity
        AgentEventEntity entity = new AgentEventEntity();
        entity.setId(eventId);
        entity.setExecutionId(execution.getId());
        entity.setTaskId(execution.getTaskId());
        entity.setAgentId(execution.getAgentId());
        entity.setParentExecutionId(execution.getParentExecutionId());
        entity.setCorrelationId(execution.getCorrelationId());
        entity.setEventType(eventType);
        entity.setTimestamp(now);
        entity.setSequenceNumber(nextSeq);
        entity.setDescription(description);
        entity.setToolName(toolName);
        entity.setSanitizedInput(sanitizedInput);
        entity.setSanitizedOutput(sanitizedOutput);
        entity.setStatus(status != null ? status : "SUCCESS");
        entity.setDurationMs(durationMs);
        entity.setErrorDetails(sanitizedError);
        entity.setPolicyResult(policyResult);
        entity.setPolicyReason(policyReason);
        entity.setEvidenceReference(evidenceReference);
        entity.setContentHash(contentHash);
        entity.setPreviousEventHash(previousHash);
        entity.setIsTampered(false);

        // 5. Compute SHA-256 canonical hash
        String currentHash = integrityService.calculateEventHash(entity);
        entity.setCurrentEventHash(currentHash);

        // 6. OpenTelemetry correlation
        entity.setTraceId(telemetryService.generateTraceId());
        entity.setSpanId(telemetryService.generateSpanId());

        // 7. Package message for Kafka / Dispatcher
        AgentEventMessage message = new AgentEventMessage();
        message.setId(entity.getId());
        message.setExecutionId(entity.getExecutionId());
        message.setTaskId(entity.getTaskId());
        message.setAgentId(entity.getAgentId());
        message.setParentExecutionId(entity.getParentExecutionId());
        message.setCorrelationId(entity.getCorrelationId());
        message.setEventType(entity.getEventType());
        message.setTimestamp(entity.getTimestamp());
        message.setSequenceNumber(entity.getSequenceNumber());
        message.setDescription(entity.getDescription());
        message.setToolName(entity.getToolName());
        message.setSanitizedInput(entity.getSanitizedInput());
        message.setSanitizedOutput(entity.getSanitizedOutput());
        message.setStatus(entity.getStatus());
        message.setDurationMs(entity.getDurationMs());
        message.setErrorDetails(entity.getErrorDetails());
        message.setPolicyResult(entity.getPolicyResult());
        message.setPolicyReason(entity.getPolicyReason());
        message.setEvidenceReference(entity.getEvidenceReference());
        message.setContentHash(entity.getContentHash());
        message.setPreviousEventHash(entity.getPreviousEventHash());
        message.setCurrentEventHash(entity.getCurrentEventHash());
        message.setTraceId(entity.getTraceId());
        message.setSpanId(entity.getSpanId());

        log.info("Recorded Event [Seq: {} | Type: {} | ID: {} | Exec: {} | Hash: {}]",
                nextSeq, eventType, eventId, execution.getId(), currentHash.substring(0, 12) + "...");

        return eventDispatcher.dispatchEvent(message);
    }
}
