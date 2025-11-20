package com.eightbitbazar.application.usecase.purchase;

import com.eightbitbazar.application.port.in.DirectPurchaseUseCase;
import com.eightbitbazar.application.port.out.ListingRepository;
import com.eightbitbazar.application.port.out.PurchaseRepository;
import com.eightbitbazar.application.port.out.EventPublisher;
import com.eightbitbazar.domain.event.ListingSoldEvent;
import com.eightbitbazar.domain.event.PurchaseCompletedEvent;
import com.eightbitbazar.domain.exception.BusinessException;
import com.eightbitbazar.domain.exception.NotFoundException;
import com.eightbitbazar.domain.listing.Listing;
import com.eightbitbazar.domain.listing.ListingId;
import com.eightbitbazar.domain.listing.ListingStatus;
import com.eightbitbazar.domain.purchase.PaymentMethod;
import com.eightbitbazar.domain.purchase.Purchase;
import com.eightbitbazar.domain.purchase.PurchaseStatus;
import com.eightbitbazar.domain.purchase.PurchaseType;
import com.eightbitbazar.domain.user.UserId;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;

@Transactional
public class DirectPurchase implements DirectPurchaseUseCase {

    private final PurchaseRepository purchaseRepository;
    private final ListingRepository listingRepository;
    private final EventPublisher eventPublisher;

    public DirectPurchase(
        PurchaseRepository purchaseRepository,
        ListingRepository listingRepository,
        EventPublisher eventPublisher
    ) {
        this.purchaseRepository = purchaseRepository;
        this.listingRepository = listingRepository;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public DirectPurchaseOutput execute(UserId buyerId, DirectPurchaseInput input) {
        ListingId listingId = new ListingId(input.listingId());

        Listing listing = listingRepository.findById(listingId)
            .filter(l -> !l.isDeleted())
            .orElseThrow(() -> new NotFoundException("Listing not found"));

        if (!listing.isActive()) {
            throw new BusinessException("This listing is no longer active");
        }

        if (listing.isShowcase()) {
            throw new BusinessException("This listing is showcase only, not for sale");
        }

        if (listing.sellerId().equals(buyerId)) {
            throw new BusinessException("You cannot buy your own listing");
        }

        BigDecimal price;
        if (listing.isAuction()) {
            if (listing.buyNowPrice() == null) {
                throw new BusinessException("This auction does not have a buy now option");
            }
            price = listing.buyNowPrice();
        } else {
            price = listing.price();
        }

        PaymentMethod paymentMethod = PaymentMethod.valueOf(input.paymentMethod());
        BigDecimal discountApplied = BigDecimal.ZERO;
        BigDecimal finalAmount = price;

        if ((paymentMethod == PaymentMethod.PIX || paymentMethod == PaymentMethod.CASH) && listing.hasCashDiscount()) {
            discountApplied = price.multiply(listing.cashDiscountPercent()).divide(BigDecimal.valueOf(100), 2, RoundingMode.FLOOR);
            finalAmount = price.subtract(discountApplied);
        }

        Purchase purchase = new Purchase(
            null,
            listingId,
            buyerId,
            listing.sellerId(),
            price,
            PurchaseType.DIRECT,
            paymentMethod,
            discountApplied,
            finalAmount,
            PurchaseStatus.PENDING,
            LocalDateTime.now()
        );

        Purchase savedPurchase = purchaseRepository.save(purchase);

        // Update listing quantity
        int newQuantity = listing.quantity() - 1;
        boolean isSold = newQuantity <= 0;
        Listing updatedListing = isSold
            ? listing.withStatus(ListingStatus.SOLD).withQuantity(0)
            : listing.withQuantity(newQuantity);
        listingRepository.save(updatedListing);

        if (isSold) {
            eventPublisher.publish(new ListingSoldEvent(
                listingId.value(),
                buyerId.value(),
                listing.sellerId().value()
            ));
        }

        eventPublisher.publish(new PurchaseCompletedEvent(
            savedPurchase.id(),
            savedPurchase.listingId().value(),
            savedPurchase.buyerId().value(),
            savedPurchase.sellerId().value(),
            savedPurchase.finalAmount(),
            savedPurchase.paymentMethod().name()
        ));

        return new DirectPurchaseOutput(
            savedPurchase.id(),
            savedPurchase.listingId().value(),
            savedPurchase.amount(),
            savedPurchase.discountApplied(),
            savedPurchase.finalAmount(),
            savedPurchase.paymentMethod().name(),
            savedPurchase.status().name(),
            savedPurchase.createdAt()
        );
    }
}
