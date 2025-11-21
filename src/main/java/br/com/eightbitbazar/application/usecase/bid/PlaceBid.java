package br.com.eightbitbazar.application.usecase.bid;

import br.com.eightbitbazar.application.port.in.PlaceBidUseCase;
import br.com.eightbitbazar.application.port.out.BidRepository;
import br.com.eightbitbazar.application.port.out.EventPublisher;
import br.com.eightbitbazar.application.port.out.ListingRepository;
import br.com.eightbitbazar.application.port.out.PurchaseRepository;
import br.com.eightbitbazar.domain.event.BidPlacedEvent;
import br.com.eightbitbazar.domain.event.ListingSoldEvent;
import br.com.eightbitbazar.domain.event.PurchaseCompletedEvent;
import br.com.eightbitbazar.domain.bid.Bid;
import br.com.eightbitbazar.domain.exception.BusinessException;
import br.com.eightbitbazar.domain.exception.NotFoundException;
import br.com.eightbitbazar.domain.listing.Listing;
import br.com.eightbitbazar.domain.listing.ListingId;
import br.com.eightbitbazar.domain.listing.ListingStatus;
import br.com.eightbitbazar.domain.purchase.PaymentMethod;
import br.com.eightbitbazar.domain.purchase.Purchase;
import br.com.eightbitbazar.domain.purchase.PurchaseStatus;
import br.com.eightbitbazar.domain.purchase.PurchaseType;
import br.com.eightbitbazar.domain.user.UserId;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Transactional
public class PlaceBid implements PlaceBidUseCase {

    private static final BigDecimal MIN_INCREMENT = new BigDecimal("1.00");

    private final BidRepository bidRepository;
    private final ListingRepository listingRepository;
    private final PurchaseRepository purchaseRepository;
    private final EventPublisher eventPublisher;

    public PlaceBid(
            BidRepository bidRepository,
            ListingRepository listingRepository,
            PurchaseRepository purchaseRepository,
            EventPublisher eventPublisher) {
        this.bidRepository = bidRepository;
        this.listingRepository = listingRepository;
        this.purchaseRepository = purchaseRepository;
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

        // Check if bid meets or exceeds buy now price - convert to direct purchase
        if (listing.buyNowPrice() != null && input.amount().compareTo(listing.buyNowPrice()) >= 0) {
            return convertToPurchase(listing, userId, listingId);
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
            savedBid.createdAt(),
            false
        );
    }

    private PlaceBidOutput convertToPurchase(Listing listing, UserId buyerId, ListingId listingId) {
        BigDecimal price = listing.buyNowPrice();

        Purchase purchase = new Purchase(
            null,
            listingId,
            buyerId,
            listing.sellerId(),
            price,
            PurchaseType.AUCTION_WIN,
            PaymentMethod.OTHER,
            BigDecimal.ZERO,
            price,
            PurchaseStatus.PENDING,
            LocalDateTime.now()
        );

        Purchase savedPurchase = purchaseRepository.save(purchase);

        // Mark listing as sold
        Listing soldListing = listing.withStatus(ListingStatus.SOLD).withQuantity(0);
        listingRepository.save(soldListing);

        // Publish events
        eventPublisher.publish(new ListingSoldEvent(
            listingId.value(),
            buyerId.value(),
            listing.sellerId().value()
        ));

        eventPublisher.publish(new PurchaseCompletedEvent(
            savedPurchase.id(),
            savedPurchase.listingId().value(),
            savedPurchase.buyerId().value(),
            savedPurchase.sellerId().value(),
            savedPurchase.finalAmount(),
            savedPurchase.paymentMethod().name()
        ));

        return new PlaceBidOutput(
            savedPurchase.id(),
            savedPurchase.listingId().value(),
            savedPurchase.finalAmount(),
            savedPurchase.createdAt(),
            true
        );
    }
}
