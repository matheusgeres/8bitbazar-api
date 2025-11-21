package br.com.eightbitbazar.application.usecase.user;

import br.com.eightbitbazar.application.port.out.PasswordEncoder;
import br.com.eightbitbazar.application.port.out.UserRepository;
import br.com.eightbitbazar.domain.exception.BusinessException;
import br.com.eightbitbazar.domain.user.User;
import br.com.eightbitbazar.domain.user.UserId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RegisterUserTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    private RegisterUser registerUser;

    @BeforeEach
    void setUp() {
        registerUser = new RegisterUser(userRepository, passwordEncoder);
    }

    @Test
    void shouldRegisterUserSuccessfully() {
        // Given
        RegisterUserInput input = new RegisterUserInput(
            "test@example.com",
            "password123",
            "testuser",
            "Test User",
            "11999999999",
            null,
            false,
            null
        );

        when(userRepository.existsByEmail("test@example.com")).thenReturn(false);
        when(userRepository.existsByNickname("testuser")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            return user.withId(new UserId(1L));
        });

        // When
        RegisterUserOutput output = registerUser.execute(input);

        // Then
        assertNotNull(output);
        assertEquals(1L, output.id());
        assertEquals("test@example.com", output.email());
        assertEquals("testuser", output.nickname());
        assertEquals("Test User", output.fullName());
        assertFalse(output.isSeller());

        verify(userRepository).save(any(User.class));
        verify(passwordEncoder).encode("password123");
    }

    @Test
    void shouldThrowExceptionWhenEmailAlreadyExists() {
        // Given
        RegisterUserInput input = new RegisterUserInput(
            "existing@example.com",
            "password123",
            "newuser",
            "New User",
            null,
            null,
            false,
            null
        );

        when(userRepository.existsByEmail("existing@example.com")).thenReturn(true);

        // When/Then
        BusinessException exception = assertThrows(
            BusinessException.class,
            () -> registerUser.execute(input)
        );

        assertEquals("Email already registered", exception.getMessage());
        verify(userRepository, never()).save(any());
    }

    @Test
    void shouldThrowExceptionWhenNicknameAlreadyExists() {
        // Given
        RegisterUserInput input = new RegisterUserInput(
            "new@example.com",
            "password123",
            "existingnick",
            "New User",
            null,
            null,
            false,
            null
        );

        when(userRepository.existsByEmail("new@example.com")).thenReturn(false);
        when(userRepository.existsByNickname("existingnick")).thenReturn(true);

        // When/Then
        BusinessException exception = assertThrows(
            BusinessException.class,
            () -> registerUser.execute(input)
        );

        assertEquals("Nickname already taken", exception.getMessage());
        verify(userRepository, never()).save(any());
    }

    @Test
    void shouldThrowExceptionWhenPasswordTooShort() {
        // Given
        RegisterUserInput input = new RegisterUserInput(
            "test@example.com",
            "short",
            "testuser",
            "Test User",
            null,
            null,
            false,
            null
        );

        // When/Then
        BusinessException exception = assertThrows(
            BusinessException.class,
            () -> registerUser.execute(input)
        );

        assertEquals("Password must be at least 8 characters", exception.getMessage());
    }

    @Test
    void shouldRegisterSeller() {
        // Given
        RegisterUserInput input = new RegisterUserInput(
            "seller@example.com",
            "password123",
            "seller",
            "Seller User",
            "11999999999",
            "https://wa.me/5511999999999",
            true,
            new RegisterUserInput.AddressInput("Rua A", "City", "ST", "12345")
        );

        when(userRepository.existsByEmail("seller@example.com")).thenReturn(false);
        when(userRepository.existsByNickname("seller")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            return user.withId(new UserId(2L));
        });

        // When
        RegisterUserOutput output = registerUser.execute(input);

        // Then
        assertTrue(output.isSeller());
    }
}
