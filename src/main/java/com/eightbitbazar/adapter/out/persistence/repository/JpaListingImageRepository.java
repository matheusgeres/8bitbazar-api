package com.eightbitbazar.adapter.out.persistence.repository;

import com.eightbitbazar.adapter.out.persistence.entity.ListingImageEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface JpaListingImageRepository extends JpaRepository<ListingImageEntity, Long> {

    List<ListingImageEntity> findByListingIdOrderByPositionAsc(Long listingId);

    void deleteByListingId(Long listingId);
}
