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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DisplayName("Gestao de anuncios")
class ListingManagementIntegrationTest extends IntegrationTestBase {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JsonMapper jsonMapper;

    @Test
    void shouldRejectListingCreationForNonSeller() throws Exception {
        String adminToken = IntegrationTestFixtures.loginAsAdmin(mockMvc, jsonMapper);
        Long platformId = IntegrationTestFixtures.createPlatform(mockMvc, jsonMapper, adminToken, "Mgmt NonSeller Platform");
        Long manufacturerId = IntegrationTestFixtures.createManufacturer(mockMvc, jsonMapper, adminToken, "Mgmt NonSeller Manufacturer");
        String buyerToken = IntegrationTestFixtures.registerAndLogin(
            mockMvc, jsonMapper, "buyer-listing@test.com", "Buyer@123", "buyerlisting", false
        );

        CreateListingRequest request = new CreateListingRequest(
            "Contra III",
            "Nao deveria criar",
            platformId,
            manufacturerId,
            "LOOSE",
            1,
            "DIRECT_SALE",
            new BigDecimal("95.00"),
            null,
            null,
            null,
            null
        );

        mockMvc.perform(post("/api/v1/listings")
                .header("Authorization", "Bearer " + buyerToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value("User is not registered as a seller"));
    }

    @Test
    void shouldRejectDirectSaleWithoutPrice() throws Exception {
        String adminToken = IntegrationTestFixtures.loginAsAdmin(mockMvc, jsonMapper);
        Long platformId = IntegrationTestFixtures.createPlatform(mockMvc, jsonMapper, adminToken, "Mgmt Direct Platform");
        Long manufacturerId = IntegrationTestFixtures.createManufacturer(mockMvc, jsonMapper, adminToken, "Mgmt Direct Manufacturer");
        String sellerToken = IntegrationTestFixtures.registerAndLogin(
            mockMvc, jsonMapper, "seller-direct-mgmt@test.com", "Seller@123", "sellerdirectmgmt", true
        );

        CreateListingRequest request = new CreateListingRequest(
            "Final Fight 3",
            "Sem preco",
            platformId,
            manufacturerId,
            "COMPLETE",
            1,
            "DIRECT_SALE",
            null,
            null,
            null,
            null,
            null
        );

        mockMvc.perform(post("/api/v1/listings")
                .header("Authorization", "Bearer " + sellerToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value("Price is required for direct sale"));
    }

    @Test
    void shouldRejectAuctionWithoutStartingPrice() throws Exception {
        String adminToken = IntegrationTestFixtures.loginAsAdmin(mockMvc, jsonMapper);
        Long platformId = IntegrationTestFixtures.createPlatform(mockMvc, jsonMapper, adminToken, "Mgmt Auction Price Platform");
        Long manufacturerId = IntegrationTestFixtures.createManufacturer(mockMvc, jsonMapper, adminToken, "Mgmt Auction Price Manufacturer");
        String sellerToken = IntegrationTestFixtures.registerAndLogin(
            mockMvc, jsonMapper, "seller-auction-price@test.com", "Seller@123", "sellerauctionprice", true
        );

        CreateListingRequest request = new CreateListingRequest(
            "Super Metroid",
            "Sem lance inicial",
            platformId,
            manufacturerId,
            "LOOSE",
            1,
            "AUCTION",
            null,
            null,
            new BigDecimal("200.00"),
            LocalDateTime.now().plusDays(2),
            null
        );

        mockMvc.perform(post("/api/v1/listings")
                .header("Authorization", "Bearer " + sellerToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value("Starting price is required for auction"));
    }

    @Test
    void shouldRejectAuctionWithoutEndDate() throws Exception {
        String adminToken = IntegrationTestFixtures.loginAsAdmin(mockMvc, jsonMapper);
        Long platformId = IntegrationTestFixtures.createPlatform(mockMvc, jsonMapper, adminToken, "Mgmt Auction Date Platform");
        Long manufacturerId = IntegrationTestFixtures.createManufacturer(mockMvc, jsonMapper, adminToken, "Mgmt Auction Date Manufacturer");
        String sellerToken = IntegrationTestFixtures.registerAndLogin(
            mockMvc, jsonMapper, "seller-auction-date@test.com", "Seller@123", "sellerauctiondate", true
        );

        CreateListingRequest request = new CreateListingRequest(
            "EarthBound",
            "Sem data final",
            platformId,
            manufacturerId,
            "COMPLETE",
            1,
            "AUCTION",
            null,
            new BigDecimal("150.00"),
            new BigDecimal("250.00"),
            null,
            null
        );

        mockMvc.perform(post("/api/v1/listings")
                .header("Authorization", "Bearer " + sellerToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value("Auction end date is required"));
    }

    @Test
    void shouldRejectAuctionWithPastEndDate() throws Exception {
        String adminToken = IntegrationTestFixtures.loginAsAdmin(mockMvc, jsonMapper);
        Long platformId = IntegrationTestFixtures.createPlatform(mockMvc, jsonMapper, adminToken, "Mgmt Auction Past Platform");
        Long manufacturerId = IntegrationTestFixtures.createManufacturer(mockMvc, jsonMapper, adminToken, "Mgmt Auction Past Manufacturer");
        String sellerToken = IntegrationTestFixtures.registerAndLogin(
            mockMvc, jsonMapper, "seller-auction-past@test.com", "Seller@123", "sellerauctionpast", true
        );

        CreateListingRequest request = new CreateListingRequest(
            "Secret of Mana",
            "Data passada",
            platformId,
            manufacturerId,
            "LOOSE",
            1,
            "AUCTION",
            null,
            new BigDecimal("70.00"),
            new BigDecimal("150.00"),
            LocalDateTime.now().minusDays(1),
            null
        );

        mockMvc.perform(post("/api/v1/listings")
                .header("Authorization", "Bearer " + sellerToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value("Auction end date must be in the future"));
    }

    @Test
    void shouldRejectDeletingAnotherUsersListing() throws Exception {
        String adminToken = IntegrationTestFixtures.loginAsAdmin(mockMvc, jsonMapper);
        Long platformId = IntegrationTestFixtures.createPlatform(mockMvc, jsonMapper, adminToken, "Mgmt Delete Platform");
        Long manufacturerId = IntegrationTestFixtures.createManufacturer(mockMvc, jsonMapper, adminToken, "Mgmt Delete Manufacturer");
        String sellerToken = IntegrationTestFixtures.registerAndLogin(
            mockMvc, jsonMapper, "seller-delete-mgmt@test.com", "Seller@123", "sellerdeletemgmt", true
        );
        String otherSellerToken = IntegrationTestFixtures.registerAndLogin(
            mockMvc, jsonMapper, "seller-delete-other@test.com", "Seller@123", "sellerdeleteother", true
        );

        Long listingId = IntegrationTestFixtures.createListing(
            mockMvc,
            jsonMapper,
            sellerToken,
            new CreateListingRequest(
                "Sunset Riders",
                "Nao pode ser apagado por outro usuario",
                platformId,
                manufacturerId,
                "LOOSE",
                1,
                "DIRECT_SALE",
                new BigDecimal("85.00"),
                null,
                null,
                null,
                null
            )
        );

        mockMvc.perform(delete("/api/v1/listings/" + listingId)
                .header("Authorization", "Bearer " + otherSellerToken))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value("You can only delete your own listings"));
    }
}
