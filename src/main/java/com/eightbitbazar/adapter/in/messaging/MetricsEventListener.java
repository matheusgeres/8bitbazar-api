package com.eightbitbazar.adapter.in.messaging;

import com.eightbitbazar.config.RabbitMQConfig;
import io.micrometer.core.instrument.Counter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class MetricsEventListener {

    private final Counter listingsCreatedCounter;
    private final Counter listingsSoldCounter;
    private final Counter bidsPlacedCounter;
    private final Counter purchasesCompletedCounter;

    public MetricsEventListener(
            Counter listingsCreatedCounter,
            Counter listingsSoldCounter,
            Counter bidsPlacedCounter,
            Counter purchasesCompletedCounter) {
        this.listingsCreatedCounter = listingsCreatedCounter;
        this.listingsSoldCounter = listingsSoldCounter;
        this.bidsPlacedCounter = bidsPlacedCounter;
        this.purchasesCompletedCounter = purchasesCompletedCounter;
    }

    @RabbitListener(queues = RabbitMQConfig.NOTIFICATION_QUEUE)
    public void handleNotificationEvents(Message message) {
        String eventType = message.getMessageProperties().getHeader("eventType");

        if (eventType == null) {
            return;
        }

        switch (eventType) {
            case "listing.created" -> {
                listingsCreatedCounter.increment();
                log.debug("Incremented listings created counter");
            }
            case "listing.sold" -> {
                listingsSoldCounter.increment();
                log.debug("Incremented listings sold counter");
            }
            case "bid.placed" -> {
                bidsPlacedCounter.increment();
                log.debug("Incremented bids placed counter");
            }
            case "purchase.completed" -> {
                purchasesCompletedCounter.increment();
                log.debug("Incremented purchases completed counter");
            }
        }
    }
}
