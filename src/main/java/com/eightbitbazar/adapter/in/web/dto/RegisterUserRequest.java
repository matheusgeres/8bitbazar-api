package com.eightbitbazar.adapter.in.web.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterUserRequest(
    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    String email,

    @NotBlank(message = "Password is required")
    @Size(min = 8, message = "Password must be at least 8 characters")
    String password,

    @NotBlank(message = "Nickname is required")
    String nickname,

    @NotBlank(message = "Full name is required")
    String fullName,

    String phone,
    String whatsappLink,
    boolean isSeller,
    AddressRequest address
) {
    public record AddressRequest(
        String street,
        String city,
        String state,
        String zip
    ) {}
}
