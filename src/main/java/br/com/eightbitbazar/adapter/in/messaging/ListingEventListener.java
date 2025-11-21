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
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
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
    private final ObjectMapper objectMapper;

    public ListingEventListener(
            ListingSearchRepository listingSearchRepository,
            ListingRepository listingRepository,
            UserRepository userRepository,
            PlatformRepository platformRepository,
            ManufacturerRepository manufacturerRepository,
            ObjectMapper objectMapper) {
        this.listingSearchRepository = listingSearchRepository;
        this.listingRepository = listingRepository;
        this.userRepository = userRepository;
        this.platformRepository = platformRepository;
        this.manufacturerRepository = manufacturerRepository;
        this.objectMapper = objectMapper;
    }

    @Transactional(readOnly = true)
    @RabbitListener(queues = RabbitMQConfig.LISTING_EVENTS_QUEUE)
    public void handleListingEvent(Message message) {
        String eventType = message.getMessageProperties().getHeader("eventType");
        String body = new String(message.getBody());

        log.info("Received listing event: {}", eventType);

        try {
            switch (eventType) {
                case "listing.created" -> handleListingCreated(objectMapper.readValue(body, ListingCreatedEvent.class));
                case "listing.deleted" -> handleListingDeleted(objectMapper.readValue(body, ListingDeletedEvent.class));
                case "listing.sold" -> handleListingSold(objectMapper.readValue(body, ListingSoldEvent.class));
                default -> log.warn("Unknown event type: {}", eventType);
            }
        } catch (Exception e) {
            log.error("Failed to process listing event: {}", eventType, e);
        }
    }

    private void handleListingCreated(ListingCreatedEvent event) {
        log.info("Processing listing created: {}", event.listingId());

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
        log.info("Successfully indexed listing: {}", event.listingId());
    }

    private void handleListingDeleted(ListingDeletedEvent event) {
        log.info("Processing listing deleted: {}", event.listingId());
        listingSearchRepository.delete(event.listingId());
        log.info("Successfully removed listing from index: {}", event.listingId());
    }

    private void handleListingSold(ListingSoldEvent event) {
        log.info("Processing listing sold: {}", event.listingId());
        listingSearchRepository.delete(event.listingId());
        log.info("Successfully removed sold listing from index: {}", event.listingId());
    }
}
