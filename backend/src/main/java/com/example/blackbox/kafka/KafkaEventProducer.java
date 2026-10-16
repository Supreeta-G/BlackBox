package com.example.blackbox.kafka;

import com.example.blackbox.dto.AgentEventMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@ConditionalOnProperty(name = "blackbox.kafka.enabled", havingValue = "true", matchIfMissing = false)
public class KafkaEventProducer {

    private static final Logger log = LoggerFactory.getLogger(KafkaEventProducer.class);

    private final KafkaTemplate<String, AgentEventMessage> kafkaTemplate;
    private final String topic;

    public KafkaEventProducer(KafkaTemplate<String, AgentEventMessage> kafkaTemplate,
                              @Value("${blackbox.kafka.topic:ai-agent-events}") String topic) {
        this.kafkaTemplate = kafkaTemplate;
        this.topic = topic;
    }

    public void publishEvent(AgentEventMessage message) {
        log.info("Publishing event {} seq #{} to Kafka topic {} (key={})",
                message.getId(), message.getSequenceNumber(), topic, message.getExecutionId());

        kafkaTemplate.send(topic, message.getExecutionId(), message)
                .whenComplete((result, ex) -> {
                    if (ex != null) {
                        log.error("Failed to publish event {} to Kafka: {}", message.getId(), ex.getMessage());
                    } else {
                        log.debug("Event {} published successfully at offset {}",
                                message.getId(), result.getRecordMetadata().offset());
                    }
                });
    }
}
