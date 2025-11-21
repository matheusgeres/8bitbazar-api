package br.com.eightbitbazar.adapter.in.web.dto;

public record UpdateUserRequest(
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
