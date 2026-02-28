package br.com.eightbitbazar.adapter.in.web;

import br.com.eightbitbazar.IntegrationTestBase;
import br.com.eightbitbazar.adapter.in.web.dto.CreateListingRequest;
import br.com.eightbitbazar.support.IntegrationTestFixtures;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.JsonNode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DisplayName("Busca e indexacao de anuncios")
class ListingSearchIntegrationTest extends IntegrationTestBase {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JsonMapper jsonMapper;

    @Test
    void shouldIndexCreatedListingInSearch() throws Exception {
        String adminToken = IntegrationTestFixtures.loginAsAdmin(mockMvc, jsonMapper);
        Long platformId = IntegrationTestFixtures.createPlatform(mockMvc, jsonMapper, adminToken, "Search Create Platform");
        Long manufacturerId = IntegrationTestFixtures.createManufacturer(mockMvc, jsonMapper, adminToken, "Search Create Manufacturer");
        String sellerToken = IntegrationTestFixtures.registerAndLogin(
            mockMvc, jsonMapper, "seller-search-create@test.com", "Seller@123", "sellersearchcreate", true
        );
        String buyerToken = IntegrationTestFixtures.registerAndLogin(
            mockMvc, jsonMapper, "buyer-search-create@test.com", "Buyer@123", "buyersearchcreate", false
        );

        String listingName = "Search Indexed Listing Alpha";
        IntegrationTestFixtures.createListing(
            mockMvc,
            jsonMapper,
            sellerToken,
            new CreateListingRequest(
                listingName,
                "Descricao indexada",
                platformId,
                manufacturerId,
                "COMPLETE",
                1,
                "DIRECT_SALE",
                new BigDecimal("110.00"),
                null,
                null,
                null,
                null
            )
        );

        assertTrue(waitForSearchPresence(buyerToken, listingName, true), "listing should appear in search after creation");
    }

    @Test
    void shouldRemoveDeletedListingFromSearch() throws Exception {
        String adminToken = IntegrationTestFixtures.loginAsAdmin(mockMvc, jsonMapper);
        Long platformId = IntegrationTestFixtures.createPlatform(mockMvc, jsonMapper, adminToken, "Search Delete Platform");
        Long manufacturerId = IntegrationTestFixtures.createManufacturer(mockMvc, jsonMapper, adminToken, "Search Delete Manufacturer");
        String sellerToken = IntegrationTestFixtures.registerAndLogin(
            mockMvc, jsonMapper, "seller-search-delete@test.com", "Seller@123", "sellersearchdelete", true
        );
        String buyerToken = IntegrationTestFixtures.registerAndLogin(
            mockMvc, jsonMapper, "buyer-search-delete@test.com", "Buyer@123", "buyersearchdelete", false
        );

        String listingName = "Search Deleted Listing Beta";
        Long listingId = IntegrationTestFixtures.createListing(
            mockMvc,
            jsonMapper,
            sellerToken,
            new CreateListingRequest(
                listingName,
                "Descricao removida",
                platformId,
                manufacturerId,
                "LOOSE",
                1,
                "DIRECT_SALE",
                new BigDecimal("90.00"),
                null,
                null,
                null,
                null
            )
        );

        assertTrue(waitForSearchPresence(buyerToken, listingName, true), "listing should appear before deletion");

        mockMvc.perform(delete("/api/v1/listings/" + listingId)
                .header("Authorization", "Bearer " + sellerToken))
            .andExpect(status().isNoContent());

        assertTrue(waitForSearchPresence(buyerToken, listingName, false), "listing should disappear after deletion");
    }

    @Test
    void shouldRemoveSoldListingFromSearchAfterPurchase() throws Exception {
        String adminToken = IntegrationTestFixtures.loginAsAdmin(mockMvc, jsonMapper);
        Long platformId = IntegrationTestFixtures.createPlatform(mockMvc, jsonMapper, adminToken, "Search Sold Platform");
        Long manufacturerId = IntegrationTestFixtures.createManufacturer(mockMvc, jsonMapper, adminToken, "Search Sold Manufacturer");
        String sellerToken = IntegrationTestFixtures.registerAndLogin(
            mockMvc, jsonMapper, "seller-search-sold@test.com", "Seller@123", "sellersearchsold", true
        );
        String buyerToken = IntegrationTestFixtures.registerAndLogin(
            mockMvc, jsonMapper, "buyer-search-sold@test.com", "Buyer@123", "buyersearchsold", false
        );

        String listingName = "Search Sold Listing Gamma";
        Long listingId = IntegrationTestFixtures.createListing(
            mockMvc,
            jsonMapper,
            sellerToken,
            new CreateListingRequest(
                listingName,
                "Descricao vendida",
                platformId,
                manufacturerId,
                "COMPLETE",
                1,
                "DIRECT_SALE",
                new BigDecimal("140.00"),
                null,
                null,
                null,
                null
            )
        );

        assertTrue(waitForSearchPresence(buyerToken, listingName, true), "listing should appear before purchase");

        mockMvc.perform(post("/api/v1/listings/" + listingId + "/purchase")
                .header("Authorization", "Bearer " + buyerToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"paymentMethod\":\"CREDIT_CARD\"}"))
            .andExpect(status().isCreated());

        assertTrue(waitForSearchPresence(buyerToken, listingName, false), "listing should disappear after sale");
    }

    private boolean waitForSearchPresence(String token, String query, boolean shouldExist) throws Exception {
        for (int i = 0; i < 20; i++) {
            boolean exists = searchContains(token, query);
            if (exists == shouldExist) {
                return true;
            }
            Thread.sleep(500);
        }
        return false;
    }

    private boolean searchContains(String token, String query) throws Exception {
        MvcResult result = mockMvc.perform(get("/api/v1/listings")
                .header("Authorization", "Bearer " + token)
                .param("search", query))
            .andExpect(status().isOk())
            .andReturn();

        JsonNode content = jsonMapper.readTree(result.getResponse().getContentAsString()).get("content");
        for (JsonNode item : content) {
            if (query.equals(item.get("name").asText())) {
                return true;
            }
        }
        return false;
    }
}
