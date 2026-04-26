package br.com.eightbitbazar.adapter.in.web;

import br.com.eightbitbazar.adapter.in.web.dto.UserTradeHistoryResponse;
import br.com.eightbitbazar.adapter.in.web.dto.UpdateUserRequest;
import br.com.eightbitbazar.adapter.in.web.dto.UserResponse;
import br.com.eightbitbazar.application.port.in.DeleteUserUseCase;
import br.com.eightbitbazar.application.port.in.GetMyPurchasesUseCase;
import br.com.eightbitbazar.application.port.in.GetMySalesUseCase;
import br.com.eightbitbazar.application.port.in.GetUserProfileUseCase;
import br.com.eightbitbazar.application.port.in.UpdateUserProfileUseCase;
import br.com.eightbitbazar.application.usecase.user.UpdateUserProfileInput;
import br.com.eightbitbazar.application.usecase.user.UpdateUserProfileOutput;
import br.com.eightbitbazar.application.usecase.user.UserTradeHistoryItemOutput;
import br.com.eightbitbazar.application.usecase.user.UserProfileOutput;
import br.com.eightbitbazar.domain.user.UserId;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import static net.logstash.logback.argument.StructuredArguments.kv;

@Slf4j
@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    private final GetUserProfileUseCase getUserProfileUseCase;
    private final UpdateUserProfileUseCase updateUserProfileUseCase;
    private final DeleteUserUseCase deleteUserUseCase;
    private final GetMyPurchasesUseCase getMyPurchasesUseCase;
    private final GetMySalesUseCase getMySalesUseCase;

    public UserController(
        GetUserProfileUseCase getUserProfileUseCase,
        UpdateUserProfileUseCase updateUserProfileUseCase,
        DeleteUserUseCase deleteUserUseCase,
        GetMyPurchasesUseCase getMyPurchasesUseCase,
        GetMySalesUseCase getMySalesUseCase
    ) {
        this.getUserProfileUseCase = getUserProfileUseCase;
        this.updateUserProfileUseCase = updateUserProfileUseCase;
        this.deleteUserUseCase = deleteUserUseCase;
        this.getMyPurchasesUseCase = getMyPurchasesUseCase;
        this.getMySalesUseCase = getMySalesUseCase;
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
        log.info("user.profile.updated", kv("userId", userId.value()));

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
        log.warn("user.delete.requested", kv("userId", userId.value()));
        deleteUserUseCase.execute(userId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/me/purchases")
    public ResponseEntity<Page<UserTradeHistoryResponse>> getMyPurchases(
        @AuthenticationPrincipal Jwt jwt,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "20") int size
    ) {
        validatePagination(page, size);
        UserId userId = new UserId(Long.parseLong(jwt.getSubject()));
        Page<UserTradeHistoryResponse> response = getMyPurchasesUseCase.execute(userId, page, size)
            .map(this::toTradeHistoryResponse);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/me/sales")
    public ResponseEntity<Page<UserTradeHistoryResponse>> getMySales(
        @AuthenticationPrincipal Jwt jwt,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "20") int size
    ) {
        validatePagination(page, size);
        UserId userId = new UserId(Long.parseLong(jwt.getSubject()));
        Page<UserTradeHistoryResponse> response = getMySalesUseCase.execute(userId, page, size)
            .map(this::toTradeHistoryResponse);
        return ResponseEntity.ok(response);
    }

    private void validatePagination(int page, int size) {
        if (page < 0) {
            throw new IllegalArgumentException("Page must be greater than or equal to 0");
        }
        if (size < 1 || size > 100) {
            throw new IllegalArgumentException("Size must be between 1 and 100");
        }
    }

    private UserTradeHistoryResponse toTradeHistoryResponse(UserTradeHistoryItemOutput output) {
        return new UserTradeHistoryResponse(
            output.purchaseId(),
            output.listingId(),
            output.listingTitle(),
            output.listingType(),
            output.listingStatus(),
            output.amount(),
            output.finalAmount(),
            output.paymentMethod(),
            output.purchaseStatus(),
            output.createdAt()
        );
    }
}
