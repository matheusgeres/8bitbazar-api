package br.com.eightbitbazar.application.port.out;

import br.com.eightbitbazar.domain.user.User;
import br.com.eightbitbazar.domain.user.UserId;

import java.util.Optional;

public interface UserRepository {

    User save(User user);

    Optional<User> findById(UserId id);

    Optional<User> findByEmail(String email);

    Optional<User> findByNickname(String nickname);

    boolean existsByEmail(String email);

    boolean existsByNickname(String nickname);

    void updatePassword(UserId id, String newPassword);
}
