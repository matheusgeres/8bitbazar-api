package br.com.eightbitbazar.adapter.in.web;

import br.com.eightbitbazar.IntegrationTestBase;
import br.com.eightbitbazar.adapter.in.web.dto.CreateListingRequest;
import br.com.eightbitbazar.support.IntegrationTestFixtures;
import tools.jackson.databind.json.JsonMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DisplayName("Regras de lances e leilao")
class BidIntegrationTest extends IntegrationTestBase {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JsonMapper jsonMapper;

    @Test
    void shouldRejectBidForDirectSaleListing() throws Exception {
        String adminToken = IntegrationTestFixtures.loginAsAdmin(mockMvc, jsonMapper);
        Long platformId = IntegrationTestFixtures.createPlatform(mockMvc, jsonMapper, adminToken, "Bid Direct Platform");
        Long manufacturerId = IntegrationTestFixtures.createManufacturer(mockMvc, jsonMapper, adminToken, "Bid Direct Manufacturer");
        String sellerToken = IntegrationTestFixtures.registerAndLogin(
            mockMvc, jsonMapper, "seller-direct-bid@test.com", "Seller@123", "sellerdirectbid", true
        );
        String buyerToken = IntegrationTestFixtures.registerAndLogin(
            mockMvc, jsonMapper, "buyer-direct-bid@test.com", "Buyer@123", "buyerdirectbid", false
        );

        Long listingId = IntegrationTestFixtures.createListing(
            mockMvc,
            jsonMapper,
            sellerToken,
            new CreateListingRequest(
                "Chrono Trigger",
                "Versao NTSC",
                platformId,
                manufacturerId,
                "LOOSE",
                1,
                "DIRECT_SALE",
                new BigDecimal("300.00"),
                null,
                null,
                null,
                null
            )
        );

        mockMvc.perform(post("/api/v1/listings/" + listingId + "/bids")
                .header("Authorization", "Bearer " + buyerToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"amount\":301.00}"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value("This listing is not an auction"));
    }

    @Test
    void shouldRejectSellerOwnBid() throws Exception {
        String adminToken = IntegrationTestFixtures.loginAsAdmin(mockMvc, jsonMapper);
        Long platformId = IntegrationTestFixtures.createPlatform(mockMvc, jsonMapper, adminToken, "Bid Own Platform");
        Long manufacturerId = IntegrationTestFixtures.createManufacturer(mockMvc, jsonMapper, adminToken, "Bid Own Manufacturer");
        String sellerToken = IntegrationTestFixtures.registerAndLogin(
            mockMvc, jsonMapper, "seller-own-bid@test.com", "Seller@123", "sellerownbid", true
        );

        Long listingId = IntegrationTestFixtures.createListing(
            mockMvc,
            jsonMapper,
            sellerToken,
            new CreateListingRequest(
                "Castlevania Bloodlines",
                "Cartucho original",
                platformId,
                manufacturerId,
                "LOOSE",
                1,
                "AUCTION",
                null,
                new BigDecimal("100.00"),
                new BigDecimal("180.00"),
                LocalDateTime.now().plusDays(3),
                null
            )
        );

        mockMvc.perform(post("/api/v1/listings/" + listingId + "/bids")
                .header("Authorization", "Bearer " + sellerToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"amount\":120.00}"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value("You cannot bid on your own listing"));
    }

    @Test
    void shouldRejectBidBelowMinimumAndAcceptNextValidIncrement() throws Exception {
        String adminToken = IntegrationTestFixtures.loginAsAdmin(mockMvc, jsonMapper);
        Long platformId = IntegrationTestFixtures.createPlatform(mockMvc, jsonMapper, adminToken, "Bid Min Platform");
        Long manufacturerId = IntegrationTestFixtures.createManufacturer(mockMvc, jsonMapper, adminToken, "Bid Min Manufacturer");
        String sellerToken = IntegrationTestFixtures.registerAndLogin(
            mockMvc, jsonMapper, "seller-min-bid@test.com", "Seller@123", "sellerminbid", true
        );
        String firstBuyerToken = IntegrationTestFixtures.registerAndLogin(
            mockMvc, jsonMapper, "buyer-one-bid@test.com", "Buyer@123", "buyeronebid", false
        );
        String secondBuyerToken = IntegrationTestFixtures.registerAndLogin(
            mockMvc, jsonMapper, "buyer-two-bid@test.com", "Buyer@123", "buyertwobid", false
        );

        Long listingId = IntegrationTestFixtures.createListing(
            mockMvc,
            jsonMapper,
            sellerToken,
            new CreateListingRequest(
                "Mega Man X3",
                "Excelente estado",
                platformId,
                manufacturerId,
                "COMPLETE",
                1,
                "AUCTION",
                null,
                new BigDecimal("50.00"),
                new BigDecimal("120.00"),
                LocalDateTime.now().plusDays(5),
                null
            )
        );

        mockMvc.perform(post("/api/v1/listings/" + listingId + "/bids")
                .header("Authorization", "Bearer " + firstBuyerToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"amount\":60.00}"))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.amount").value(60.00))
            .andExpect(jsonPath("$.convertedToPurchase").value(false));

        mockMvc.perform(post("/api/v1/listings/" + listingId + "/bids")
                .header("Authorization", "Bearer " + secondBuyerToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"amount\":60.50}"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value("Bid must be at least 61.00"));

        mockMvc.perform(post("/api/v1/listings/" + listingId + "/bids")
                .header("Authorization", "Bearer " + secondBuyerToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"amount\":61.00}"))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.amount").value(61.00))
            .andExpect(jsonPath("$.convertedToPurchase").value(false));

        mockMvc.perform(get("/api/v1/listings/" + listingId)
                .header("Authorization", "Bearer " + secondBuyerToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.recentBids[0].amount").value(61.00));
    }

    @Test
    void shouldConvertBidToPurchaseWhenReachingBuyNowPrice() throws Exception {
        String adminToken = IntegrationTestFixtures.loginAsAdmin(mockMvc, jsonMapper);
        Long platformId = IntegrationTestFixtures.createPlatform(mockMvc, jsonMapper, adminToken, "Bid BuyNow Platform");
        Long manufacturerId = IntegrationTestFixtures.createManufacturer(mockMvc, jsonMapper, adminToken, "Bid BuyNow Manufacturer");
        String sellerToken = IntegrationTestFixtures.registerAndLogin(
            mockMvc, jsonMapper, "seller-buynow-bid@test.com", "Seller@123", "sellerbuynowbid", true
        );
        String buyerToken = IntegrationTestFixtures.registerAndLogin(
            mockMvc, jsonMapper, "buyer-buynow-bid@test.com", "Buyer@123", "buyerbuynowbid", false
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
                LocalDateTime.now().plusDays(2),
                null
            )
        );

        mockMvc.perform(post("/api/v1/listings/" + listingId + "/bids")
                .header("Authorization", "Bearer " + buyerToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"amount\":150.00}"))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.amount").value(150.00))
            .andExpect(jsonPath("$.convertedToPurchase").value(true));

        mockMvc.perform(get("/api/v1/listings/" + listingId)
                .header("Authorization", "Bearer " + buyerToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("SOLD"))
            .andExpect(jsonPath("$.quantity").value(0));
    }
}
