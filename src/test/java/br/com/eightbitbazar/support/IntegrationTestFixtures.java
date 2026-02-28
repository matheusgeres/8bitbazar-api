package br.com.eightbitbazar.support;

import br.com.eightbitbazar.adapter.in.web.dto.CreateListingRequest;
import br.com.eightbitbazar.adapter.in.web.dto.LoginRequest;
import br.com.eightbitbazar.adapter.in.web.dto.RegisterUserRequest;
import tools.jackson.databind.json.JsonMapper;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public final class IntegrationTestFixtures {

    private IntegrationTestFixtures() {
    }

    public static String loginAsAdmin(MockMvc mockMvc, JsonMapper jsonMapper) throws Exception {
        LoginRequest loginRequest = new LoginRequest("admin@8bitbazar.com", "Admin@123");

        MvcResult result = mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonMapper.writeValueAsString(loginRequest)))
            .andExpect(status().isOk())
            .andReturn();

        return extractToken(jsonMapper, result);
    }

    public static String registerAndLogin(
        MockMvc mockMvc,
        JsonMapper jsonMapper,
        String email,
        String password,
        String nickname,
        boolean isSeller
    ) throws Exception {
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

        return extractToken(jsonMapper, result);
    }

    public static String extractToken(JsonMapper jsonMapper, MvcResult result) throws Exception {
        String response = result.getResponse().getContentAsString();
        return jsonMapper.readTree(response).get("accessToken").asString();
    }

    public static Long extractId(JsonMapper jsonMapper, MvcResult result) throws Exception {
        String response = result.getResponse().getContentAsString();
        return jsonMapper.readTree(response).get("id").asLong();
    }

    public static Long createPlatform(MockMvc mockMvc, JsonMapper jsonMapper, String adminToken, String name) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/v1/admin/platforms")
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"" + name + "\"}"))
            .andExpect(status().isCreated())
            .andReturn();

        return extractId(jsonMapper, result);
    }

    public static Long createManufacturer(MockMvc mockMvc, JsonMapper jsonMapper, String adminToken, String name) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/v1/admin/manufacturers")
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"" + name + "\"}"))
            .andExpect(status().isCreated())
            .andReturn();

        return extractId(jsonMapper, result);
    }

    public static Long createListing(
        MockMvc mockMvc,
        JsonMapper jsonMapper,
        String sellerToken,
        CreateListingRequest request
    ) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/v1/listings")
                .header("Authorization", "Bearer " + sellerToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andReturn();

        return extractId(jsonMapper, result);
    }
}
