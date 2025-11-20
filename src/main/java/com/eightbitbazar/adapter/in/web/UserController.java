package com.eightbitbazar.adapter.in.web;

import com.eightbitbazar.adapter.in.web.dto.UpdateUserRequest;
import com.eightbitbazar.adapter.in.web.dto.UserResponse;
import com.eightbitbazar.application.port.in.DeleteUserUseCase;
import com.eightbitbazar.application.port.in.GetUserProfileUseCase;
import com.eightbitbazar.application.port.in.UpdateUserProfileUseCase;
import com.eightbitbazar.application.usecase.user.UpdateUserProfileInput;
import com.eightbitbazar.application.usecase.user.UpdateUserProfileOutput;
import com.eightbitbazar.application.usecase.user.UserProfileOutput;
import com.eightbitbazar.domain.user.UserId;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
public class UserController {

    private final GetUserProfileUseCase getUserProfileUseCase;
    private final UpdateUserProfileUseCase updateUserProfileUseCase;
    private final DeleteUserUseCase deleteUserUseCase;

    public UserController(
        GetUserProfileUseCase getUserProfileUseCase,
        UpdateUserProfileUseCase updateUserProfileUseCase,
        DeleteUserUseCase deleteUserUseCase
    ) {
        this.getUserProfileUseCase = getUserProfileUseCase;
        this.updateUserProfileUseCase = updateUserProfileUseCase;
        this.deleteUserUseCase = deleteUserUseCase;
    }

    @GetMapping("/me")
    public ResponseEntity<UserResponse> getProfile(@AuthenticationPrincipal Jwt jwt) {
        UserId userId = new UserId(Long.parseLong(jwt.getSubject()));
        UserProfileOutput output = getUserProfileUseCase.execute(userId);

        UserResponse response = new UserResponse(
            output.id(),
            output.email(),
            output.nickname(),
            output.fullName(),
            output.phone(),
            output.whatsappLink(),
            output.role(),
            output.isSeller(),
            new UserResponse.AddressResponse(
                output.address().street(),
                output.address().city(),
                output.address().state(),
                output.address().zip()
            )
        );

        return ResponseEntity.ok(response);
    }

    @PutMapping("/me")
    public ResponseEntity<UserResponse> updateProfile(
        @AuthenticationPrincipal Jwt jwt,
        @RequestBody UpdateUserRequest request
    ) {
        UserId userId = new UserId(Long.parseLong(jwt.getSubject()));

        UpdateUserProfileInput input = new UpdateUserProfileInput(
            request.fullName(),
            request.phone(),
            request.whatsappLink(),
            request.isSeller(),
            request.address() != null
                ? new UpdateUserProfileInput.AddressInput(
                    request.address().street(),
                    request.address().city(),
                    request.address().state(),
                    request.address().zip()
                )
                : null
        );

        UpdateUserProfileOutput output = updateUserProfileUseCase.execute(userId, input);

        UserResponse response = new UserResponse(
            output.id(),
            output.email(),
            output.nickname(),
            output.fullName(),
            output.phone(),
            output.whatsappLink(),
            "USER",
            output.isSeller(),
            new UserResponse.AddressResponse(
                output.address().street(),
                output.address().city(),
                output.address().state(),
                output.address().zip()
            )
        );

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/me")
    public ResponseEntity<Void> deleteProfile(@AuthenticationPrincipal Jwt jwt) {
        UserId userId = new UserId(Long.parseLong(jwt.getSubject()));
        deleteUserUseCase.execute(userId);
        return ResponseEntity.noContent().build();
    }
}
