package br.com.eightbitbazar.domain.user;

public record Address(
    String street,
    String city,
    String state,
    String zip
) {
    public static Address empty() {
        return new Address(null, null, null, null);
    }
}
