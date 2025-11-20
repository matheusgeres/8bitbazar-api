package com.eightbitbazar.adapter.in.messaging;

import com.eightbitbazar.application.port.out.ListingRepository;
import com.eightbitbazar.application.port.out.ListingSearchRepository;
import com.eightbitbazar.application.port.out.ListingSearchRepository.ListingSearchResult;
import com.eightbitbazar.application.port.out.ManufacturerRepository;
import com.eightbitbazar.application.port.out.PlatformRepository;
import com.eightbitbazar.application.port.out.UserRepository;
import com.eightbitbazar.config.RabbitMQConfig;
import com.eightbitbazar.domain.event.ListingCreatedEvent;
import com.eightbitbazar.domain.event.ListingDeletedEvent;
import com.eightbitbazar.domain.listing.Listing;
import com.eightbitbazar.domain.listing.ListingId;
import com.eightbitbazar.domain.manufacturer.Manufacturer;
import com.eightbitbazar.domain.platform.Platform;
import com.eightbitbazar.domain.user.User;
import lombok.extern.slf4j.Slf4j;
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

    public ListingEventListener(
            ListingSearchRepository listingSearchRepository,
            ListingRepository listingRepository,
            UserRepository userRepository,
            PlatformRepository platformRepository,
            ManufacturerRepository manufacturerRepository) {
        this.listingSearchRepository = listingSearchRepository;
        this.listingRepository = listingRepository;
        this.userRepository = userRepository;
        this.platformRepository = platformRepository;
        this.manufacturerRepository = manufacturerRepository;
    }

    @Transactional(readOnly = true)
    @RabbitListener(queues = RabbitMQConfig.LISTING_EVENTS_QUEUE)
    public void handleListingCreated(ListingCreatedEvent event) {
        log.info("Received listing created event: {}", event.listingId());

        try {
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

        } catch (Exception e) {
            log.error("Failed to index listing: {}", event.listingId(), e);
        }
    }

    @RabbitListener(queues = RabbitMQConfig.LISTING_EVENTS_QUEUE)
    public void handleListingDeleted(ListingDeletedEvent event) {
        log.info("Received listing deleted event: {}", event.listingId());

        try {
            listingSearchRepository.delete(event.listingId());
            log.info("Successfully removed listing from index: {}", event.listingId());
        } catch (Exception e) {
            log.error("Failed to remove listing from index: {}", event.listingId(), e);
        }
    }
}
