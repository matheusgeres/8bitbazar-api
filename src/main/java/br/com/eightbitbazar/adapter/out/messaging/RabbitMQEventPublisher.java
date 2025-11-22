package br.com.eightbitbazar.adapter.out.messaging;

import br.com.eightbitbazar.application.port.out.EventPublisher;
import br.com.eightbitbazar.config.RabbitMQConfig;
import br.com.eightbitbazar.domain.event.DomainEvent;
import tools.jackson.databind.json.JsonMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class RabbitMQEventPublisher implements EventPublisher {

    private final RabbitTemplate rabbitTemplate;
    private final JsonMapper jsonMapper;

    public RabbitMQEventPublisher(RabbitTemplate rabbitTemplate, JsonMapper jsonMapper) {
        this.rabbitTemplate = rabbitTemplate;
        this.jsonMapper = jsonMapper;
    }

    @Override
    public void publish(DomainEvent event) {
        try {
            String json = jsonMapper.writeValueAsString(event);

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
