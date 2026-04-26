package br.com.eightbitbazar.adapter.in.web;

import br.com.eightbitbazar.IntegrationTestBase;
import br.com.eightbitbazar.adapter.in.web.dto.CreateListingRequest;
import br.com.eightbitbazar.adapter.out.persistence.repository.JpaListingRepository;
import br.com.eightbitbazar.adapter.out.persistence.repository.JpaPurchaseRepository;
import br.com.eightbitbazar.adapter.out.persistence.repository.JpaUserRepository;
import br.com.eightbitbazar.application.port.in.CloseExpiredAuctionsUseCase;
import br.com.eightbitbazar.support.IntegrationTestFixtures;
import tools.jackson.databind.json.JsonMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DisplayName("Fechamento de leiloes expirados")
class AuctionClosingIntegrationTest extends IntegrationTestBase {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JsonMapper jsonMapper;

    @Autowired
    private CloseExpiredAuctionsUseCase closeExpiredAuctionsUseCase;

    @Autowired
    private JpaListingRepository jpaListingRepository;

    @Autowired
    private JpaPurchaseRepository jpaPurchaseRepository;

    @Autowired
    private JpaUserRepository jpaUserRepository;

    @Test
    void shouldCloseExpiredAuctionWithWinnerAndCreatePurchase() throws Exception {
        String adminToken = IntegrationTestFixtures.loginAsAdmin(mockMvc, jsonMapper);
        Long platformId = IntegrationTestFixtures.createPlatform(mockMvc, jsonMapper, adminToken, "Auction Closing Platform");
        Long manufacturerId = IntegrationTestFixtures.createManufacturer(mockMvc, jsonMapper, adminToken, "Auction Closing Manufacturer");
        String sellerToken = IntegrationTestFixtures.registerAndLogin(
            mockMvc, jsonMapper, "seller-auction-close@test.com", "Seller@123", "sellerauctionclose", true
        );
        String buyerToken = IntegrationTestFixtures.registerAndLogin(
            mockMvc, jsonMapper, "buyer-auction-close@test.com", "Buyer@123", "buyerauctionclose", false
        );

        Long listingId = IntegrationTestFixtures.createListing(
            mockMvc,
            jsonMapper,
            sellerToken,
            new CreateListingRequest(
                "Resident Evil 2",
                "Midia original",
                platformId,
                manufacturerId,
                "COMPLETE",
                1,
                "AUCTION",
                null,
                new BigDecimal("80.00"),
                new BigDecimal("150.00"),
                LocalDateTime.now().plusHours(2),
                null
            )
        );

        mockMvc.perform(post("/api/v1/listings/" + listingId + "/bids")
                .header("Authorization", "Bearer " + buyerToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"amount\":100.00}"))
            .andExpect(status().isCreated());

        expireListing(listingId);

        int processed = closeExpiredAuctionsUseCase.execute();

        assertEquals(1, processed);

        mockMvc.perform(get("/api/v1/listings/" + listingId)
                .header("Authorization", "Bearer " + buyerToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("SOLD"))
            .andExpect(jsonPath("$.quantity").value(0));

        mockMvc.perform(get("/api/v1/users/me/purchases")
                .header("Authorization", "Bearer " + buyerToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content[0].listingId").value(listingId))
            .andExpect(jsonPath("$.content[0].listingTitle").value("Resident Evil 2"))
            .andExpect(jsonPath("$.content[0].amount").value(100.00))
            .andExpect(jsonPath("$.content[0].paymentMethod").value("OTHER"));

        mockMvc.perform(get("/api/v1/users/me/sales")
                .header("Authorization", "Bearer " + sellerToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content[0].listingId").value(listingId))
            .andExpect(jsonPath("$.content[0].listingTitle").value("Resident Evil 2"));
    }

    @Test
    void shouldExpireAuctionWithoutWinnerOnlyOnce() throws Exception {
        String sellerEmail = "seller-auction-expire@test.com";
        String adminToken = IntegrationTestFixtures.loginAsAdmin(mockMvc, jsonMapper);
        Long platformId = IntegrationTestFixtures.createPlatform(mockMvc, jsonMapper, adminToken, "Auction Expire Platform");
        Long manufacturerId = IntegrationTestFixtures.createManufacturer(mockMvc, jsonMapper, adminToken, "Auction Expire Manufacturer");
        String sellerToken = IntegrationTestFixtures.registerAndLogin(
            mockMvc, jsonMapper, sellerEmail, "Seller@123", "sellerauctionexpire", true
        );
        Long sellerId = jpaUserRepository.findByEmail(sellerEmail).orElseThrow().getId();

        Long listingId = IntegrationTestFixtures.createListing(
            mockMvc,
            jsonMapper,
            sellerToken,
            new CreateListingRequest(
                "Chrono Cross",
                "Completo",
                platformId,
                manufacturerId,
                "COMPLETE",
                1,
                "AUCTION",
                null,
                new BigDecimal("50.00"),
                new BigDecimal("120.00"),
                LocalDateTime.now().plusHours(1),
                null
            )
        );

        expireListing(listingId);

        int firstRun = closeExpiredAuctionsUseCase.execute();
        int secondRun = closeExpiredAuctionsUseCase.execute();

        assertEquals(1, firstRun);
        assertEquals(0, secondRun);
        long purchasesForListing = jpaPurchaseRepository.findBySellerId(sellerId, Pageable.unpaged()).stream()
            .filter(purchase -> purchase.getListingId().equals(listingId))
            .count();
        assertEquals(0, purchasesForListing);

        mockMvc.perform(get("/api/v1/listings/" + listingId)
                .header("Authorization", "Bearer " + sellerToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("EXPIRED"));
    }

    @Test
    void shouldCloseExpiredAuctionWithWinnerOnlyOnce() throws Exception {
        String suffix = String.valueOf(System.nanoTime());
        String buyerEmail = "buyer-auction-idempotent-" + suffix + "@test.com";
        String adminToken = IntegrationTestFixtures.loginAsAdmin(mockMvc, jsonMapper);
        Long platformId = IntegrationTestFixtures.createPlatform(
            mockMvc, jsonMapper, adminToken, "Auction Idempotent Platform " + suffix
        );
        Long manufacturerId = IntegrationTestFixtures.createManufacturer(
            mockMvc, jsonMapper, adminToken, "Auction Idempotent Manufacturer " + suffix
        );
        String sellerToken = IntegrationTestFixtures.registerAndLogin(
            mockMvc,
            jsonMapper,
            "seller-auction-idempotent-" + suffix + "@test.com",
            "Seller@123",
            "sellerauctionidempotent" + suffix,
            true
        );
        String buyerToken = IntegrationTestFixtures.registerAndLogin(
            mockMvc,
            jsonMapper,
            buyerEmail,
            "Buyer@123",
            "buyerauctionidempotent" + suffix,
            false
        );
        Long buyerId = jpaUserRepository.findByEmail(buyerEmail).orElseThrow().getId();

        Long listingId = IntegrationTestFixtures.createListing(
            mockMvc,
            jsonMapper,
            sellerToken,
            new CreateListingRequest(
                "Silent Hill",
                "Completo",
                platformId,
                manufacturerId,
                "COMPLETE",
                1,
                "AUCTION",
                null,
                new BigDecimal("90.00"),
                new BigDecimal("180.00"),
                LocalDateTime.now().plusHours(1),
                null
            )
        );

        mockMvc.perform(post("/api/v1/listings/" + listingId + "/bids")
                .header("Authorization", "Bearer " + buyerToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"amount\":120.00}"))
            .andExpect(status().isCreated());

        expireListing(listingId);

        int firstRun = closeExpiredAuctionsUseCase.execute();
        int secondRun = closeExpiredAuctionsUseCase.execute();

        assertEquals(1, firstRun);
        assertEquals(0, secondRun);
        long purchasesForListing = jpaPurchaseRepository.findByBuyerId(buyerId, Pageable.unpaged()).stream()
            .filter(purchase -> purchase.getListingId().equals(listingId))
            .count();
        assertEquals(1, purchasesForListing);

        mockMvc.perform(get("/api/v1/listings/" + listingId)
                .header("Authorization", "Bearer " + buyerToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("SOLD"))
            .andExpect(jsonPath("$.quantity").value(0));
    }

    private void expireListing(Long listingId) {
        jpaListingRepository.findById(listingId).ifPresent(entity -> {
            entity.setAuctionEndDate(LocalDateTime.now().minusMinutes(5));
            jpaListingRepository.save(entity);
        });
    }
}
