package br.com.eightbitbazar.adapter.out.persistence.repository;

import br.com.eightbitbazar.adapter.out.persistence.entity.PurchaseEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaPurchaseRepository extends JpaRepository<PurchaseEntity, Long> {

    Page<PurchaseEntity> findByBuyerId(Long buyerId, Pageable pageable);

    Page<PurchaseEntity> findBySellerId(Long sellerId, Pageable pageable);
}
