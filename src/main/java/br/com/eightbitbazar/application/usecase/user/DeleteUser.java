package br.com.eightbitbazar.application.usecase.user;

import br.com.eightbitbazar.application.port.in.DeleteUserUseCase;
import br.com.eightbitbazar.application.port.out.UserRepository;
import br.com.eightbitbazar.domain.exception.NotFoundException;
import br.com.eightbitbazar.domain.user.User;
import br.com.eightbitbazar.domain.user.UserId;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;

@Slf4j
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

        log.atWarn()
            .addKeyValue("userId", userId.value())
            .log("user.deleting");

        User deletedUser = user.withDeletedAt(LocalDateTime.now());
        userRepository.save(deletedUser);

        log.atWarn()
            .addKeyValue("userId", userId.value())
            .log("user.deleted");
    }
}
