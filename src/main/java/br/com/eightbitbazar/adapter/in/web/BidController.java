package br.com.eightbitbazar.adapter.in.web;

import br.com.eightbitbazar.application.port.in.PlaceBidUseCase;
import br.com.eightbitbazar.application.usecase.bid.PlaceBidInput;
import br.com.eightbitbazar.application.usecase.bid.PlaceBidOutput;
import br.com.eightbitbazar.domain.user.UserId;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@Slf4j
@RestController
@RequestMapping("/api/v1/listings/{listingId}/bids")
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
        log.atInfo()
            .addKeyValue("listingId", listingId)
            .addKeyValue("userId", userId.value())
            .addKeyValue("amount", request.amount())
            .log("bid.place.requested");
        PlaceBidOutput output = placeBidUseCase.execute(userId, input);
        log.atInfo()
            .addKeyValue("bidId", output.id())
            .addKeyValue("listingId", listingId)
            .addKeyValue("convertedToPurchase", output.convertedToPurchase())
            .log("bid.placed");

        return ResponseEntity.status(HttpStatus.CREATED).body(output);
    }

    public record PlaceBidRequest(
        @NotNull(message = "Amount is required")
        @Positive(message = "Amount must be positive")
        BigDecimal amount
    ) {}
}
