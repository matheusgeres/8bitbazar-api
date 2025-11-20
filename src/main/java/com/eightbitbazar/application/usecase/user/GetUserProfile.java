package com.eightbitbazar.application.usecase.user;

import com.eightbitbazar.application.port.in.GetUserProfileUseCase;
import com.eightbitbazar.application.port.out.UserRepository;
import com.eightbitbazar.domain.exception.NotFoundException;
import com.eightbitbazar.domain.user.User;
import com.eightbitbazar.domain.user.UserId;

public class GetUserProfile implements GetUserProfileUseCase {

    private final UserRepository userRepository;

    public GetUserProfile(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserProfileOutput execute(UserId userId) {
        User user = userRepository.findById(userId)
            .filter(u -> !u.isDeleted())
            .orElseThrow(() -> new NotFoundException("User not found"));

        return new UserProfileOutput(
            user.id().value(),
            user.email(),
            user.nickname(),
            user.fullName(),
            user.phone(),
            user.whatsappLink(),
            user.role().name(),
            user.isSeller(),
            new UserProfileOutput.AddressOutput(
                user.address().street(),
                user.address().city(),
                user.address().state(),
                user.address().zip()
            )
        );
    }
}
