package br.com.eightbitbazar.application.usecase.listing;

import br.com.eightbitbazar.application.port.in.CreateListingUseCase;
import br.com.eightbitbazar.application.port.out.EventPublisher;
import br.com.eightbitbazar.application.port.out.ListingRepository;
import br.com.eightbitbazar.application.port.out.ManufacturerRepository;
import br.com.eightbitbazar.application.port.out.PlatformRepository;
import br.com.eightbitbazar.application.port.out.UserRepository;
import br.com.eightbitbazar.domain.event.ListingCreatedEvent;
import br.com.eightbitbazar.domain.exception.BusinessException;
import br.com.eightbitbazar.domain.exception.NotFoundException;
import br.com.eightbitbazar.domain.listing.*;
import br.com.eightbitbazar.domain.manufacturer.Manufacturer;
import br.com.eightbitbazar.domain.platform.Platform;
import br.com.eightbitbazar.domain.user.User;
import br.com.eightbitbazar.domain.user.UserId;

import java.time.LocalDateTime;
import java.util.List;

public class CreateListing implements CreateListingUseCase {

    private final ListingRepository listingRepository;
    private final UserRepository userRepository;
    private final PlatformRepository platformRepository;
    private final ManufacturerRepository manufacturerRepository;
    private final EventPublisher eventPublisher;

    public CreateListing(
        ListingRepository listingRepository,
        UserRepository userRepository,
        PlatformRepository platformRepository,
        ManufacturerRepository manufacturerRepository,
        EventPublisher eventPublisher
    ) {
        this.listingRepository = listingRepository;
        this.userRepository = userRepository;
        this.platformRepository = platformRepository;
        this.manufacturerRepository = manufacturerRepository;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public CreateListingOutput execute(UserId sellerId, CreateListingInput input) {
        User seller = userRepository.findById(sellerId)
            .filter(u -> !u.isDeleted())
            .orElseThrow(() -> new NotFoundException("User not found"));

        if (!seller.isSeller()) {
            throw new BusinessException("User is not registered as a seller");
        }

        Platform platform = platformRepository.findById(input.platformId())
            .orElseThrow(() -> new NotFoundException("Platform not found"));

        Manufacturer manufacturer = manufacturerRepository.findById(input.manufacturerId())
            .orElseThrow(() -> new NotFoundException("Manufacturer not found"));

        ListingType type = ListingType.valueOf(input.type());
        ItemCondition condition = ItemCondition.valueOf(input.condition());

        validateListingInput(input, type);

        Listing listing = new Listing(
            null,
            sellerId,
            input.name(),
            input.description(),
            input.platformId(),
            input.manufacturerId(),
            condition,
            input.quantity(),
            type,
            input.price(),
            input.startingPrice(),
            input.buyNowPrice(),
            input.auctionEndDate(),
            input.cashDiscountPercent(),
            ListingStatus.ACTIVE,
            List.of(),
            LocalDateTime.now(),
            LocalDateTime.now(),
            null
        );

        Listing savedListing = listingRepository.save(listing);

        eventPublisher.publish(new ListingCreatedEvent(
            savedListing.id().value(),
            sellerId.value(),
            savedListing.name(),
            savedListing.type().name()
        ));

        return new CreateListingOutput(
            savedListing.id().value(),
            savedListing.name(),
            savedListing.description(),
            platform.name(),
            manufacturer.name(),
            savedListing.condition().name(),
            savedListing.quantity(),
            savedListing.type().name(),
            savedListing.price(),
            savedListing.startingPrice(),
            savedListing.buyNowPrice(),
            savedListing.auctionEndDate(),
            savedListing.cashDiscountPercent(),
            savedListing.status().name(),
            savedListing.createdAt()
        );
    }

    private void validateListingInput(CreateListingInput input, ListingType type) {
        if (type == ListingType.DIRECT_SALE && input.price() == null) {
            throw new BusinessException("Price is required for direct sale");
        }
        if (type == ListingType.AUCTION) {
            if (input.startingPrice() == null) {
                throw new BusinessException("Starting price is required for auction");
            }
            if (input.auctionEndDate() == null) {
                throw new BusinessException("Auction end date is required");
            }
            if (input.auctionEndDate().isBefore(LocalDateTime.now())) {
                throw new BusinessException("Auction end date must be in the future");
            }
        }
    }
}
