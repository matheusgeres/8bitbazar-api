package com.eightbitbazar.adapter.out.persistence;

import com.eightbitbazar.adapter.out.persistence.entity.ListingEntity;
import com.eightbitbazar.adapter.out.persistence.mapper.ListingMapper;
import com.eightbitbazar.adapter.out.persistence.repository.JpaListingRepository;
import com.eightbitbazar.application.port.out.ListingRepository;
import com.eightbitbazar.domain.listing.Listing;
import com.eightbitbazar.domain.listing.ListingId;
import com.eightbitbazar.domain.user.UserId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class ListingRepositoryAdapter implements ListingRepository {

    private final JpaListingRepository jpaListingRepository;
    private final ListingMapper listingMapper;

    public ListingRepositoryAdapter(JpaListingRepository jpaListingRepository, ListingMapper listingMapper) {
        this.jpaListingRepository = jpaListingRepository;
        this.listingMapper = listingMapper;
    }

    @Override
    public Listing save(Listing listing) {
        ListingEntity entity = listingMapper.toEntity(listing);
        ListingEntity savedEntity = jpaListingRepository.save(entity);
        return listingMapper.toDomain(savedEntity);
    }

    @Override
    public Optional<Listing> findById(ListingId id) {
        return jpaListingRepository.findById(id.value())
            .map(listingMapper::toDomain);
    }

    @Override
    public Page<Listing> findAll(ListingSearchCriteria criteria, Pageable pageable) {
        return jpaListingRepository.searchListings(
            criteria.search(),
            criteria.type(),
            criteria.platformId(),
            criteria.manufacturerId(),
            pageable
        ).map(listingMapper::toDomain);
    }

    @Override
    public Page<Listing> findBySellerId(UserId sellerId, Pageable pageable) {
        return jpaListingRepository.findBySellerId(sellerId.value(), pageable)
            .map(listingMapper::toDomain);
    }
}
