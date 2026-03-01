package br.com.eightbitbazar.adapter.in.web;

import br.com.eightbitbazar.IntegrationTestBase;
import br.com.eightbitbazar.adapter.in.web.dto.CreateListingRequest;
import br.com.eightbitbazar.support.IntegrationTestFixtures;
import io.micrometer.core.instrument.MeterRegistry;
import tools.jackson.databind.json.JsonMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DisplayName("Fluxo de eventos de compra")
class PurchaseEventFlowIntegrationTest extends IntegrationTestBase {

    private static final String PURCHASE_EVENTS_CONSUMED_METER = "eightbitbazar.purchase.events.consumed";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JsonMapper jsonMapper;

    @Autowired
    private MeterRegistry meterRegistry;

    @Test
    void directPurchaseShouldPublishConsumablePurchaseCompletedEvent() throws Exception {
        double initialCount = meterRegistry.get(PURCHASE_EVENTS_CONSUMED_METER).counter().count();
        String adminToken = IntegrationTestFixtures.loginAsAdmin(mockMvc, jsonMapper);
        Long platformId = IntegrationTestFixtures.createPlatform(mockMvc, jsonMapper, adminToken, "Purchase Event Platform");
        Long manufacturerId = IntegrationTestFixtures.createManufacturer(mockMvc, jsonMapper, adminToken, "Purchase Event Manufacturer");
        String sellerToken = IntegrationTestFixtures.registerAndLogin(
            mockMvc, jsonMapper, "seller-purchase-event@test.com", "Seller@123", "sellerpurchaseevent", true
        );
        String buyerToken = IntegrationTestFixtures.registerAndLogin(
            mockMvc, jsonMapper, "buyer-purchase-event@test.com", "Buyer@123", "buyerpurchaseevent", false
        );

        Long listingId = IntegrationTestFixtures.createListing(
            mockMvc,
            jsonMapper,
            sellerToken,
            new CreateListingRequest(
                "Metal Gear Solid",
                "Completo",
                platformId,
                manufacturerId,
                "COMPLETE",
                1,
                "DIRECT_SALE",
                new BigDecimal("200.00"),
                null,
                null,
                null,
                null
            )
        );

        mockMvc.perform(post("/api/v1/listings/" + listingId + "/purchase")
                .header("Authorization", "Bearer " + buyerToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"paymentMethod\":\"PIX\"}"))
            .andExpect(status().isCreated());

        assertTrue(awaitCounterIncrement(initialCount, Duration.ofSeconds(10)));
    }

    private boolean awaitCounterIncrement(double initialCount, Duration timeout) throws InterruptedException {
        Instant deadline = Instant.now().plus(timeout);

        while (Instant.now().isBefore(deadline)) {
            double currentCount = meterRegistry.get(PURCHASE_EVENTS_CONSUMED_METER).counter().count();
            if (currentCount > initialCount) {
                return true;
            }
            Thread.sleep(200);
        }

        return meterRegistry.get(PURCHASE_EVENTS_CONSUMED_METER).counter().count() > initialCount;
    }
}
