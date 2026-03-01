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
}
