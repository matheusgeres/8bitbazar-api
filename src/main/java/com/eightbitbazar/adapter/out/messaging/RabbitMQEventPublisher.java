package com.eightbitbazar.adapter.out.messaging;

import com.eightbitbazar.application.port.out.EventPublisher;
import com.eightbitbazar.config.RabbitMQConfig;
import com.eightbitbazar.domain.event.DomainEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
public class RabbitMQEventPublisher implements EventPublisher {

    private static final Logger log = LoggerFactory.getLogger(RabbitMQEventPublisher.class);

    private final RabbitTemplate rabbitTemplate;
    private final ObjectMapper objectMapper;

    public RabbitMQEventPublisher(RabbitTemplate rabbitTemplate, ObjectMapper objectMapper) {
        this.rabbitTemplate = rabbitTemplate;
        this.objectMapper = objectMapper;
    }

    @Override
    public void publish(DomainEvent event) {
        try {
            String json = objectMapper.writeValueAsString(event);

            MessageProperties properties = new MessageProperties();
            properties.setContentType("application/json");
            properties.setHeader("eventType", event.eventType());

            Message message = new Message(json.getBytes(), properties);

            rabbitTemplate.send(RabbitMQConfig.EXCHANGE_NAME, event.eventType(), message);

            log.info("Published event: {} with routing key: {}", event.getClass().getSimpleName(), event.eventType());
        } catch (Exception e) {
            log.error("Failed to publish event: {}", event.eventType(), e);
        }
    }
}
