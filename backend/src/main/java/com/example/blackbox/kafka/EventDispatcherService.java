package com.example.blackbox.kafka;

import com.example.blackbox.dto.AgentEventMessage;
import com.example.blackbox.entity.AgentEventEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class EventDispatcherService {

    private static final Logger log = LoggerFactory.getLogger(EventDispatcherService.class);

    private final Optional<KafkaEventProducer> kafkaProducer;
    private final EventPersistenceService eventPersistenceService;

    public EventDispatcherService(@Autowired(required = false) Optional<KafkaEventProducer> kafkaProducer,
                                  EventPersistenceService eventPersistenceService) {
        this.kafkaProducer = kafkaProducer != null ? kafkaProducer : Optional.empty();
        this.eventPersistenceService = eventPersistenceService;
    }

    public AgentEventEntity dispatchEvent(AgentEventMessage message) {
        if (kafkaProducer.isPresent()) {
            log.debug("Dispatching event {} via Kafka Producer", message.getId());
            kafkaProducer.get().publishEvent(message);
            // In Kafka mode, the consumer will asynchronously persist the event;
            // for immediate API query consistency during synchronous runs, we also return the entity.
            return eventPersistenceService.persistEvent(message);
        } else {
            log.debug("Kafka disabled; dispatching event {} directly to EventPersistenceService", message.getId());
            return eventPersistenceService.persistEvent(message);
        }
    }
}
