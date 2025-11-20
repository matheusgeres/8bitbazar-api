package com.eightbitbazar.application.port.out;

import com.eightbitbazar.domain.user.User;
import com.eightbitbazar.domain.user.UserId;

import java.util.Optional;

public interface UserRepository {

    User save(User user);

    Optional<User> findById(UserId id);

    Optional<User> findByEmail(String email);

    Optional<User> findByNickname(String nickname);

    boolean existsByEmail(String email);

    boolean existsByNickname(String nickname);
}
