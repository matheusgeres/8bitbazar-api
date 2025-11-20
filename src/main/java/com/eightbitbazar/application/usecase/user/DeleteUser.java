package com.eightbitbazar.application.usecase.user;

import com.eightbitbazar.application.port.in.DeleteUserUseCase;
import com.eightbitbazar.application.port.out.UserRepository;
import com.eightbitbazar.domain.exception.NotFoundException;
import com.eightbitbazar.domain.user.User;
import com.eightbitbazar.domain.user.UserId;

import java.time.LocalDateTime;

public class DeleteUser implements DeleteUserUseCase {

    private final UserRepository userRepository;

    public DeleteUser(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public void execute(UserId userId) {
        User user = userRepository.findById(userId)
            .filter(u -> !u.isDeleted())
            .orElseThrow(() -> new NotFoundException("User not found"));

        User deletedUser = user.withDeletedAt(LocalDateTime.now());
        userRepository.save(deletedUser);
    }
}
