package br.com.eightbitbazar.adapter.in.web.dto;

import br.com.eightbitbazar.adapter.in.web.validation.ValidItemCondition;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record CreateListingRequest(
    @NotBlank(message = "Name is required")
    String name,

    String description,

    @NotNull(message = "Platform is required")
    Long platformId,

    @NotNull(message = "Manufacturer is required")
    Long manufacturerId,

    @NotBlank(message = "Condition is required")
    @ValidItemCondition(message = "Invalid condition value. Valid values are: SEALED, COMPLETE, LOOSE, DAMAGED")
    String condition,

    @Positive(message = "Quantity must be positive")
    int quantity,

    @NotBlank(message = "Type is required")
    String type,

    BigDecimal price,
    BigDecimal startingPrice,
    BigDecimal buyNowPrice,
    LocalDateTime auctionEndDate,
    BigDecimal cashDiscountPercent
) {}
