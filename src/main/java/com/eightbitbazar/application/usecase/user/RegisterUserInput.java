package com.eightbitbazar.application.usecase.user;

public record RegisterUserInput(
    String email,
    String password,
    String nickname,
    String fullName,
    String phone,
    String whatsappLink,
    boolean isSeller,
    AddressInput address
) {
    public record AddressInput(
        String street,
        String city,
        String state,
        String zip
    ) {}
}
