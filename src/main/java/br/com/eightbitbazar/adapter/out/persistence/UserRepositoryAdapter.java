package br.com.eightbitbazar.adapter.out.persistence;

import br.com.eightbitbazar.adapter.out.persistence.entity.UserEntity;
import br.com.eightbitbazar.adapter.out.persistence.mapper.UserMapper;
import br.com.eightbitbazar.adapter.out.persistence.repository.JpaUserRepository;
import br.com.eightbitbazar.application.port.out.UserRepository;
import br.com.eightbitbazar.domain.user.User;
import br.com.eightbitbazar.domain.user.UserId;
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

    @Override
    public void updatePassword(UserId id, String newPassword) {
        jpaUserRepository.updatePassword(id.value(), newPassword);
    }
}
