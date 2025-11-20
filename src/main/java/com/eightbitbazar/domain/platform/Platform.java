package com.eightbitbazar.domain.platform;

import java.time.LocalDateTime;

public record Platform(
    Long id,
    String name,
    LocalDateTime createdAt
) {
    public Platform {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Platform name is required");
        }
    }

    public Platform withId(Long id) {
        return new Platform(id, name, createdAt);
    }
}
