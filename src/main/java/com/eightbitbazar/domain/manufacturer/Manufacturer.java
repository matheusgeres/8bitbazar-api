package com.eightbitbazar.domain.manufacturer;

import java.time.LocalDateTime;

public record Manufacturer(
    Long id,
    String name,
    LocalDateTime createdAt
) {
    public Manufacturer {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Manufacturer name is required");
        }
    }

    public Manufacturer withId(Long id) {
        return new Manufacturer(id, name, createdAt);
    }
}
