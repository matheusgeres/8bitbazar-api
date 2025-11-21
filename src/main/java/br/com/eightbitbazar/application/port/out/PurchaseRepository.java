package br.com.eightbitbazar.application.port.out;

import br.com.eightbitbazar.domain.purchase.Purchase;
import br.com.eightbitbazar.domain.user.UserId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface PurchaseRepository {

    Purchase save(Purchase purchase);

    Page<Purchase> findByBuyerId(UserId buyerId, Pageable pageable);

    Page<Purchase> findBySellerId(UserId sellerId, Pageable pageable);
}
