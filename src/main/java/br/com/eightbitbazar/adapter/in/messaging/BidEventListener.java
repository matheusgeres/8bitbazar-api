package br.com.eightbitbazar.adapter.in.messaging;

import br.com.eightbitbazar.application.port.out.ListingRepository;
import br.com.eightbitbazar.application.port.out.ListingSearchRepository;
import br.com.eightbitbazar.application.port.out.ListingSearchRepository.ListingSearchResult;
import br.com.eightbitbazar.application.port.out.ManufacturerRepository;
import br.com.eightbitbazar.application.port.out.PlatformRepository;
import br.com.eightbitbazar.application.port.out.UserRepository;
import br.com.eightbitbazar.config.RabbitMQConfig;
import br.com.eightbitbazar.domain.event.BidPlacedEvent;
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
public class BidEventListener {

    private final ListingSearchRepository listingSearchRepository;
    private final ListingRepository listingRepository;
    private final UserRepository userRepository;
    private final PlatformRepository platformRepository;
    private final ManufacturerRepository manufacturerRepository;
    private final JsonMapper jsonMapper;

    public BidEventListener(
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
    @RabbitListener(queues = RabbitMQConfig.BID_EVENTS_QUEUE)
    public void handleBidEvent(Message message) {
        String eventType = message.getMessageProperties().getHeader("eventType");
        String body = new String(message.getBody());

        log.info("bid.event.received", kv("eventType", eventType));

        try {
            if ("bid.placed".equals(eventType)) {
                handleBidPlaced(jsonMapper.readValue(body, BidPlacedEvent.class));
            } else {
                log.warn("bid.event.unknown", kv("eventType", eventType));
            }
        } catch (Exception e) {
            log.error("bid.event.failed", kv("eventType", eventType), kv("error", e.getMessage() != null ? e.getMessage() : e.toString()), e);
        }
    }

    private void handleBidPlaced(BidPlacedEvent event) {
        log.info("bid.event.processing", kv("listingId", event.listingId()));

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
        log.info("bid.reindex.completed", kv("listingId", event.listingId()));
    }
}
