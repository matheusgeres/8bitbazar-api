package com.eightbitbazar.adapter.in.web;

import com.eightbitbazar.adapter.in.web.dto.ErrorResponse;
import com.eightbitbazar.adapter.in.web.dto.LoginRequest;
import com.eightbitbazar.adapter.in.web.dto.LoginResponse;
import com.eightbitbazar.adapter.in.web.dto.RegisterUserRequest;
import com.eightbitbazar.adapter.in.web.dto.RegisterUserResponse;
import com.eightbitbazar.application.port.in.RegisterUserUseCase;
import com.eightbitbazar.application.port.out.UserRepository;
import com.eightbitbazar.application.usecase.user.RegisterUserInput;
import com.eightbitbazar.application.usecase.user.RegisterUserOutput;
import com.eightbitbazar.domain.user.User;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private static final long ACCESS_TOKEN_TTL_SECONDS = 3600;

    private final RegisterUserUseCase registerUserUseCase;
    private final AuthenticationManager authenticationManager;
    private final JwtEncoder jwtEncoder;
    private final UserRepository userRepository;
    private final String issuer;

    public AuthController(
        RegisterUserUseCase registerUserUseCase,
        AuthenticationManager authenticationManager,
        JwtEncoder jwtEncoder,
        UserRepository userRepository,
        @Value("${spring.security.oauth2.authorizationserver.issuer:http://localhost:8080}") String issuer
    ) {
        this.registerUserUseCase = registerUserUseCase;
        this.authenticationManager = authenticationManager;
        this.jwtEncoder = jwtEncoder;
        this.userRepository = userRepository;
        this.issuer = issuer;
    }

    @PostMapping("/register")
    public ResponseEntity<RegisterUserResponse> register(@Valid @RequestBody RegisterUserRequest request) {
        RegisterUserInput input = new RegisterUserInput(
            request.email(),
            request.password(),
            request.nickname(),
            request.fullName(),
            request.phone(),
            request.whatsappLink(),
            request.isSeller(),
            request.address() != null
                ? new RegisterUserInput.AddressInput(
                    request.address().street(),
                    request.address().city(),
                    request.address().state(),
                    request.address().zip()
                )
                : null
        );

        RegisterUserOutput output = registerUserUseCase.execute(input);

        RegisterUserResponse response = new RegisterUserResponse(
            output.id(),
            output.email(),
            output.nickname(),
            output.fullName(),
            output.isSeller()
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(request.email(), request.password())
        );

        User user = userRepository.findByEmail(request.email())
            .filter(u -> !u.isDeleted())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Credenciais inválidas"));

        Instant now = Instant.now();
        JwtClaimsSet claims = JwtClaimsSet.builder()
            .issuer(issuer)
            .issuedAt(now)
            .expiresAt(now.plusSeconds(ACCESS_TOKEN_TTL_SECONDS))
            .subject(user.id().value().toString())
            .claim("email", user.email())
            .claim("nickname", user.nickname())
            .claim("roles", List.of(user.role().name()))
            .build();

        String token = jwtEncoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();

        LoginResponse response = new LoginResponse(
            token,
            "Bearer",
            ACCESS_TOKEN_TTL_SECONDS,
            user.id().value(),
            user.email(),
            user.nickname(),
            user.role().name()
        );

        return ResponseEntity.ok(response);
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<ErrorResponse> forgotPassword() {
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED)
            .body(new ErrorResponse(501, "Funcionalidade em desenvolvimento"));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<ErrorResponse> resetPassword() {
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED)
            .body(new ErrorResponse(501, "Funcionalidade em desenvolvimento"));
    }
}
