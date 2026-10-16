package com.example.blackbox;

import com.example.blackbox.dto.AgentEventMessage;
import com.example.blackbox.entity.AgentEventEntity;
import com.example.blackbox.entity.ExecutionEntity;
import com.example.blackbox.enums.EventType;
import com.example.blackbox.enums.ExecutionStatus;
import com.example.blackbox.kafka.EventPersistenceService;
import com.example.blackbox.repository.AgentEventRepository;
import com.example.blackbox.repository.ExecutionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EventPersistenceAndDeduplicationTest {

    @Mock
    private AgentEventRepository eventRepository;

    @Mock
    private ExecutionRepository executionRepository;

    private EventPersistenceService persistenceService;

    @BeforeEach
    void setUp() {
        persistenceService = new EventPersistenceService(eventRepository, executionRepository);
    }

    @Test
    @DisplayName("Duplicate event delivery must be idempotently ignored without duplicate insertion")
    void testEventDeduplication() {
        String eventId = "evt-dedup-100";
        String execId = "exec-dedup-100";

        AgentEventMessage message = new AgentEventMessage();
        message.setId(eventId);
        message.setExecutionId(execId);
        message.setTaskId("task-1");
        message.setAgentId("agent-1");
        message.setCorrelationId("corr-1");
        message.setEventType(EventType.TOOL_INVOCATION);
        message.setTimestamp(Instant.now());
        message.setSequenceNumber(1);
        message.setDescription("Sample event");
        message.setStatus("SUCCESS");
        message.setPreviousEventHash("0000000000000000000000000000000000000000000000000000000000000000");
        message.setCurrentEventHash("abc12345");

        // First delivery: event does not exist in DB
        when(eventRepository.existsById(eventId)).thenReturn(false);
        when(eventRepository.save(any(AgentEventEntity.class))).thenAnswer(i -> i.getArgument(0));
        when(executionRepository.findById(execId)).thenReturn(Optional.of(new ExecutionEntity(
                execId, "task-1", "agent-1", "corr-1", execId, null, ExecutionStatus.RUNNING, Instant.now(), Instant.now()
        )));

        AgentEventEntity saved1 = persistenceService.persistEvent(message);
        assertNotNull(saved1);
        verify(eventRepository, times(1)).save(any(AgentEventEntity.class));

        // Second duplicate delivery: event already exists in DB
        AgentEventEntity existing = new AgentEventEntity();
        existing.setId(eventId);
        when(eventRepository.existsById(eventId)).thenReturn(true);
        when(eventRepository.findById(eventId)).thenReturn(Optional.of(existing));

        AgentEventEntity saved2 = persistenceService.persistEvent(message);
        assertEquals(eventId, saved2.getId());

        // Verify save was NOT called a second time
        verify(eventRepository, times(1)).save(any(AgentEventEntity.class));
    }
}
