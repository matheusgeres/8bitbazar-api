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
import static org.junit.jupiter.api.Assertions.assertEquals;
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

    @Test
    void shouldFilterSearchByTypeAndManufacturer() throws Exception {
        String adminToken = IntegrationTestFixtures.loginAsAdmin(mockMvc, jsonMapper);
        Long platformId = IntegrationTestFixtures.createPlatform(mockMvc, jsonMapper, adminToken, "Search Filter Platform");
        Long manufacturerA = IntegrationTestFixtures.createManufacturer(mockMvc, jsonMapper, adminToken, "Search Filter Manufacturer A");
        Long manufacturerB = IntegrationTestFixtures.createManufacturer(mockMvc, jsonMapper, adminToken, "Search Filter Manufacturer B");
        String sellerToken = IntegrationTestFixtures.registerAndLogin(
            mockMvc, jsonMapper, "seller-search-filter@test.com", "Seller@123", "sellersearchfilter", true
        );
        String buyerToken = IntegrationTestFixtures.registerAndLogin(
            mockMvc, jsonMapper, "buyer-search-filter@test.com", "Buyer@123", "buyersearchfilter", false
        );

        String directSaleName = "Search Filter Direct Sale";
        String auctionName = "Search Filter Auction";
        String otherManufacturerName = "Search Filter Other Manufacturer";

        IntegrationTestFixtures.createListing(
            mockMvc,
            jsonMapper,
            sellerToken,
            new CreateListingRequest(
                directSaleName,
                "Filtro por tipo e fabricante",
                platformId,
                manufacturerA,
                "COMPLETE",
                1,
                "DIRECT_SALE",
                new BigDecimal("80.00"),
                null,
                null,
                null,
                null
            )
        );

        IntegrationTestFixtures.createListing(
            mockMvc,
            jsonMapper,
            sellerToken,
            new CreateListingRequest(
                auctionName,
                "Outro tipo de anuncio",
                platformId,
                manufacturerA,
                "LOOSE",
                1,
                "AUCTION",
                null,
                new BigDecimal("40.00"),
                new BigDecimal("100.00"),
                LocalDateTime.now().plusDays(4),
                null
            )
        );

        IntegrationTestFixtures.createListing(
            mockMvc,
            jsonMapper,
            sellerToken,
            new CreateListingRequest(
                otherManufacturerName,
                "Outro fabricante",
                platformId,
                manufacturerB,
                "LOOSE",
                1,
                "DIRECT_SALE",
                new BigDecimal("75.00"),
                null,
                null,
                null,
                null
            )
        );

        assertTrue(waitForSearchPresence(buyerToken, directSaleName, true), "direct sale listing should be indexed");
        assertTrue(waitForSearchPresence(buyerToken, auctionName, true), "auction listing should be indexed");
        assertTrue(waitForSearchPresence(buyerToken, otherManufacturerName, true), "other manufacturer listing should be indexed");

        JsonNode typeFiltered = searchListings(
            buyerToken,
            "Search Filter",
            "DIRECT_SALE",
            null,
            null
        );
        assertTrue(containsListing(typeFiltered, directSaleName));
        assertTrue(containsListing(typeFiltered, otherManufacturerName));
        assertFalse(containsListing(typeFiltered, auctionName));

        JsonNode manufacturerFiltered = searchListings(
            buyerToken,
            "Search Filter",
            null,
            manufacturerA,
            null
        );
        assertTrue(containsListing(manufacturerFiltered, directSaleName));
        assertTrue(containsListing(manufacturerFiltered, auctionName));
        assertFalse(containsListing(manufacturerFiltered, otherManufacturerName));
    }

    @Test
    void shouldSortSearchResultsByPriceAndEndingSoon() throws Exception {
        String adminToken = IntegrationTestFixtures.loginAsAdmin(mockMvc, jsonMapper);
        Long platformId = IntegrationTestFixtures.createPlatform(mockMvc, jsonMapper, adminToken, "Search Sort Platform");
        Long manufacturerId = IntegrationTestFixtures.createManufacturer(mockMvc, jsonMapper, adminToken, "Search Sort Manufacturer");
        String sellerToken = IntegrationTestFixtures.registerAndLogin(
            mockMvc, jsonMapper, "seller-search-sort@test.com", "Seller@123", "sellersearchsort", true
        );
        String buyerToken = IntegrationTestFixtures.registerAndLogin(
            mockMvc, jsonMapper, "buyer-search-sort@test.com", "Buyer@123", "buyersearchsort", false
        );

        String cheapName = "Search Sort Cheap";
        String expensiveName = "Search Sort Expensive";
        String soonerAuction = "Search Sort Sooner";
        String laterAuction = "Search Sort Later";

        IntegrationTestFixtures.createListing(
            mockMvc,
            jsonMapper,
            sellerToken,
            new CreateListingRequest(
                cheapName,
                "Menor preco",
                platformId,
                manufacturerId,
                "LOOSE",
                1,
                "DIRECT_SALE",
                new BigDecimal("50.00"),
                null,
                null,
                null,
                null
            )
        );

        IntegrationTestFixtures.createListing(
            mockMvc,
            jsonMapper,
            sellerToken,
            new CreateListingRequest(
                expensiveName,
                "Maior preco",
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

        IntegrationTestFixtures.createListing(
            mockMvc,
            jsonMapper,
            sellerToken,
            new CreateListingRequest(
                soonerAuction,
                "Leilao termina primeiro",
                platformId,
                manufacturerId,
                "LOOSE",
                1,
                "AUCTION",
                null,
                new BigDecimal("30.00"),
                new BigDecimal("90.00"),
                LocalDateTime.now().plusDays(1),
                null
            )
        );

        IntegrationTestFixtures.createListing(
            mockMvc,
            jsonMapper,
            sellerToken,
            new CreateListingRequest(
                laterAuction,
                "Leilao termina depois",
                platformId,
                manufacturerId,
                "COMPLETE",
                1,
                "AUCTION",
                null,
                new BigDecimal("35.00"),
                new BigDecimal("95.00"),
                LocalDateTime.now().plusDays(3),
                null
            )
        );

        assertTrue(waitForSearchPresence(buyerToken, cheapName, true), "cheap listing should be indexed");
        assertTrue(waitForSearchPresence(buyerToken, expensiveName, true), "expensive listing should be indexed");
        assertTrue(waitForSearchPresence(buyerToken, soonerAuction, true), "soon auction should be indexed");
        assertTrue(waitForSearchPresence(buyerToken, laterAuction, true), "later auction should be indexed");

        JsonNode priceAsc = searchListings(buyerToken, "Search Sort", "DIRECT_SALE", null, "price_asc");
        assertEquals(cheapName, priceAsc.get(0).get("name").asText());
        assertEquals(expensiveName, priceAsc.get(priceAsc.size() - 1).get("name").asText());

        JsonNode priceDesc = searchListings(buyerToken, "Search Sort", "DIRECT_SALE", null, "price_desc");
        assertEquals(expensiveName, priceDesc.get(0).get("name").asText());
        assertEquals(cheapName, priceDesc.get(priceDesc.size() - 1).get("name").asText());

        JsonNode endingSoon = searchListings(buyerToken, "Search Sort", "AUCTION", null, "ending_soon");
        assertEquals(soonerAuction, endingSoon.get(0).get("name").asText());
        assertEquals(laterAuction, endingSoon.get(1).get("name").asText());
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
        JsonNode content = searchListings(token, query, null, null, null);
        for (JsonNode item : content) {
            if (query.equals(item.get("name").asText())) {
                return true;
            }
        }
        return false;
    }

    private JsonNode searchListings(
        String token,
        String query,
        String type,
        Long manufacturerId,
        String sort
    ) throws Exception {
        var requestBuilder = get("/api/v1/listings")
            .header("Authorization", "Bearer " + token);

        if (query != null) {
            requestBuilder = requestBuilder.param("search", query);
        }
        if (type != null) {
            requestBuilder = requestBuilder.param("type", type);
        }
        if (manufacturerId != null) {
            requestBuilder = requestBuilder.param("manufacturerId", manufacturerId.toString());
        }
        if (sort != null) {
            requestBuilder = requestBuilder.param("sort", sort);
        }

        MvcResult result = mockMvc.perform(requestBuilder)
            .andExpect(status().isOk())
            .andReturn();

        return jsonMapper.readTree(result.getResponse().getContentAsString()).get("content");
    }

    private boolean containsListing(JsonNode content, String listingName) {
        for (JsonNode item : content) {
            if (listingName.equals(item.get("name").asText())) {
                return true;
            }
        }
        return false;
    }
}
