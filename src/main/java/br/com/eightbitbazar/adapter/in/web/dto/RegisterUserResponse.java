package br.com.eightbitbazar.adapter.in.web.dto;

public record RegisterUserResponse(
    Long id,
    String email,
    String nickname,
    String fullName,
    boolean isSeller
) {}
