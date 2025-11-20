package com.eightbitbazar.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String EXCHANGE_NAME = "eightbitbazar.events";

    // Queues
    public static final String LISTING_EVENTS_QUEUE = "listing.events";
    public static final String BID_EVENTS_QUEUE = "bid.events";
    public static final String PURCHASE_EVENTS_QUEUE = "purchase.events";
    public static final String NOTIFICATION_QUEUE = "notifications";

    @Bean
    public TopicExchange eventsExchange() {
        return new TopicExchange(EXCHANGE_NAME);
    }

    @Bean
    public Queue listingEventsQueue() {
        return QueueBuilder.durable(LISTING_EVENTS_QUEUE).build();
    }

    @Bean
    public Queue bidEventsQueue() {
        return QueueBuilder.durable(BID_EVENTS_QUEUE).build();
    }

    @Bean
    public Queue purchaseEventsQueue() {
        return QueueBuilder.durable(PURCHASE_EVENTS_QUEUE).build();
    }

    @Bean
    public Queue notificationQueue() {
        return QueueBuilder.durable(NOTIFICATION_QUEUE).build();
    }

    @Bean
    public Binding listingCreatedBinding(Queue listingEventsQueue, TopicExchange eventsExchange) {
        return BindingBuilder.bind(listingEventsQueue).to(eventsExchange).with("listing.*");
    }

    @Bean
    public Binding bidPlacedBinding(Queue bidEventsQueue, TopicExchange eventsExchange) {
        return BindingBuilder.bind(bidEventsQueue).to(eventsExchange).with("bid.*");
    }

    @Bean
    public Binding purchaseBinding(Queue purchaseEventsQueue, TopicExchange eventsExchange) {
        return BindingBuilder.bind(purchaseEventsQueue).to(eventsExchange).with("purchase.*");
    }

    @Bean
    public Binding auctionEndedBinding(Queue notificationQueue, TopicExchange eventsExchange) {
        return BindingBuilder.bind(notificationQueue).to(eventsExchange).with("auction.*");
    }

    @Bean
    public Binding notificationBidBinding(Queue notificationQueue, TopicExchange eventsExchange) {
        return BindingBuilder.bind(notificationQueue).to(eventsExchange).with("bid.placed");
    }

    @Bean
    public Binding notificationPurchaseBinding(Queue notificationQueue, TopicExchange eventsExchange) {
        return BindingBuilder.bind(notificationQueue).to(eventsExchange).with("purchase.completed");
    }

    @Bean
    public Jackson2JsonMessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(messageConverter());
        return template;
    }
}
