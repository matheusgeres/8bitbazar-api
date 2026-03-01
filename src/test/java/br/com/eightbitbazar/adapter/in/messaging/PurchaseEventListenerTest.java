package br.com.eightbitbazar.adapter.in.messaging;

import br.com.eightbitbazar.domain.event.PurchaseCompletedEvent;
import io.micrometer.core.instrument.Counter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import tools.jackson.databind.json.JsonMapper;

import java.math.BigDecimal;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class PurchaseEventListenerTest {

    private JsonMapper jsonMapper;
    private Counter purchaseEventsConsumedCounter;
    private PurchaseEventListener purchaseEventListener;

    @BeforeEach
    void setUp() {
        jsonMapper = mock(JsonMapper.class);
        purchaseEventsConsumedCounter = mock(Counter.class);
        purchaseEventListener = new PurchaseEventListener(jsonMapper, purchaseEventsConsumedCounter);
    }

    @Test
    void shouldConsumePurchaseCompletedEventAndIncrementCounter() throws Exception {
        PurchaseCompletedEvent event = new PurchaseCompletedEvent(
            10L,
            20L,
            30L,
            40L,
            new BigDecimal("99.90"),
            "PIX"
        );

        MessageProperties properties = new MessageProperties();
        properties.setHeader("eventType", "purchase.completed");
        Message message = new Message("{\"purchaseId\":10}".getBytes(), properties);

        when(jsonMapper.readValue(message.getBody(), PurchaseCompletedEvent.class)).thenReturn(event);

        purchaseEventListener.handlePurchaseEvent(message);

        verify(jsonMapper).readValue(message.getBody(), PurchaseCompletedEvent.class);
        verify(purchaseEventsConsumedCounter).increment();
    }
}
