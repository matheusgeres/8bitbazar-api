package br.com.eightbitbazar.config;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MetricsConfig {

    @Bean
    public Counter listingsCreatedCounter(MeterRegistry registry) {
        // Avoid Prometheus reserved suffix `_created` by ending with `.count`
        return Counter.builder("eightbitbazar.listings.created.count")
                .description("Total number of listings created")
                .register(registry);
    }

    @Bean
    public Counter listingsSoldCounter(MeterRegistry registry) {
        return Counter.builder("eightbitbazar.listings.sold")
                .description("Total number of listings sold")
                .register(registry);
    }

    @Bean
    public Counter bidsPlacedCounter(MeterRegistry registry) {
        return Counter.builder("eightbitbazar.bids.placed")
                .description("Total number of bids placed")
                .register(registry);
    }

    @Bean
    public Counter purchasesCompletedCounter(MeterRegistry registry) {
        return Counter.builder("eightbitbazar.purchases.completed")
                .description("Total number of purchases completed")
                .register(registry);
    }

    @Bean
    public Counter usersRegisteredCounter(MeterRegistry registry) {
        return Counter.builder("eightbitbazar.users.registered")
                .description("Total number of users registered")
                .register(registry);
    }
}
