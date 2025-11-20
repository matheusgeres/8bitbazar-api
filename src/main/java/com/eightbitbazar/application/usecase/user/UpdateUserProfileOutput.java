package com.eightbitbazar.application.usecase.user;

public record UpdateUserProfileOutput(
    Long id,
    String email,
    String nickname,
    String fullName,
    String phone,
    String whatsappLink,
    boolean isSeller,
    AddressOutput address
) {
    public record AddressOutput(
        String street,
        String city,
        String state,
        String zip
    ) {}
}
