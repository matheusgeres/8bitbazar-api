package br.com.eightbitbazar.domain.user;

import java.time.LocalDateTime;

public record User(
    UserId id,
    String email,
    String password,
    String nickname,
    String fullName,
    String phone,
    String whatsappLink,
    Role role,
    boolean isSeller,
    Address address,
    LocalDateTime createdAt,
    LocalDateTime deletedAt
) {
    public User {
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("Email is required");
        }
        if (password == null || password.isBlank()) {
            throw new IllegalArgumentException("Password is required");
        }
        if (nickname == null || nickname.isBlank()) {
            throw new IllegalArgumentException("Nickname is required");
        }
        if (fullName == null || fullName.isBlank()) {
            throw new IllegalArgumentException("Full name is required");
        }
        if (role == null) {
            role = Role.USER;
        }
        if (address == null) {
            address = Address.empty();
        }
    }

    public boolean isDeleted() {
        return deletedAt != null;
    }

    public boolean isAdmin() {
        return role == Role.ADMIN;
    }

    public User withId(UserId id) {
        return new User(id, email, password, nickname, fullName, phone, whatsappLink, role, isSeller, address, createdAt, deletedAt);
    }

    public User withPassword(String password) {
        return new User(id, email, password, nickname, fullName, phone, whatsappLink, role, isSeller, address, createdAt, deletedAt);
    }

    public User withDeletedAt(LocalDateTime deletedAt) {
        return new User(id, email, password, nickname, fullName, phone, whatsappLink, role, isSeller, address, createdAt, deletedAt);
    }
}
