package com.eightbitbazar.adapter.out.persistence;

import com.eightbitbazar.adapter.out.persistence.entity.BidEntity;
import com.eightbitbazar.adapter.out.persistence.repository.JpaBidRepository;
import com.eightbitbazar.application.port.out.BidRepository;
import com.eightbitbazar.domain.bid.Bid;
import com.eightbitbazar.domain.listing.ListingId;
import com.eightbitbazar.domain.user.UserId;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class BidRepositoryAdapter implements BidRepository {

    private final JpaBidRepository jpaBidRepository;

    public BidRepositoryAdapter(JpaBidRepository jpaBidRepository) {
        this.jpaBidRepository = jpaBidRepository;
    }

    @Override
    public Bid save(Bid bid) {
        BidEntity entity = toEntity(bid);
        BidEntity savedEntity = jpaBidRepository.save(entity);
        return toDomain(savedEntity);
    }

    @Override
    public List<Bid> findByListingIdOrderByAmountDesc(ListingId listingId) {
        return jpaBidRepository.findByListingIdOrderByAmountDesc(listingId.value())
            .stream().map(this::toDomain).toList();
    }

    @Override
    public Optional<Bid> findHighestBidByListingId(ListingId listingId) {
        return jpaBidRepository.findFirstByListingIdOrderByAmountDesc(listingId.value())
            .map(this::toDomain);
    }

    private Bid toDomain(BidEntity entity) {
        return new Bid(
            entity.getId(),
            new ListingId(entity.getListingId()),
            new UserId(entity.getUserId()),
            entity.getAmount(),
            entity.getCreatedAt()
        );
    }

    private BidEntity toEntity(Bid domain) {
        BidEntity entity = new BidEntity();
        entity.setId(domain.id());
        entity.setListingId(domain.listingId().value());
        entity.setUserId(domain.userId().value());
        entity.setAmount(domain.amount());
        entity.setCreatedAt(domain.createdAt());
        return entity;
    }
}
