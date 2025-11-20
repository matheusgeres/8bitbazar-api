package com.eightbitbazar.adapter.in.web.dto;

public record UserResponse(
    Long id,
    String email,
    String nickname,
    String fullName,
    String phone,
    String whatsappLink,
    String role,
    boolean isSeller,
    AddressResponse address
) {
    public record AddressResponse(
        String street,
        String city,
        String state,
        String zip
    ) {}
}
