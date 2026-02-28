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
import org.springframework.test.web.servlet.MvcResult;

import java.math.BigDecimal;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DisplayName("Regras de compra direta")
class PurchaseIntegrationTest extends IntegrationTestBase {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JsonMapper jsonMapper;

    @Test
    void shouldRejectBuyingOwnListing() throws Exception {
        String adminToken = IntegrationTestFixtures.loginAsAdmin(mockMvc, jsonMapper);
        Long platformId = IntegrationTestFixtures.createPlatform(mockMvc, jsonMapper, adminToken, "Nintendo 64 Purchase");
        Long manufacturerId = IntegrationTestFixtures.createManufacturer(mockMvc, jsonMapper, adminToken, "Nintendo Purchase");
        String sellerToken = IntegrationTestFixtures.registerAndLogin(
            mockMvc, jsonMapper, "seller-purchase@test.com", "Seller@123", "sellerpurchase", true
        );

        Long listingId = IntegrationTestFixtures.createListing(
            mockMvc,
            jsonMapper,
            sellerToken,
            new CreateListingRequest(
                "Mario Kart 64",
                "Original e funcionando",
                platformId,
                manufacturerId,
                "LOOSE",
                1,
                "DIRECT_SALE",
                new BigDecimal("120.00"),
                null,
                null,
                null,
                null
            )
        );

        mockMvc.perform(post("/api/v1/listings/" + listingId + "/purchase")
                .header("Authorization", "Bearer " + sellerToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"paymentMethod\":\"PIX\"}"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value("You cannot buy your own listing"));
    }

    @Test
    void shouldRejectPurchasingShowcaseListing() throws Exception {
        String adminToken = IntegrationTestFixtures.loginAsAdmin(mockMvc, jsonMapper);
        Long platformId = IntegrationTestFixtures.createPlatform(mockMvc, jsonMapper, adminToken, "Sega Saturn Showcase");
        Long manufacturerId = IntegrationTestFixtures.createManufacturer(mockMvc, jsonMapper, adminToken, "Sega Showcase");
        String sellerToken = IntegrationTestFixtures.registerAndLogin(
            mockMvc, jsonMapper, "seller-showcase@test.com", "Seller@123", "sellershowcase", true
        );
        String buyerToken = IntegrationTestFixtures.registerAndLogin(
            mockMvc, jsonMapper, "buyer-showcase@test.com", "Buyer@123", "buyershowcase", false
        );

        Long listingId = IntegrationTestFixtures.createListing(
            mockMvc,
            jsonMapper,
            sellerToken,
            new CreateListingRequest(
                "Panzer Dragoon Saga",
                "Item de colecao",
                platformId,
                manufacturerId,
                "COMPLETE",
                1,
                "SHOWCASE",
                null,
                null,
                null,
                null,
                null
            )
        );

        mockMvc.perform(post("/api/v1/listings/" + listingId + "/purchase")
                .header("Authorization", "Bearer " + buyerToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"paymentMethod\":\"CREDIT_CARD\"}"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value("This listing is showcase only, not for sale"));
    }

    @Test
    void shouldApplyPixDiscountAndMarkListingAsSold() throws Exception {
        String adminToken = IntegrationTestFixtures.loginAsAdmin(mockMvc, jsonMapper);
        Long platformId = IntegrationTestFixtures.createPlatform(mockMvc, jsonMapper, adminToken, "PlayStation 2 Discount");
        Long manufacturerId = IntegrationTestFixtures.createManufacturer(mockMvc, jsonMapper, adminToken, "Sony Discount");
        String sellerToken = IntegrationTestFixtures.registerAndLogin(
            mockMvc, jsonMapper, "seller-discount@test.com", "Seller@123", "sellerdiscount", true
        );
        String buyerToken = IntegrationTestFixtures.registerAndLogin(
            mockMvc, jsonMapper, "buyer-discount@test.com", "Buyer@123", "buyerdiscount", false
        );

        Long listingId = IntegrationTestFixtures.createListing(
            mockMvc,
            jsonMapper,
            sellerToken,
            new CreateListingRequest(
                "Shadow of the Colossus",
                "Com manual",
                platformId,
                manufacturerId,
                "COMPLETE",
                1,
                "DIRECT_SALE",
                new BigDecimal("100.00"),
                null,
                null,
                null,
                new BigDecimal("5.00")
            )
        );

        mockMvc.perform(post("/api/v1/listings/" + listingId + "/purchase")
                .header("Authorization", "Bearer " + buyerToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"paymentMethod\":\"PIX\"}"))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.amount").value(100.00))
            .andExpect(jsonPath("$.discountApplied").value(5.00))
            .andExpect(jsonPath("$.finalAmount").value(95.00))
            .andExpect(jsonPath("$.paymentMethod").value("PIX"));

        mockMvc.perform(get("/api/v1/listings/" + listingId)
                .header("Authorization", "Bearer " + buyerToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("SOLD"))
            .andExpect(jsonPath("$.quantity").value(0));
    }
}
