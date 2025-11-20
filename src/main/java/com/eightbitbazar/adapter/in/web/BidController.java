package com.eightbitbazar.adapter.in.web;

import com.eightbitbazar.application.port.in.PlaceBidUseCase;
import com.eightbitbazar.application.usecase.bid.PlaceBidInput;
import com.eightbitbazar.application.usecase.bid.PlaceBidOutput;
import com.eightbitbazar.domain.user.UserId;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/listings/{listingId}/bids")
public class BidController {

    private final PlaceBidUseCase placeBidUseCase;

    public BidController(PlaceBidUseCase placeBidUseCase) {
        this.placeBidUseCase = placeBidUseCase;
    }

    @PostMapping
    public ResponseEntity<PlaceBidOutput> placeBid(
        @AuthenticationPrincipal Jwt jwt,
        @PathVariable Long listingId,
        @Valid @RequestBody PlaceBidRequest request
    ) {
        UserId userId = new UserId(Long.parseLong(jwt.getSubject()));

        PlaceBidInput input = new PlaceBidInput(listingId, request.amount());
        PlaceBidOutput output = placeBidUseCase.execute(userId, input);

        return ResponseEntity.status(HttpStatus.CREATED).body(output);
    }

    public record PlaceBidRequest(
        @NotNull(message = "Amount is required")
        @Positive(message = "Amount must be positive")
        BigDecimal amount
    ) {}
}
