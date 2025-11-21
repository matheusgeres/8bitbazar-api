package br.com.eightbitbazar.application.usecase.user;

public record UserProfileOutput(
    Long id,
    String email,
    String nickname,
    String fullName,
    String phone,
    String whatsappLink,
    String role,
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
