package com.example.blackbox.kafka;

import com.example.blackbox.dto.AgentEventMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@ConditionalOnProperty(name = "blackbox.kafka.enabled", havingValue = "true", matchIfMissing = false)
public class KafkaEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(KafkaEventConsumer.class);

    private final EventPersistenceService eventPersistenceService;

    public KafkaEventConsumer(EventPersistenceService eventPersistenceService) {
        this.eventPersistenceService = eventPersistenceService;
    }

    @KafkaListener(topics = "${blackbox.kafka.topic:ai-agent-events}", groupId = "${spring.kafka.consumer.group-id:blackbox-event-group}")
    public void consumeEvent(AgentEventMessage message) {
        log.info("Kafka consumer received event {} seq #{} for execution {}",
                message.getId(), message.getSequenceNumber(), message.getExecutionId());
        try {
            eventPersistenceService.persistEvent(message);
        } catch (Exception e) {
            log.error("Error in Kafka consumer while persisting event {}: {}", message.getId(), e.getMessage(), e);
        }
    }
}
