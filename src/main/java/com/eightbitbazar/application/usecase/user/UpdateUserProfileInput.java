package com.eightbitbazar.application.usecase.user;

public record UpdateUserProfileInput(
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
