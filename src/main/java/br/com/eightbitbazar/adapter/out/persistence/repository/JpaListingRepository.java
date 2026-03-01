package br.com.eightbitbazar.adapter.out.persistence.repository;

import br.com.eightbitbazar.adapter.out.persistence.entity.ListingEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface JpaListingRepository extends JpaRepository<ListingEntity, Long> {

    Page<ListingEntity> findBySellerId(Long sellerId, Pageable pageable);

    @Query("""
        SELECT l FROM ListingEntity l
        WHERE l.deletedAt IS NULL
          AND l.type IN :types
          AND l.status = :status
          AND l.auctionEndDate IS NOT NULL
          AND l.auctionEndDate < :now
        """)
    List<ListingEntity> findExpiredActiveAuctions(
        @Param("types") List<String> types,
        @Param("status") String status,
        @Param("now") java.time.LocalDateTime now
    );

    @Query("SELECT l FROM ListingEntity l WHERE l.deletedAt IS NULL " +
           "AND (:search IS NULL OR LOWER(l.name) LIKE LOWER(CONCAT('%', :search, '%'))) " +
           "AND (:type IS NULL OR l.type = :type) " +
           "AND (:platformId IS NULL OR l.platformId = :platformId) " +
           "AND (:manufacturerId IS NULL OR l.manufacturerId = :manufacturerId)")
    Page<ListingEntity> searchListings(
        @Param("search") String search,
        @Param("type") String type,
        @Param("platformId") Long platformId,
        @Param("manufacturerId") Long manufacturerId,
        Pageable pageable
    );
}
