package br.com.eightbitbazar.adapter.in.web;

import br.com.eightbitbazar.IntegrationTestBase;
import br.com.eightbitbazar.adapter.in.web.dto.CreateListingRequest;
import br.com.eightbitbazar.adapter.in.web.dto.LoginRequest;
import br.com.eightbitbazar.adapter.in.web.dto.RegisterUserRequest;
import tools.jackson.databind.json.JsonMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@DisplayName("Testes de Fluxo Completo E2E")
class FullFlowIntegrationTest extends IntegrationTestBase {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JsonMapper jsonMapper;

    @Nested
    @DisplayName("Fluxo Completo: Admin → Vendedor → Comprador")
    @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
    @org.junit.jupiter.api.TestInstance(org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS)
    class CompleteFlow {

        private String adminToken;
        private String sellerToken;
        private String buyerToken;
        private Long platformId;
        private Long manufacturerId;
        private Long auctionListingId;
        private Long directSaleListingId;

        @Test
        @Order(1)
        @DisplayName("1. Admin faz login")
        void step1_adminLogin() throws Exception {
            LoginRequest loginRequest = new LoginRequest(
                "admin@8bitbazar.com",
                "Admin@123"
            );

            MvcResult result = mockMvc.perform(post("/api/v1/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(jsonMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").exists())
                .andExpect(jsonPath("$.role").value("ADMIN"))
                .andReturn();

            adminToken = extractToken(result);
        }

        @Test
        @Order(2)
        @DisplayName("2. Admin cadastra plataforma")
        void step2_adminCreatesPlatform() throws Exception {
            MvcResult result = mockMvc.perform(post("/api/v1/admin/platforms")
                    .header("Authorization", "Bearer " + adminToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"name\": \"Super Nintendo\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Super Nintendo"))
                .andReturn();

            platformId = extractId(result);
        }

        @Test
        @Order(3)
        @DisplayName("3. Admin cadastra fabricante")
        void step3_adminCreatesManufacturer() throws Exception {
            MvcResult result = mockMvc.perform(post("/api/v1/admin/manufacturers")
                    .header("Authorization", "Bearer " + adminToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"name\": \"Nintendo\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Nintendo"))
                .andReturn();

            manufacturerId = extractId(result);
        }

        @Test
        @Order(4)
        @DisplayName("4. Vendedor se registra e faz login")
        void step4_sellerRegistersAndLogins() throws Exception {
            RegisterUserRequest registerRequest = new RegisterUserRequest(
                "seller@test.com",
                "Seller@123",
                "retroseller",
                "Vendedor Retro",
                "11999999999",
                "https://wa.me/5511999999999",
                true,
                new RegisterUserRequest.AddressRequest(
                    "Rua dos Games, 100",
                    "São Paulo",
                    "SP",
                    "01234-567"
                )
            );

            mockMvc.perform(post("/api/v1/auth/register")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(jsonMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isCreated());

            LoginRequest loginRequest = new LoginRequest("seller@test.com", "Seller@123");

            MvcResult result = mockMvc.perform(post("/api/v1/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(jsonMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andReturn();

            sellerToken = extractToken(result);
        }

        @Test
        @Order(5)
        @DisplayName("5. Vendedor cria leilão")
        void step5_sellerCreatesAuction() throws Exception {
            CreateListingRequest auctionRequest = new CreateListingRequest(
                "Super Mario World Original",
                "Cartucho em excelente estado, testado e funcionando",
                platformId,
                manufacturerId,
                "EXCELLENT",
                1,
                "AUCTION",
                null,
                new BigDecimal("50.00"),
                new BigDecimal("150.00"),
                LocalDateTime.now().plusDays(7),
                null
            );

            MvcResult result = mockMvc.perform(post("/api/v1/listings")
                    .header("Authorization", "Bearer " + sellerToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(jsonMapper.writeValueAsString(auctionRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Super Mario World Original"))
                .andExpect(jsonPath("$.type").value("AUCTION"))
                .andReturn();

            auctionListingId = extractId(result);
        }

        @Test
        @Order(6)
        @DisplayName("6. Vendedor cria venda direta")
        void step6_sellerCreatesDirectSale() throws Exception {
            CreateListingRequest directSaleRequest = new CreateListingRequest(
                "Donkey Kong Country 2",
                "Cartucho original com manual",
                platformId,
                manufacturerId,
                "GOOD",
                1,
                "DIRECT_SALE",
                new BigDecimal("80.00"),
                null,
                null,
                null,
                new BigDecimal("5.00")
            );

            MvcResult result = mockMvc.perform(post("/api/v1/listings")
                    .header("Authorization", "Bearer " + sellerToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(jsonMapper.writeValueAsString(directSaleRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Donkey Kong Country 2"))
                .andExpect(jsonPath("$.type").value("DIRECT_SALE"))
                .andReturn();

            directSaleListingId = extractId(result);
        }

        @Test
        @Order(7)
        @DisplayName("7. Comprador se registra e faz login")
        void step7_buyerRegistersAndLogins() throws Exception {
            RegisterUserRequest registerRequest = new RegisterUserRequest(
                "buyer@test.com",
                "Buyer@123",
                "retrobuyer",
                "Comprador Retro",
                "11888888888",
                null,
                false,
                new RegisterUserRequest.AddressRequest(
                    "Av. dos Colecionadores, 200",
                    "Rio de Janeiro",
                    "RJ",
                    "20000-000"
                )
            );

            mockMvc.perform(post("/api/v1/auth/register")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(jsonMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isCreated());

            LoginRequest loginRequest = new LoginRequest("buyer@test.com", "Buyer@123");

            MvcResult result = mockMvc.perform(post("/api/v1/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(jsonMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andReturn();

            buyerToken = extractToken(result);
        }

        @Test
        @Order(8)
        @DisplayName("8. Comprador pesquisa anúncios")
        void step8_buyerSearchesListings() throws Exception {
            mockMvc.perform(get("/api/v1/listings")
                    .header("Authorization", "Bearer " + buyerToken)
                    .param("search", "Mario"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());

            mockMvc.perform(get("/api/v1/listings")
                    .header("Authorization", "Bearer " + buyerToken)
                    .param("platformId", platformId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
        }

        @Test
        @Order(9)
        @DisplayName("9. Comprador visualiza detalhes do anúncio")
        void step9_buyerViewsListingDetails() throws Exception {
            mockMvc.perform(get("/api/v1/listings/" + auctionListingId)
                    .header("Authorization", "Bearer " + buyerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Super Mario World Original"));

            mockMvc.perform(get("/api/v1/listings/" + directSaleListingId)
                    .header("Authorization", "Bearer " + buyerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Donkey Kong Country 2"));
        }

        @Test
        @Order(10)
        @DisplayName("10. Comprador dá lance no leilão")
        void step10_buyerPlacesBid() throws Exception {
            mockMvc.perform(post("/api/v1/listings/" + auctionListingId + "/bids")
                    .header("Authorization", "Bearer " + buyerToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"amount\": 60.00}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.amount").value(60.00));
        }

        @Test
        @Order(11)
        @DisplayName("11. Comprador faz compra direta")
        void step11_buyerMakesDirectPurchase() throws Exception {
            mockMvc.perform(post("/api/v1/listings/" + directSaleListingId + "/purchase")
                    .header("Authorization", "Bearer " + buyerToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"paymentMethod\": \"PIX\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.listingId").value(directSaleListingId));
        }
    }

    @Nested
    @DisplayName("Fluxo de Autenticação")
    class AuthenticationFlow {

        @Test
        @DisplayName("Deve registrar novo usuário e fazer login")
        void shouldRegisterAndLogin() throws Exception {
            RegisterUserRequest registerRequest = new RegisterUserRequest(
                "newuser@test.com",
                "Password@123",
                "newuser",
                "New User",
                null,
                null,
                false,
                null
            );

            mockMvc.perform(post("/api/v1/auth/register")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(jsonMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value("newuser@test.com"));

            LoginRequest loginRequest = new LoginRequest("newuser@test.com", "Password@123");

            mockMvc.perform(post("/api/v1/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(jsonMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").exists())
                .andExpect(jsonPath("$.tokenType").value("Bearer"));
        }

        @Test
        @DisplayName("Deve rejeitar login com credenciais inválidas")
        void shouldRejectInvalidCredentials() throws Exception {
            LoginRequest loginRequest = new LoginRequest("invalid@test.com", "wrongpassword");

            mockMvc.perform(post("/api/v1/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(jsonMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isUnauthorized());
        }
    }

    @Nested
    @DisplayName("Fluxo de Administração")
    class AdminFlow {

        @Test
        @DisplayName("Admin deve criar e listar plataformas")
        void adminShouldCreateAndListPlatforms() throws Exception {
            String token = loginAsAdmin();

            mockMvc.perform(post("/api/v1/admin/platforms")
                    .header("Authorization", "Bearer " + token)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"name\": \"PlayStation\"}"))
                .andExpect(status().isCreated());

            mockMvc.perform(post("/api/v1/admin/platforms")
                    .header("Authorization", "Bearer " + token)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"name\": \"Mega Drive\"}"))
                .andExpect(status().isCreated());

            mockMvc.perform(get("/api/v1/admin/platforms")
                    .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
        }

        @Test
        @DisplayName("Admin deve criar e listar fabricantes")
        void adminShouldCreateAndListManufacturers() throws Exception {
            String token = loginAsAdmin();

            mockMvc.perform(post("/api/v1/admin/manufacturers")
                    .header("Authorization", "Bearer " + token)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"name\": \"Sega\"}"))
                .andExpect(status().isCreated());

            mockMvc.perform(post("/api/v1/admin/manufacturers")
                    .header("Authorization", "Bearer " + token)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"name\": \"Sony\"}"))
                .andExpect(status().isCreated());

            mockMvc.perform(get("/api/v1/admin/manufacturers")
                    .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
        }

        @Test
        @DisplayName("Usuário comum não deve acessar endpoints de admin")
        void regularUserShouldNotAccessAdminEndpoints() throws Exception {
            RegisterUserRequest registerRequest = new RegisterUserRequest(
                "regular@test.com",
                "Regular@123",
                "regularuser",
                "Regular User",
                null,
                null,
                false,
                null
            );

            mockMvc.perform(post("/api/v1/auth/register")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(jsonMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isCreated());

            LoginRequest loginRequest = new LoginRequest("regular@test.com", "Regular@123");
            MvcResult result = mockMvc.perform(post("/api/v1/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(jsonMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andReturn();

            String userToken = extractToken(result);

            mockMvc.perform(get("/api/v1/admin/platforms")
                    .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isForbidden());
        }
    }

    @Nested
    @DisplayName("Fluxo de Venda")
    class SaleFlow {

        @Test
        @DisplayName("Vendedor deve criar listing e gerenciar")
        void sellerShouldCreateAndManageListing() throws Exception {
            String adminToken = loginAsAdmin();

            MvcResult platformResult = mockMvc.perform(post("/api/v1/admin/platforms")
                    .header("Authorization", "Bearer " + adminToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"name\": \"Game Boy\"}"))
                .andExpect(status().isCreated())
                .andReturn();
            Long platformId = extractId(platformResult);

            MvcResult manufacturerResult = mockMvc.perform(post("/api/v1/admin/manufacturers")
                    .header("Authorization", "Bearer " + adminToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"name\": \"Nintendo GB\"}"))
                .andExpect(status().isCreated())
                .andReturn();
            Long manufacturerId = extractId(manufacturerResult);

            String sellerToken = registerAndLogin("seller2@test.com", "Seller2@123", "seller2", true);

            CreateListingRequest listingRequest = new CreateListingRequest(
                "Pokemon Red",
                "Cartucho original americano",
                platformId,
                manufacturerId,
                "GOOD",
                1,
                "SHOWCASE",
                null,
                null,
                null,
                null,
                null
            );

            MvcResult listingResult = mockMvc.perform(post("/api/v1/listings")
                    .header("Authorization", "Bearer " + sellerToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(jsonMapper.writeValueAsString(listingRequest)))
                .andExpect(status().isCreated())
                .andReturn();
            Long listingId = extractId(listingResult);

            mockMvc.perform(get("/api/v1/listings/" + listingId)
                    .header("Authorization", "Bearer " + sellerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Pokemon Red"));

            mockMvc.perform(delete("/api/v1/listings/" + listingId)
                    .header("Authorization", "Bearer " + sellerToken))
                .andExpect(status().isNoContent());
        }
    }

    @Nested
    @DisplayName("Fluxo de Compra")
    class PurchaseFlow {

        @Test
        @DisplayName("Comprador deve pesquisar, visualizar e comprar")
        void buyerShouldSearchViewAndPurchase() throws Exception {
            String adminToken = loginAsAdmin();

            MvcResult platformResult = mockMvc.perform(post("/api/v1/admin/platforms")
                    .header("Authorization", "Bearer " + adminToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"name\": \"Atari 2600\"}"))
                .andExpect(status().isCreated())
                .andReturn();
            Long platformId = extractId(platformResult);

            MvcResult manufacturerResult = mockMvc.perform(post("/api/v1/admin/manufacturers")
                    .header("Authorization", "Bearer " + adminToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"name\": \"Atari\"}"))
                .andExpect(status().isCreated())
                .andReturn();
            Long manufacturerId = extractId(manufacturerResult);

            String sellerToken = registerAndLogin("seller3@test.com", "Seller3@123", "seller3", true);

            CreateListingRequest listingRequest = new CreateListingRequest(
                "Pitfall",
                "Jogo clássico do Atari",
                platformId,
                manufacturerId,
                "FAIR",
                1,
                "DIRECT_SALE",
                new BigDecimal("45.00"),
                null,
                null,
                null,
                null
            );

            MvcResult listingResult = mockMvc.perform(post("/api/v1/listings")
                    .header("Authorization", "Bearer " + sellerToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(jsonMapper.writeValueAsString(listingRequest)))
                .andExpect(status().isCreated())
                .andReturn();
            Long listingId = extractId(listingResult);

            String buyerToken = registerAndLogin("buyer2@test.com", "Buyer2@123", "buyer2", false);

            mockMvc.perform(get("/api/v1/listings")
                    .header("Authorization", "Bearer " + buyerToken)
                    .param("search", "Pitfall"))
                .andExpect(status().isOk());

            mockMvc.perform(get("/api/v1/listings/" + listingId)
                    .header("Authorization", "Bearer " + buyerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Pitfall"));

            mockMvc.perform(post("/api/v1/listings/" + listingId + "/purchase")
                    .header("Authorization", "Bearer " + buyerToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"paymentMethod\": \"CREDIT_CARD\"}"))
                .andExpect(status().isCreated());
        }
    }

    private String loginAsAdmin() throws Exception {
        LoginRequest loginRequest = new LoginRequest("admin@8bitbazar.com", "Admin@123");

        MvcResult result = mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonMapper.writeValueAsString(loginRequest)))
            .andExpect(status().isOk())
            .andReturn();

        return extractToken(result);
    }

    private String registerAndLogin(String email, String password, String nickname, boolean isSeller) throws Exception {
        RegisterUserRequest registerRequest = new RegisterUserRequest(
            email,
            password,
            nickname,
            "Test User " + nickname,
            null,
            null,
            isSeller,
            new RegisterUserRequest.AddressRequest(
                "Rua Teste, 123",
                "São Paulo",
                "SP",
                "01234-567"
            )
        );

        mockMvc.perform(post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonMapper.writeValueAsString(registerRequest)))
            .andExpect(status().isCreated());

        LoginRequest loginRequest = new LoginRequest(email, password);

        MvcResult result = mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonMapper.writeValueAsString(loginRequest)))
            .andExpect(status().isOk())
            .andReturn();

        return extractToken(result);
    }

    private String extractToken(MvcResult result) throws Exception {
        String response = result.getResponse().getContentAsString();
        return jsonMapper.readTree(response).get("accessToken").asString();
    }

    private Long extractId(MvcResult result) throws Exception {
        String response = result.getResponse().getContentAsString();
        return jsonMapper.readTree(response).get("id").asLong();
    }
}
