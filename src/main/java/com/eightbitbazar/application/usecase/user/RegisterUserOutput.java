package com.eightbitbazar.application.usecase.user;

public record RegisterUserOutput(
    Long id,
    String email,
    String nickname,
    String fullName,
    boolean isSeller
) {}
