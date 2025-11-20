package com.eightbitbazar.adapter.out.persistence;

import com.eightbitbazar.adapter.out.persistence.entity.UserEntity;
import com.eightbitbazar.adapter.out.persistence.mapper.UserMapper;
import com.eightbitbazar.adapter.out.persistence.repository.JpaUserRepository;
import com.eightbitbazar.application.port.out.UserRepository;
import com.eightbitbazar.domain.user.User;
import com.eightbitbazar.domain.user.UserId;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class UserRepositoryAdapter implements UserRepository {

    private final JpaUserRepository jpaUserRepository;
    private final UserMapper userMapper;

    public UserRepositoryAdapter(JpaUserRepository jpaUserRepository, UserMapper userMapper) {
        this.jpaUserRepository = jpaUserRepository;
        this.userMapper = userMapper;
    }

    @Override
    public User save(User user) {
        UserEntity entity = userMapper.toEntity(user);
        UserEntity savedEntity = jpaUserRepository.save(entity);
        return userMapper.toDomain(savedEntity);
    }

    @Override
    public Optional<User> findById(UserId id) {
        return jpaUserRepository.findById(id.value())
            .map(userMapper::toDomain);
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return jpaUserRepository.findByEmail(email)
            .map(userMapper::toDomain);
    }

    @Override
    public Optional<User> findByNickname(String nickname) {
        return jpaUserRepository.findByNickname(nickname)
            .map(userMapper::toDomain);
    }

    @Override
    public boolean existsByEmail(String email) {
        return jpaUserRepository.existsByEmail(email);
    }

    @Override
    public boolean existsByNickname(String nickname) {
        return jpaUserRepository.existsByNickname(nickname);
    }
}
