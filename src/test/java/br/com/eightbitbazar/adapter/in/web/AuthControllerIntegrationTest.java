package br.com.eightbitbazar.adapter.in.web;

import br.com.eightbitbazar.IntegrationTestBase;
import br.com.eightbitbazar.adapter.in.web.dto.RegisterUserRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@AutoConfigureMockMvc
class AuthControllerIntegrationTest extends IntegrationTestBase {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void shouldRegisterUser() throws Exception {
        RegisterUserRequest request = new RegisterUserRequest(
            "test@example.com",
            "password123",
            "testuser",
            "Test User",
            "11999999999",
            "https://wa.me/5511999999999",
            false,
            new RegisterUserRequest.AddressRequest(
                "Rua Teste, 123",
                "São Paulo",
                "SP",
                "01234-567"
            )
        );

        mockMvc.perform(post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.email").value("test@example.com"))
            .andExpect(jsonPath("$.nickname").value("testuser"))
            .andExpect(jsonPath("$.fullName").value("Test User"));
    }

    @Test
    void shouldRejectDuplicateEmail() throws Exception {
        RegisterUserRequest request = new RegisterUserRequest(
            "duplicate@example.com",
            "password123",
            "user1",
            "User One",
            null,
            null,
            false,
            null
        );

        // First registration
        mockMvc.perform(post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated());

        // Second registration with same email
        RegisterUserRequest duplicateRequest = new RegisterUserRequest(
            "duplicate@example.com",
            "password123",
            "user2",
            "User Two",
            null,
            null,
            false,
            null
        );

        mockMvc.perform(post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(duplicateRequest)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value("Email already registered"));
    }

    @Test
    void shouldRejectInvalidPassword() throws Exception {
        RegisterUserRequest request = new RegisterUserRequest(
            "short@example.com",
            "short",
            "shortpwd",
            "Short Password",
            null,
            null,
            false,
            null
        );

        mockMvc.perform(post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest());
    }

    @Test
    void forgotPasswordShouldReturn501() throws Exception {
        mockMvc.perform(post("/api/v1/auth/forgot-password"))
            .andExpect(status().isNotImplemented())
            .andExpect(jsonPath("$.message").value("Funcionalidade em desenvolvimento"));
    }
}
