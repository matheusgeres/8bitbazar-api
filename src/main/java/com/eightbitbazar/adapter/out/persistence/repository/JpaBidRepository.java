package com.eightbitbazar.adapter.out.persistence.repository;

import com.eightbitbazar.adapter.out.persistence.entity.BidEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface JpaBidRepository extends JpaRepository<BidEntity, Long> {

    List<BidEntity> findByListingIdOrderByAmountDesc(Long listingId);

    Optional<BidEntity> findFirstByListingIdOrderByAmountDesc(Long listingId);
}
