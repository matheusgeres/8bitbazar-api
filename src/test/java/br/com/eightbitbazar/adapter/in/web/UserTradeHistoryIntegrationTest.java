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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DisplayName("Historico comercial do usuario")
class UserTradeHistoryIntegrationTest extends IntegrationTestBase {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JsonMapper jsonMapper;

    @Test
    void shouldListAuthenticatedUserPurchases() throws Exception {
        String adminToken = IntegrationTestFixtures.loginAsAdmin(mockMvc, jsonMapper);
        Long platformId = IntegrationTestFixtures.createPlatform(mockMvc, jsonMapper, adminToken, "Trade History Platform");
        Long manufacturerId = IntegrationTestFixtures.createManufacturer(mockMvc, jsonMapper, adminToken, "Trade History Manufacturer");
        String sellerToken = IntegrationTestFixtures.registerAndLogin(
            mockMvc, jsonMapper, "seller-history@test.com", "Seller@123", "sellerhistory", true
        );
        String buyerToken = IntegrationTestFixtures.registerAndLogin(
            mockMvc, jsonMapper, "buyer-history@test.com", "Buyer@123", "buyerhistory", false
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
            .andExpect(status().isCreated());

        mockMvc.perform(get("/api/v1/users/me/purchases")
                .header("Authorization", "Bearer " + buyerToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content[0].purchaseId").exists())
            .andExpect(jsonPath("$.content[0].listingId").value(listingId))
            .andExpect(jsonPath("$.content[0].listingTitle").value("Shadow of the Colossus"))
            .andExpect(jsonPath("$.content[0].listingType").value("DIRECT_SALE"))
            .andExpect(jsonPath("$.content[0].listingStatus").value("SOLD"))
            .andExpect(jsonPath("$.content[0].amount").value(100.00))
            .andExpect(jsonPath("$.content[0].finalAmount").value(95.00))
            .andExpect(jsonPath("$.content[0].paymentMethod").value("PIX"))
            .andExpect(jsonPath("$.content[0].purchaseStatus").value("PENDING"));
    }

    @Test
    void shouldListAuthenticatedUserSales() throws Exception {
        String adminToken = IntegrationTestFixtures.loginAsAdmin(mockMvc, jsonMapper);
        Long platformId = IntegrationTestFixtures.createPlatform(mockMvc, jsonMapper, adminToken, "Sales History Platform");
        Long manufacturerId = IntegrationTestFixtures.createManufacturer(mockMvc, jsonMapper, adminToken, "Sales History Manufacturer");
        String sellerToken = IntegrationTestFixtures.registerAndLogin(
            mockMvc, jsonMapper, "seller-sales-history@test.com", "Seller@123", "sellersaleshistory", true
        );
        String buyerToken = IntegrationTestFixtures.registerAndLogin(
            mockMvc, jsonMapper, "buyer-sales-history@test.com", "Buyer@123", "buyersaleshistory", false
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
                "DIRECT_SALE",
                new BigDecimal("150.00"),
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
            .andExpect(status().isCreated());

        mockMvc.perform(get("/api/v1/users/me/sales")
                .header("Authorization", "Bearer " + sellerToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content[0].purchaseId").exists())
            .andExpect(jsonPath("$.content[0].listingId").value(listingId))
            .andExpect(jsonPath("$.content[0].listingTitle").value("Resident Evil 2"))
            .andExpect(jsonPath("$.content[0].listingType").value("DIRECT_SALE"))
            .andExpect(jsonPath("$.content[0].listingStatus").value("SOLD"))
            .andExpect(jsonPath("$.content[0].amount").value(150.00))
            .andExpect(jsonPath("$.content[0].finalAmount").value(150.00))
            .andExpect(jsonPath("$.content[0].paymentMethod").value("CREDIT_CARD"))
            .andExpect(jsonPath("$.content[0].purchaseStatus").value("PENDING"));
    }

    @Test
    void shouldRejectInvalidPurchasesPagination() throws Exception {
        String adminToken = IntegrationTestFixtures.loginAsAdmin(mockMvc, jsonMapper);

        mockMvc.perform(get("/api/v1/users/me/purchases")
                .header("Authorization", "Bearer " + adminToken)
                .param("page", "-1"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value("Page must be greater than or equal to 0"));

        mockMvc.perform(get("/api/v1/users/me/purchases")
                .header("Authorization", "Bearer " + adminToken)
                .param("size", "101"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value("Size must be between 1 and 100"));
    }

    @Test
    void shouldRejectInvalidSalesPagination() throws Exception {
        String adminToken = IntegrationTestFixtures.loginAsAdmin(mockMvc, jsonMapper);

        mockMvc.perform(get("/api/v1/users/me/sales")
                .header("Authorization", "Bearer " + adminToken)
                .param("page", "-1"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value("Page must be greater than or equal to 0"));

        mockMvc.perform(get("/api/v1/users/me/sales")
                .header("Authorization", "Bearer " + adminToken)
                .param("size", "0"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value("Size must be between 1 and 100"));
    }

    @Test
    void shouldListPurchasesNewestFirst() throws Exception {
        String suffix = Long.toString(System.nanoTime());
        String adminToken = IntegrationTestFixtures.loginAsAdmin(mockMvc, jsonMapper);
        Long platformId = IntegrationTestFixtures.createPlatform(mockMvc, jsonMapper, adminToken, "Newest First Platform " + suffix);
        Long manufacturerId = IntegrationTestFixtures.createManufacturer(mockMvc, jsonMapper, adminToken, "Newest First Manufacturer " + suffix);
        String sellerToken = IntegrationTestFixtures.registerAndLogin(
            mockMvc, jsonMapper, "seller-newest-" + suffix + "@test.com", "Seller@123", "sellernewest" + suffix, true
        );
        String buyerToken = IntegrationTestFixtures.registerAndLogin(
            mockMvc, jsonMapper, "buyer-newest-" + suffix + "@test.com", "Buyer@123", "buyernewest" + suffix, false
        );

        Long firstListingId = IntegrationTestFixtures.createListing(
            mockMvc,
            jsonMapper,
            sellerToken,
            new CreateListingRequest(
                "First Chrono Trigger",
                "First purchase",
                platformId,
                manufacturerId,
                "COMPLETE",
                1,
                "DIRECT_SALE",
                new BigDecimal("100.00"),
                null,
                null,
                null,
                null
            )
        );
        Long secondListingId = IntegrationTestFixtures.createListing(
            mockMvc,
            jsonMapper,
            sellerToken,
            new CreateListingRequest(
                "Second Chrono Trigger",
                "Second purchase",
                platformId,
                manufacturerId,
                "COMPLETE",
                1,
                "DIRECT_SALE",
                new BigDecimal("120.00"),
                null,
                null,
                null,
                null
            )
        );

        mockMvc.perform(post("/api/v1/listings/" + firstListingId + "/purchase")
                .header("Authorization", "Bearer " + buyerToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"paymentMethod\":\"PIX\"}"))
            .andExpect(status().isCreated());

        mockMvc.perform(post("/api/v1/listings/" + secondListingId + "/purchase")
                .header("Authorization", "Bearer " + buyerToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"paymentMethod\":\"PIX\"}"))
            .andExpect(status().isCreated());

        mockMvc.perform(get("/api/v1/users/me/purchases")
                .header("Authorization", "Bearer " + buyerToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content[0].listingId").value(secondListingId))
            .andExpect(jsonPath("$.content[1].listingId").value(firstListingId));
    }
}
