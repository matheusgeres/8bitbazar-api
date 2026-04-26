package br.com.eightbitbazar.adapter.out.persistence;

import br.com.eightbitbazar.adapter.out.persistence.entity.ListingEntity;
import br.com.eightbitbazar.adapter.out.persistence.mapper.ListingMapper;
import br.com.eightbitbazar.adapter.out.persistence.repository.JpaListingRepository;
import br.com.eightbitbazar.application.port.out.ListingRepository;
import br.com.eightbitbazar.domain.listing.Listing;
import br.com.eightbitbazar.domain.listing.ListingId;
import br.com.eightbitbazar.domain.user.UserId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

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
    public List<Listing> findByIds(Set<ListingId> ids) {
        if (ids.isEmpty()) {
            return List.of();
        }

        return jpaListingRepository.findAllById(ids.stream().map(ListingId::value).toList()).stream()
            .map(listingMapper::toDomain)
            .toList();
    }

    @Override
    public List<ListingId> findExpiredActiveAuctionIds(LocalDateTime now) {
        return jpaListingRepository.findExpiredActiveAuctionIds("AUCTION", "ACTIVE", now).stream()
            .map(ListingId::new)
            .toList();
    }

    @Override
    public Optional<Listing> findExpiredActiveAuctionForUpdate(ListingId id, LocalDateTime now) {
        return jpaListingRepository.findExpiredActiveAuctionForUpdate(id.value(), "AUCTION", "ACTIVE", now)
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
