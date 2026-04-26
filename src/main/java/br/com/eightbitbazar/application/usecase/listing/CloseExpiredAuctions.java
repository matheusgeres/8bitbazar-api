package br.com.eightbitbazar.application.usecase.listing;

import br.com.eightbitbazar.application.port.in.CloseExpiredAuctionsUseCase;
import br.com.eightbitbazar.application.port.out.BidRepository;
import br.com.eightbitbazar.application.port.out.EventPublisher;
import br.com.eightbitbazar.application.port.out.ListingRepository;
import br.com.eightbitbazar.application.port.out.PurchaseRepository;
import br.com.eightbitbazar.domain.bid.Bid;
import br.com.eightbitbazar.domain.event.AuctionEndedEvent;
import br.com.eightbitbazar.domain.event.ListingSoldEvent;
import br.com.eightbitbazar.domain.event.PurchaseCompletedEvent;
import br.com.eightbitbazar.domain.listing.Listing;
import br.com.eightbitbazar.domain.listing.ListingId;
import br.com.eightbitbazar.domain.listing.ListingStatus;
import br.com.eightbitbazar.domain.purchase.PaymentMethod;
import br.com.eightbitbazar.domain.purchase.Purchase;
import br.com.eightbitbazar.domain.purchase.PurchaseStatus;
import br.com.eightbitbazar.domain.purchase.PurchaseType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static net.logstash.logback.argument.StructuredArguments.kv;

@Slf4j
@Transactional
public class CloseExpiredAuctions implements CloseExpiredAuctionsUseCase {

    private final ListingRepository listingRepository;
    private final BidRepository bidRepository;
    private final PurchaseRepository purchaseRepository;
    private final EventPublisher eventPublisher;

    public CloseExpiredAuctions(
        ListingRepository listingRepository,
        BidRepository bidRepository,
        PurchaseRepository purchaseRepository,
        EventPublisher eventPublisher
    ) {
        this.listingRepository = listingRepository;
        this.bidRepository = bidRepository;
        this.purchaseRepository = purchaseRepository;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public int execute() {
        log.info("auction.closing.started");

        LocalDateTime now = LocalDateTime.now();
        List<ListingId> candidateIds = listingRepository.findExpiredActiveAuctionIds(now);

        int processed = 0;
        for (ListingId listingId : candidateIds) {
            Optional<Listing> lockedListing = listingRepository.findExpiredActiveAuctionForUpdate(listingId, now);
            if (lockedListing.isEmpty()) {
                continue;
            }
            closeAuction(lockedListing.get());
            processed++;
        }

        log.info("auction.closing.finished", kv("processed", processed));

        return processed;
    }

    private void closeAuction(Listing listing) {
        Optional<Bid> highestBid = bidRepository.findHighestBidByListingId(listing.id());

        if (highestBid.isPresent()) {
            closeWithWinner(listing, highestBid.get());
            return;
        }

        Listing expiredListing = listing.withStatus(ListingStatus.EXPIRED);
        listingRepository.save(expiredListing);

        log.info("auction.closing.expired", kv("listingId", listing.id().value()));

        eventPublisher.publish(new AuctionEndedEvent(
            listing.id().value(),
            listing.sellerId().value(),
            null,
            null
        ));
    }

    private void closeWithWinner(Listing listing, Bid highestBid) {
        Purchase savedPurchase = purchaseRepository.save(new Purchase(
            null,
            listing.id(),
            highestBid.userId(),
            listing.sellerId(),
            highestBid.amount(),
            PurchaseType.AUCTION_WIN,
            PaymentMethod.OTHER,
            java.math.BigDecimal.ZERO,
            highestBid.amount(),
            PurchaseStatus.PENDING,
            LocalDateTime.now()
        ));

        Listing soldListing = listing.withStatus(ListingStatus.SOLD).withQuantity(0);
        listingRepository.save(soldListing);

        log.info("auction.closing.with_winner", kv("listingId", listing.id().value()), kv("winnerId", highestBid.userId().value()), kv("amount", highestBid.amount()));

        eventPublisher.publish(new AuctionEndedEvent(
            listing.id().value(),
            listing.sellerId().value(),
            highestBid.userId().value(),
            highestBid.amount()
        ));
        eventPublisher.publish(new PurchaseCompletedEvent(
            savedPurchase.id(),
            savedPurchase.listingId().value(),
            savedPurchase.buyerId().value(),
            savedPurchase.sellerId().value(),
            savedPurchase.finalAmount(),
            savedPurchase.paymentMethod().name()
        ));
        eventPublisher.publish(new ListingSoldEvent(
            listing.id().value(),
            highestBid.userId().value(),
            listing.sellerId().value()
        ));
    }
}
