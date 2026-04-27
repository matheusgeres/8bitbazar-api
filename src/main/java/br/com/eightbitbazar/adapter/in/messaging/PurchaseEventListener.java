package br.com.eightbitbazar.adapter.in.messaging;

import br.com.eightbitbazar.config.RabbitMQConfig;
import br.com.eightbitbazar.domain.event.PurchaseCompletedEvent;
import io.micrometer.core.instrument.Counter;
import lombok.extern.slf4j.Slf4j;
import static net.logstash.logback.argument.StructuredArguments.kv;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import tools.jackson.databind.json.JsonMapper;

@Slf4j
@Component
public class PurchaseEventListener {

    private final JsonMapper jsonMapper;
    private final Counter purchaseEventsConsumedCounter;

    public PurchaseEventListener(JsonMapper jsonMapper, Counter purchaseEventsConsumedCounter) {
        this.jsonMapper = jsonMapper;
        this.purchaseEventsConsumedCounter = purchaseEventsConsumedCounter;
    }

    @RabbitListener(queues = RabbitMQConfig.PURCHASE_EVENTS_QUEUE)
    public void handlePurchaseEvent(Message message) {
        String eventType = message.getMessageProperties().getHeader("eventType");

        if (!"purchase.completed".equals(eventType)) {
            return;
        }

        try {
            PurchaseCompletedEvent event = jsonMapper.readValue(message.getBody(), PurchaseCompletedEvent.class);
            purchaseEventsConsumedCounter.increment();
            log.info("purchase.event.processed", kv("purchaseId", event.purchaseId()));
        } catch (Exception e) {
            log.error("purchase.event.failed", kv("eventType", eventType), kv("error", e.getMessage() != null ? e.getMessage() : e.toString()), e);
        }
    }
}
