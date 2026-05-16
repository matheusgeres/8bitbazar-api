package br.com.eightbitbazar.adapter.in.web;

import br.com.eightbitbazar.adapter.in.web.dto.PurchaseRequest;
import br.com.eightbitbazar.application.port.in.DirectPurchaseUseCase;
import br.com.eightbitbazar.application.usecase.purchase.DirectPurchaseInput;
import br.com.eightbitbazar.application.usecase.purchase.DirectPurchaseOutput;
import br.com.eightbitbazar.domain.user.UserId;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/v1/listings/{listingId}/purchase")
public class PurchaseController {

    private final DirectPurchaseUseCase directPurchaseUseCase;

    public PurchaseController(DirectPurchaseUseCase directPurchaseUseCase) {
        this.directPurchaseUseCase = directPurchaseUseCase;
    }

    @PostMapping
    public ResponseEntity<DirectPurchaseOutput> purchase(
        @AuthenticationPrincipal Jwt jwt,
        @PathVariable Long listingId,
        @Valid @RequestBody PurchaseRequest request
    ) {
        UserId buyerId = new UserId(Long.parseLong(jwt.getSubject()));

        DirectPurchaseInput input = new DirectPurchaseInput(listingId, request.paymentMethod());
        log.atInfo()
            .addKeyValue("listingId", listingId)
            .addKeyValue("buyerId", buyerId.value())
            .addKeyValue("paymentMethod", request.paymentMethod())
            .log("purchase.requested");
        DirectPurchaseOutput output = directPurchaseUseCase.execute(buyerId, input);
        log.atInfo()
            .addKeyValue("purchaseId", output.id())
            .addKeyValue("listingId", listingId)
            .addKeyValue("finalAmount", output.finalAmount())
            .log("purchase.completed");

        return ResponseEntity.status(HttpStatus.CREATED).body(output);
    }

}
