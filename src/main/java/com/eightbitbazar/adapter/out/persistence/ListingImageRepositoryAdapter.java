package com.eightbitbazar.adapter.out.persistence;

import com.eightbitbazar.adapter.out.persistence.entity.ListingImageEntity;
import com.eightbitbazar.adapter.out.persistence.repository.JpaListingImageRepository;
import com.eightbitbazar.adapter.out.persistence.repository.JpaListingRepository;
import com.eightbitbazar.application.port.out.ListingImageRepository;
import com.eightbitbazar.domain.listing.ListingId;
import com.eightbitbazar.domain.listing.ListingImage;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public class ListingImageRepositoryAdapter implements ListingImageRepository {

    private final JpaListingImageRepository jpaListingImageRepository;
    private final JpaListingRepository jpaListingRepository;

    public ListingImageRepositoryAdapter(
        JpaListingImageRepository jpaListingImageRepository,
        JpaListingRepository jpaListingRepository
    ) {
        this.jpaListingImageRepository = jpaListingImageRepository;
        this.jpaListingRepository = jpaListingRepository;
    }

    @Override
    public ListingImage save(ListingImage image) {
        ListingImageEntity entity = new ListingImageEntity();
        entity.setId(image.id());
        entity.setListing(jpaListingRepository.getReferenceById(image.listingId().value()));
        entity.setUrl(image.url());
        entity.setPosition(image.position());

        ListingImageEntity savedEntity = jpaListingImageRepository.save(entity);

        return new ListingImage(
            savedEntity.getId(),
            image.listingId(),
            savedEntity.getUrl(),
            savedEntity.getPosition()
        );
    }

    @Override
    public List<ListingImage> findByListingId(ListingId listingId) {
        return jpaListingImageRepository.findByListingIdOrderByPositionAsc(listingId.value())
            .stream()
            .map(entity -> new ListingImage(
                entity.getId(),
                listingId,
                entity.getUrl(),
                entity.getPosition()
            ))
            .toList();
    }

    @Override
    @Transactional
    public void deleteByListingId(ListingId listingId) {
        jpaListingImageRepository.deleteByListingId(listingId.value());
    }
}
