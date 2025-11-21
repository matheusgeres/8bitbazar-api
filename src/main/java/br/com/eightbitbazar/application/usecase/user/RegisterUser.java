package br.com.eightbitbazar.application.usecase.user;

import br.com.eightbitbazar.application.port.in.RegisterUserUseCase;
import br.com.eightbitbazar.application.port.out.PasswordEncoder;
import br.com.eightbitbazar.application.port.out.UserRepository;
import br.com.eightbitbazar.domain.exception.BusinessException;
import br.com.eightbitbazar.domain.user.Address;
import br.com.eightbitbazar.domain.user.Role;
import br.com.eightbitbazar.domain.user.User;

import java.time.LocalDateTime;

public class RegisterUser implements RegisterUserUseCase {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public RegisterUser(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public RegisterUserOutput execute(RegisterUserInput input) {
        validateInput(input);

        if (userRepository.existsByEmail(input.email())) {
            throw new BusinessException("Email already registered");
        }

        if (userRepository.existsByNickname(input.nickname())) {
            throw new BusinessException("Nickname already taken");
        }

        String encodedPassword = passwordEncoder.encode(input.password());

        Address address = input.address() != null
            ? new Address(
                input.address().street(),
                input.address().city(),
                input.address().state(),
                input.address().zip()
            )
            : Address.empty();

        User user = new User(
            null,
            input.email(),
            encodedPassword,
            input.nickname(),
            input.fullName(),
            input.phone(),
            input.whatsappLink(),
            Role.USER,
            input.isSeller(),
            address,
            LocalDateTime.now(),
            null
        );

        User savedUser = userRepository.save(user);

        return new RegisterUserOutput(
            savedUser.id().value(),
            savedUser.email(),
            savedUser.nickname(),
            savedUser.fullName(),
            savedUser.isSeller()
        );
    }

    private void validateInput(RegisterUserInput input) {
        if (input.email() == null || input.email().isBlank()) {
            throw new BusinessException("Email is required");
        }
        if (input.password() == null || input.password().length() < 8) {
            throw new BusinessException("Password must be at least 8 characters");
        }
        if (input.nickname() == null || input.nickname().isBlank()) {
            throw new BusinessException("Nickname is required");
        }
        if (input.fullName() == null || input.fullName().isBlank()) {
            throw new BusinessException("Full name is required");
        }
    }
}
