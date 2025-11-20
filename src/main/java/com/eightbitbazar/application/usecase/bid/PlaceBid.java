package com.eightbitbazar.application.usecase.bid;

import com.eightbitbazar.application.port.in.PlaceBidUseCase;
import com.eightbitbazar.application.port.out.BidRepository;
import com.eightbitbazar.application.port.out.EventPublisher;
import com.eightbitbazar.application.port.out.ListingRepository;
import com.eightbitbazar.domain.event.BidPlacedEvent;
import com.eightbitbazar.domain.bid.Bid;
import com.eightbitbazar.domain.exception.BusinessException;
import com.eightbitbazar.domain.exception.NotFoundException;
import com.eightbitbazar.domain.listing.Listing;
import com.eightbitbazar.domain.listing.ListingId;
import com.eightbitbazar.domain.user.UserId;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class PlaceBid implements PlaceBidUseCase {

    private static final BigDecimal MIN_INCREMENT = new BigDecimal("1.00");

    private final BidRepository bidRepository;
    private final ListingRepository listingRepository;
    private final EventPublisher eventPublisher;

    public PlaceBid(BidRepository bidRepository, ListingRepository listingRepository, EventPublisher eventPublisher) {
        this.bidRepository = bidRepository;
        this.listingRepository = listingRepository;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public PlaceBidOutput execute(UserId userId, PlaceBidInput input) {
        ListingId listingId = new ListingId(input.listingId());

        Listing listing = listingRepository.findById(listingId)
            .filter(l -> !l.isDeleted())
            .orElseThrow(() -> new NotFoundException("Listing not found"));

        if (!listing.isAuction()) {
            throw new BusinessException("This listing is not an auction");
        }

        if (!listing.isActive()) {
            throw new BusinessException("This auction is no longer active");
        }

        if (listing.hasAuctionEnded()) {
            throw new BusinessException("This auction has ended");
        }

        if (listing.sellerId().equals(userId)) {
            throw new BusinessException("You cannot bid on your own listing");
        }

        BigDecimal minimumBid = bidRepository.findHighestBidByListingId(listingId)
            .map(bid -> bid.amount().add(MIN_INCREMENT))
            .orElse(listing.startingPrice());

        if (input.amount().compareTo(minimumBid) < 0) {
            throw new BusinessException("Bid must be at least " + minimumBid);
        }

        Bid bid = new Bid(
            null,
            listingId,
            userId,
            input.amount(),
            LocalDateTime.now()
        );

        Bid savedBid = bidRepository.save(bid);

        eventPublisher.publish(new BidPlacedEvent(
            savedBid.id(),
            savedBid.listingId().value(),
            savedBid.userId().value(),
            savedBid.amount()
        ));

        return new PlaceBidOutput(
            savedBid.id(),
            savedBid.listingId().value(),
            savedBid.amount(),
            savedBid.createdAt()
        );
    }
}
