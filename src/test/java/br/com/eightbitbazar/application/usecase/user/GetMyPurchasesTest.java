package br.com.eightbitbazar.application.usecase.user;

import br.com.eightbitbazar.application.port.out.ListingRepository;
import br.com.eightbitbazar.application.port.out.PurchaseRepository;
import br.com.eightbitbazar.domain.listing.ItemCondition;
import br.com.eightbitbazar.domain.listing.Listing;
import br.com.eightbitbazar.domain.listing.ListingId;
import br.com.eightbitbazar.domain.listing.ListingStatus;
import br.com.eightbitbazar.domain.listing.ListingType;
import br.com.eightbitbazar.domain.purchase.PaymentMethod;
import br.com.eightbitbazar.domain.purchase.Purchase;
import br.com.eightbitbazar.domain.purchase.PurchaseStatus;
import br.com.eightbitbazar.domain.purchase.PurchaseType;
import br.com.eightbitbazar.domain.user.UserId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetMyPurchasesTest {

    @Mock
    private PurchaseRepository purchaseRepository;

    @Mock
    private ListingRepository listingRepository;

    private GetMyPurchases getMyPurchases;

    @BeforeEach
    void setUp() {
        getMyPurchases = new GetMyPurchases(purchaseRepository, listingRepository);
    }

    @Test
    void shouldReturnBuyerHistoryWithListingSummary() {
        UserId buyerId = new UserId(10L);
        ListingId listingId = new ListingId(55L);
        Purchase purchase = new Purchase(
            99L,
            listingId,
            buyerId,
            new UserId(20L),
            new BigDecimal("100.00"),
            PurchaseType.DIRECT,
            PaymentMethod.PIX,
            BigDecimal.ZERO,
            new BigDecimal("95.00"),
            PurchaseStatus.PENDING,
            LocalDateTime.of(2026, 3, 1, 10, 0)
        );
        Listing listing = new Listing(
            listingId,
            new UserId(20L),
            "Shadow of the Colossus",
            "Com manual",
            1L,
            2L,
            ItemCondition.COMPLETE,
            1,
            ListingType.DIRECT_SALE,
            new BigDecimal("100.00"),
            null,
            null,
            null,
            null,
            ListingStatus.SOLD,
            List.of(),
            LocalDateTime.of(2026, 2, 28, 10, 0),
            LocalDateTime.of(2026, 3, 1, 10, 0),
            null
        );

        PageRequest pageRequest = PageRequest.of(0, 20, Sort.by(Sort.Direction.DESC, "createdAt"));
        when(purchaseRepository.findByBuyerId(buyerId, pageRequest))
            .thenReturn(new PageImpl<>(List.of(purchase), pageRequest, 1));
        when(listingRepository.findByIds(Set.of(listingId))).thenReturn(List.of(listing));

        Page<UserTradeHistoryItemOutput> output = getMyPurchases.execute(buyerId, 0, 20);

        assertEquals(1, output.getTotalElements());
        assertEquals(99L, output.getContent().get(0).purchaseId());
        assertEquals(55L, output.getContent().get(0).listingId());
        assertEquals("Shadow of the Colossus", output.getContent().get(0).listingTitle());
        assertEquals("DIRECT_SALE", output.getContent().get(0).listingType());
        assertEquals("SOLD", output.getContent().get(0).listingStatus());
        assertEquals(new BigDecimal("100.00"), output.getContent().get(0).amount());
        assertEquals(new BigDecimal("95.00"), output.getContent().get(0).finalAmount());
        assertEquals("PIX", output.getContent().get(0).paymentMethod());
        assertEquals("PENDING", output.getContent().get(0).purchaseStatus());
        assertEquals(LocalDateTime.of(2026, 3, 1, 10, 0), output.getContent().get(0).createdAt());

        verify(purchaseRepository).findByBuyerId(buyerId, pageRequest);
        verify(listingRepository).findByIds(Set.of(listingId));
    }
}
