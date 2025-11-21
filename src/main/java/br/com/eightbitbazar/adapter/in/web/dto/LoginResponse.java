package br.com.eightbitbazar.adapter.in.web.dto;

public record LoginResponse(
    String accessToken,
    String tokenType,
    long expiresIn,
    Long userId,
    String email,
    String nickname,
    String role
) {}
