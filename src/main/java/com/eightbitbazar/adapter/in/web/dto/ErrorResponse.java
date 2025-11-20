package com.eightbitbazar.adapter.in.web.dto;

import java.time.LocalDateTime;

public record ErrorResponse(
    int status,
    String message,
    LocalDateTime timestamp
) {
    public ErrorResponse(int status, String message) {
        this(status, message, LocalDateTime.now());
    }
}
