package br.com.eightbitbazar.adapter.in.messaging;

import br.com.eightbitbazar.application.port.out.ListingRepository;
import br.com.eightbitbazar.application.port.out.ListingSearchRepository;
import br.com.eightbitbazar.application.port.out.ListingSearchRepository.ListingSearchResult;
import br.com.eightbitbazar.application.port.out.ManufacturerRepository;
import br.com.eightbitbazar.application.port.out.PlatformRepository;
import br.com.eightbitbazar.application.port.out.UserRepository;
import br.com.eightbitbazar.config.RabbitMQConfig;
import br.com.eightbitbazar.domain.event.ListingCreatedEvent;
import br.com.eightbitbazar.domain.event.ListingDeletedEvent;
import br.com.eightbitbazar.domain.event.ListingSoldEvent;
import br.com.eightbitbazar.domain.listing.Listing;
import br.com.eightbitbazar.domain.listing.ListingId;
import br.com.eightbitbazar.domain.manufacturer.Manufacturer;
import br.com.eightbitbazar.domain.platform.Platform;
import br.com.eightbitbazar.domain.user.User;
import tools.jackson.databind.json.JsonMapper;
import lombok.extern.slf4j.Slf4j;
import static net.logstash.logback.argument.StructuredArguments.kv;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZoneOffset;
import java.util.List;

@Slf4j
@Component
public class ListingEventListener {

    private final ListingSearchRepository listingSearchRepository;
    private final ListingRepository listingRepository;
    private final UserRepository userRepository;
    private final PlatformRepository platformRepository;
    private final ManufacturerRepository manufacturerRepository;
    private final JsonMapper jsonMapper;

    public ListingEventListener(
            ListingSearchRepository listingSearchRepository,
            ListingRepository listingRepository,
            UserRepository userRepository,
            PlatformRepository platformRepository,
            ManufacturerRepository manufacturerRepository,
            JsonMapper jsonMapper) {
        this.listingSearchRepository = listingSearchRepository;
        this.listingRepository = listingRepository;
        this.userRepository = userRepository;
        this.platformRepository = platformRepository;
        this.manufacturerRepository = manufacturerRepository;
        this.jsonMapper = jsonMapper;
    }

    @Transactional(readOnly = true)
    @RabbitListener(queues = RabbitMQConfig.LISTING_EVENTS_QUEUE)
    public void handleListingEvent(Message message) {
        String eventType = message.getMessageProperties().getHeader("eventType");
        String body = new String(message.getBody());

        log.info("listing.event.received", kv("eventType", eventType));

        try {
            switch (eventType) {
                case "listing.created" -> handleListingCreated(jsonMapper.readValue(body, ListingCreatedEvent.class));
                case "listing.deleted" -> handleListingDeleted(jsonMapper.readValue(body, ListingDeletedEvent.class));
                case "listing.sold" -> handleListingSold(jsonMapper.readValue(body, ListingSoldEvent.class));
                default -> log.warn("listing.event.unknown", kv("eventType", eventType));
            }
        } catch (Exception e) {
            log.error("listing.event.failed", kv("eventType", eventType), kv("error", e.getMessage() != null ? e.getMessage() : e.toString()), e);
        }
    }

    private void handleListingCreated(ListingCreatedEvent event) {
        log.info("listing.created.processing", kv("listingId", event.listingId()));

        Listing listing = listingRepository.findById(new ListingId(event.listingId()))
                .orElseThrow(() -> new IllegalStateException("Listing not found: " + event.listingId()));

        User seller = userRepository.findById(listing.sellerId())
                .orElseThrow(() -> new IllegalStateException("Seller not found: " + listing.sellerId()));

        Platform platform = platformRepository.findById(listing.platformId())
                .orElseThrow(() -> new IllegalStateException("Platform not found: " + listing.platformId()));

        Manufacturer manufacturer = manufacturerRepository.findById(listing.manufacturerId())
                .orElseThrow(() -> new IllegalStateException("Manufacturer not found: " + listing.manufacturerId()));

        ListingSearchResult searchResult = new ListingSearchResult(
                listing.id().value(),
                listing.name(),
                listing.description(),
                listing.platformId(),
                platform.name(),
                listing.manufacturerId(),
                manufacturer.name(),
                listing.condition().name(),
                listing.quantity(),
                listing.type().name(),
                listing.price(),
                listing.startingPrice(),
                listing.buyNowPrice(),
                listing.auctionEndDate() != null ? listing.auctionEndDate().toInstant(ZoneOffset.UTC) : null,
                listing.cashDiscountPercent(),
                listing.status().name(),
                listing.sellerId().value(),
                seller.nickname(),
                List.of(),
                listing.createdAt().toInstant(ZoneOffset.UTC)
        );

        listingSearchRepository.index(searchResult);
        log.info("listing.index.completed", kv("listingId", event.listingId()));
    }

    private void handleListingDeleted(ListingDeletedEvent event) {
        log.info("listing.deleted.processing", kv("listingId", event.listingId()));
        listingSearchRepository.delete(event.listingId());
        log.info("listing.index.removed", kv("listingId", event.listingId()));
    }

    private void handleListingSold(ListingSoldEvent event) {
        log.info("listing.sold.processing", kv("listingId", event.listingId()));
        listingSearchRepository.delete(event.listingId());
        log.info("listing.sold.index.removed", kv("listingId", event.listingId()));
    }
}
