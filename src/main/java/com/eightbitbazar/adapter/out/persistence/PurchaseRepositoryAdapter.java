package com.eightbitbazar.adapter.out.persistence;

import com.eightbitbazar.adapter.out.persistence.entity.PurchaseEntity;
import com.eightbitbazar.adapter.out.persistence.repository.JpaPurchaseRepository;
import com.eightbitbazar.application.port.out.PurchaseRepository;
import com.eightbitbazar.domain.listing.ListingId;
import com.eightbitbazar.domain.purchase.*;
import com.eightbitbazar.domain.user.UserId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

@Repository
public class PurchaseRepositoryAdapter implements PurchaseRepository {

    private final JpaPurchaseRepository jpaPurchaseRepository;

    public PurchaseRepositoryAdapter(JpaPurchaseRepository jpaPurchaseRepository) {
        this.jpaPurchaseRepository = jpaPurchaseRepository;
    }

    @Override
    public Purchase save(Purchase purchase) {
        PurchaseEntity entity = toEntity(purchase);
        PurchaseEntity savedEntity = jpaPurchaseRepository.save(entity);
        return toDomain(savedEntity);
    }

    @Override
    public Page<Purchase> findByBuyerId(UserId buyerId, Pageable pageable) {
        return jpaPurchaseRepository.findByBuyerId(buyerId.value(), pageable)
            .map(this::toDomain);
    }

    @Override
    public Page<Purchase> findBySellerId(UserId sellerId, Pageable pageable) {
        return jpaPurchaseRepository.findBySellerId(sellerId.value(), pageable)
            .map(this::toDomain);
    }

    private Purchase toDomain(PurchaseEntity entity) {
        return new Purchase(
            entity.getId(),
            new ListingId(entity.getListingId()),
            new UserId(entity.getBuyerId()),
            new UserId(entity.getSellerId()),
            entity.getAmount(),
            PurchaseType.valueOf(entity.getType()),
            entity.getPaymentMethod() != null ? PaymentMethod.valueOf(entity.getPaymentMethod()) : null,
            entity.getDiscountApplied(),
            entity.getFinalAmount(),
            PurchaseStatus.valueOf(entity.getStatus()),
            entity.getCreatedAt()
        );
    }

    private PurchaseEntity toEntity(Purchase domain) {
        PurchaseEntity entity = new PurchaseEntity();
        entity.setId(domain.id());
        entity.setListingId(domain.listingId().value());
        entity.setBuyerId(domain.buyerId().value());
        entity.setSellerId(domain.sellerId().value());
        entity.setAmount(domain.amount());
        entity.setType(domain.type().name());
        entity.setPaymentMethod(domain.paymentMethod() != null ? domain.paymentMethod().name() : null);
        entity.setDiscountApplied(domain.discountApplied());
        entity.setFinalAmount(domain.finalAmount());
        entity.setStatus(domain.status().name());
        entity.setCreatedAt(domain.createdAt());
        return entity;
    }
}
