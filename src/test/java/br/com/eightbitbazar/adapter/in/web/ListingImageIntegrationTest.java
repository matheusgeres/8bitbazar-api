package br.com.eightbitbazar.adapter.in.web;

import br.com.eightbitbazar.IntegrationTestBase;
import br.com.eightbitbazar.adapter.in.web.dto.CreateListingRequest;
import br.com.eightbitbazar.support.IntegrationTestFixtures;
import tools.jackson.databind.json.JsonMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DisplayName("Upload de imagens do anuncio")
class ListingImageIntegrationTest extends IntegrationTestBase {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JsonMapper jsonMapper;

    @Test
    void shouldUploadImageForListingOwner() throws Exception {
        String adminToken = IntegrationTestFixtures.loginAsAdmin(mockMvc, jsonMapper);
        Long platformId = IntegrationTestFixtures.createPlatform(mockMvc, jsonMapper, adminToken, "Image Upload Platform");
        Long manufacturerId = IntegrationTestFixtures.createManufacturer(mockMvc, jsonMapper, adminToken, "Image Upload Manufacturer");
        String sellerToken = IntegrationTestFixtures.registerAndLogin(
            mockMvc, jsonMapper, "seller-image@test.com", "Seller@123", "sellerimage", true
        );

        Long listingId = IntegrationTestFixtures.createListing(
            mockMvc,
            jsonMapper,
            sellerToken,
            new CreateListingRequest(
                "Illusion of Gaia",
                "Anuncio com imagem",
                platformId,
                manufacturerId,
                "COMPLETE",
                1,
                "DIRECT_SALE",
                new BigDecimal("130.00"),
                null,
                null,
                null,
                null
            )
        );

        MockMultipartFile file = new MockMultipartFile(
            "files",
            "cover.jpg",
            MediaType.IMAGE_JPEG_VALUE,
            "fake-jpg-content".getBytes()
        );

        mockMvc.perform(multipart("/api/v1/listings/" + listingId + "/images")
                .file(file)
                .header("Authorization", "Bearer " + sellerToken))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$[0]", containsString("/test-bucket/listings/" + listingId + "/")));
    }

    @Test
    void shouldRejectUploadForNonOwner() throws Exception {
        String adminToken = IntegrationTestFixtures.loginAsAdmin(mockMvc, jsonMapper);
        Long platformId = IntegrationTestFixtures.createPlatform(mockMvc, jsonMapper, adminToken, "Image Reject Platform");
        Long manufacturerId = IntegrationTestFixtures.createManufacturer(mockMvc, jsonMapper, adminToken, "Image Reject Manufacturer");
        String sellerToken = IntegrationTestFixtures.registerAndLogin(
            mockMvc, jsonMapper, "seller-image-owner@test.com", "Seller@123", "sellerimageowner", true
        );
        String buyerToken = IntegrationTestFixtures.registerAndLogin(
            mockMvc, jsonMapper, "buyer-image-owner@test.com", "Buyer@123", "buyerimageowner", false
        );

        Long listingId = IntegrationTestFixtures.createListing(
            mockMvc,
            jsonMapper,
            sellerToken,
            new CreateListingRequest(
                "Terranigma",
                "Outro anuncio com imagem",
                platformId,
                manufacturerId,
                "LOOSE",
                1,
                "DIRECT_SALE",
                new BigDecimal("125.00"),
                null,
                null,
                null,
                null
            )
        );

        MockMultipartFile file = new MockMultipartFile(
            "files",
            "cover.png",
            MediaType.IMAGE_PNG_VALUE,
            "fake-png-content".getBytes()
        );

        mockMvc.perform(multipart("/api/v1/listings/" + listingId + "/images")
                .file(file)
                .header("Authorization", "Bearer " + buyerToken))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value("You can only upload images to your own listings"));
    }
}
