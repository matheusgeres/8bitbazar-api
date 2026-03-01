package br.com.eightbitbazar.application.usecase.listing;

import br.com.eightbitbazar.application.port.out.BidRepository;
import br.com.eightbitbazar.application.port.out.EventPublisher;
import br.com.eightbitbazar.application.port.out.ListingRepository;
import br.com.eightbitbazar.application.port.out.PurchaseRepository;
import br.com.eightbitbazar.domain.bid.Bid;
import br.com.eightbitbazar.domain.event.AuctionEndedEvent;
import br.com.eightbitbazar.domain.event.ListingSoldEvent;
import br.com.eightbitbazar.domain.event.PurchaseCompletedEvent;
import br.com.eightbitbazar.domain.listing.ItemCondition;
import br.com.eightbitbazar.domain.listing.Listing;
import br.com.eightbitbazar.domain.listing.ListingId;
import br.com.eightbitbazar.domain.listing.ListingStatus;
import br.com.eightbitbazar.domain.listing.ListingType;
import br.com.eightbitbazar.domain.purchase.PaymentMethod;
import br.com.eightbitbazar.domain.purchase.Purchase;
import br.com.eightbitbazar.domain.purchase.PurchaseStatus;
import br.com.eightbitbazar.domain.purchase.PurchaseType;
import br.com.eightbitbazar.domain.user.UserId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CloseExpiredAuctionsTest {

    @Mock
    private ListingRepository listingRepository;

    @Mock
    private BidRepository bidRepository;

    @Mock
    private PurchaseRepository purchaseRepository;

    @Mock
    private EventPublisher eventPublisher;

    private CloseExpiredAuctions closeExpiredAuctions;

    @BeforeEach
    void setUp() {
        closeExpiredAuctions = new CloseExpiredAuctions(
            listingRepository,
            bidRepository,
            purchaseRepository,
            eventPublisher
        );
    }

    @Test
    void shouldCreateAuctionWinPurchaseAndMarkListingAsSoldWhenExpiredAuctionHasWinner() {
        Listing expiredAuction = expiredAuctionListing(55L, 20L);
        Bid winningBid = new Bid(
            80L,
            new ListingId(55L),
            new UserId(10L),
            new BigDecimal("150.00"),
            LocalDateTime.of(2026, 3, 1, 9, 0)
        );

        when(listingRepository.findExpiredActiveAuctions(any(LocalDateTime.class))).thenReturn(List.of(expiredAuction));
        when(bidRepository.findHighestBidByListingId(new ListingId(55L))).thenReturn(Optional.of(winningBid));
        when(purchaseRepository.save(any(Purchase.class))).thenAnswer(invocation -> {
            Purchase purchase = invocation.getArgument(0);
            return purchase.withId(999L);
        });

        int processed = closeExpiredAuctions.execute();

        assertEquals(1, processed);
        verify(purchaseRepository).save(argThat(purchase ->
            purchase.listingId().value().equals(55L)
                && purchase.buyerId().value().equals(10L)
                && purchase.sellerId().value().equals(20L)
                && purchase.amount().compareTo(new BigDecimal("150.00")) == 0
                && purchase.type() == PurchaseType.AUCTION_WIN
                && purchase.paymentMethod() == PaymentMethod.OTHER
                && purchase.status() == PurchaseStatus.PENDING
        ));
        verify(listingRepository).save(argThat(listing ->
            listing.id().value().equals(55L)
                && listing.status() == ListingStatus.SOLD
                && listing.quantity() == 0
        ));
        verify(eventPublisher).publish(argThat(event ->
            event instanceof AuctionEndedEvent auctionEndedEvent
                && auctionEndedEvent.listingId().equals(55L)
                && auctionEndedEvent.sellerId().equals(20L)
                && auctionEndedEvent.winnerId().equals(10L)
                && auctionEndedEvent.winningBid().compareTo(new BigDecimal("150.00")) == 0
        ));
        verify(eventPublisher).publish(argThat(event ->
            event instanceof PurchaseCompletedEvent purchaseCompletedEvent
                && purchaseCompletedEvent.purchaseId().equals(999L)
                && purchaseCompletedEvent.listingId().equals(55L)
                && purchaseCompletedEvent.buyerId().equals(10L)
                && purchaseCompletedEvent.sellerId().equals(20L)
                && purchaseCompletedEvent.totalAmount().compareTo(new BigDecimal("150.00")) == 0
                && purchaseCompletedEvent.paymentMethod().equals("OTHER")
        ));
        verify(eventPublisher).publish(argThat(event ->
            event instanceof ListingSoldEvent listingSoldEvent
                && listingSoldEvent.listingId().equals(55L)
                && listingSoldEvent.buyerId().equals(10L)
                && listingSoldEvent.sellerId().equals(20L)
        ));
    }

    @Test
    void shouldExpireListingWithoutCreatingPurchaseWhenExpiredAuctionHasNoBids() {
        Listing expiredAuction = expiredAuctionListing(66L, 21L);

        when(listingRepository.findExpiredActiveAuctions(any(LocalDateTime.class))).thenReturn(List.of(expiredAuction));
        when(bidRepository.findHighestBidByListingId(new ListingId(66L))).thenReturn(Optional.empty());

        int processed = closeExpiredAuctions.execute();

        assertEquals(1, processed);
        verify(purchaseRepository, never()).save(any(Purchase.class));
        verify(listingRepository).save(argThat(listing ->
            listing.id().value().equals(66L)
                && listing.status() == ListingStatus.EXPIRED
        ));
        verify(eventPublisher).publish(argThat(event ->
            event instanceof AuctionEndedEvent auctionEndedEvent
                && auctionEndedEvent.listingId().equals(66L)
                && auctionEndedEvent.sellerId().equals(21L)
                && auctionEndedEvent.winnerId() == null
                && auctionEndedEvent.winningBid() == null
        ));
    }

    private Listing expiredAuctionListing(Long listingId, Long sellerId) {
        return new Listing(
            new ListingId(listingId),
            new UserId(sellerId),
            "Auction Listing " + listingId,
            "Expired auction",
            1L,
            2L,
            ItemCondition.COMPLETE,
            1,
            ListingType.AUCTION,
            null,
            new BigDecimal("80.00"),
            new BigDecimal("150.00"),
            LocalDateTime.of(2026, 2, 28, 10, 0),
            null,
            ListingStatus.ACTIVE,
            List.of(),
            LocalDateTime.of(2026, 2, 27, 10, 0),
            LocalDateTime.of(2026, 2, 28, 10, 0),
            null
        );
    }
}
