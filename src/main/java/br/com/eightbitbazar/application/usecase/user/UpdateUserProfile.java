package br.com.eightbitbazar.application.usecase.user;

import br.com.eightbitbazar.application.port.in.UpdateUserProfileUseCase;
import br.com.eightbitbazar.application.port.out.UserRepository;
import br.com.eightbitbazar.domain.exception.NotFoundException;
import br.com.eightbitbazar.domain.user.Address;
import br.com.eightbitbazar.domain.user.User;
import br.com.eightbitbazar.domain.user.UserId;

public class UpdateUserProfile implements UpdateUserProfileUseCase {

    private final UserRepository userRepository;

    public UpdateUserProfile(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UpdateUserProfileOutput execute(UserId userId, UpdateUserProfileInput input) {
        User user = userRepository.findById(userId)
            .filter(u -> !u.isDeleted())
            .orElseThrow(() -> new NotFoundException("User not found"));

        Address address = input.address() != null
            ? new Address(
                input.address().street(),
                input.address().city(),
                input.address().state(),
                input.address().zip()
            )
            : user.address();

        User updatedUser = new User(
            user.id(),
            user.email(),
            user.password(),
            user.nickname(),
            input.fullName() != null ? input.fullName() : user.fullName(),
            input.phone(),
            input.whatsappLink(),
            user.role(),
            input.isSeller(),
            address,
            user.createdAt(),
            user.deletedAt()
        );

        User savedUser = userRepository.save(updatedUser);

        return new UpdateUserProfileOutput(
            savedUser.id().value(),
            savedUser.email(),
            savedUser.nickname(),
            savedUser.fullName(),
            savedUser.phone(),
            savedUser.whatsappLink(),
            savedUser.isSeller(),
            new UpdateUserProfileOutput.AddressOutput(
                savedUser.address().street(),
                savedUser.address().city(),
                savedUser.address().state(),
                savedUser.address().zip()
            )
        );
    }
}
