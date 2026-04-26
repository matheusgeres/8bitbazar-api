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
class GetMySalesTest {

    @Mock
    private PurchaseRepository purchaseRepository;

    @Mock
    private ListingRepository listingRepository;

    private GetMySales getMySales;

    @BeforeEach
    void setUp() {
        getMySales = new GetMySales(purchaseRepository, listingRepository);
    }

    @Test
    void shouldReturnSellerHistoryWithListingSummary() {
        UserId sellerId = new UserId(20L);
        ListingId listingId = new ListingId(77L);
        Purchase purchase = new Purchase(
            150L,
            listingId,
            new UserId(10L),
            sellerId,
            new BigDecimal("150.00"),
            PurchaseType.AUCTION_WIN,
            PaymentMethod.OTHER,
            BigDecimal.ZERO,
            new BigDecimal("150.00"),
            PurchaseStatus.PENDING,
            LocalDateTime.of(2026, 3, 1, 11, 30)
        );
        Listing listing = new Listing(
            listingId,
            sellerId,
            "Resident Evil 2",
            "Midia original",
            1L,
            2L,
            ItemCondition.COMPLETE,
            0,
            ListingType.AUCTION,
            null,
            new BigDecimal("80.00"),
            new BigDecimal("150.00"),
            LocalDateTime.of(2026, 3, 1, 9, 0),
            null,
            ListingStatus.SOLD,
            List.of(),
            LocalDateTime.of(2026, 2, 27, 12, 0),
            LocalDateTime.of(2026, 3, 1, 11, 30),
            null
        );

        PageRequest pageRequest = PageRequest.of(0, 20, Sort.by(Sort.Direction.DESC, "createdAt"));
        when(purchaseRepository.findBySellerId(sellerId, pageRequest))
            .thenReturn(new PageImpl<>(List.of(purchase), pageRequest, 1));
        when(listingRepository.findByIds(Set.of(listingId))).thenReturn(List.of(listing));

        Page<UserTradeHistoryItemOutput> output = getMySales.execute(sellerId, 0, 20);

        assertEquals(1, output.getTotalElements());
        assertEquals(150L, output.getContent().get(0).purchaseId());
        assertEquals(77L, output.getContent().get(0).listingId());
        assertEquals("Resident Evil 2", output.getContent().get(0).listingTitle());
        assertEquals("AUCTION", output.getContent().get(0).listingType());
        assertEquals("SOLD", output.getContent().get(0).listingStatus());
        assertEquals(new BigDecimal("150.00"), output.getContent().get(0).amount());
        assertEquals(new BigDecimal("150.00"), output.getContent().get(0).finalAmount());
        assertEquals("OTHER", output.getContent().get(0).paymentMethod());
        assertEquals("PENDING", output.getContent().get(0).purchaseStatus());
        assertEquals(LocalDateTime.of(2026, 3, 1, 11, 30), output.getContent().get(0).createdAt());

        verify(purchaseRepository).findBySellerId(sellerId, pageRequest);
        verify(listingRepository).findByIds(Set.of(listingId));
    }
}
