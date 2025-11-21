package br.com.eightbitbazar.adapter.out.persistence.mapper;

import br.com.eightbitbazar.adapter.out.persistence.entity.UserEntity;
import br.com.eightbitbazar.domain.user.Address;
import br.com.eightbitbazar.domain.user.Role;
import br.com.eightbitbazar.domain.user.User;
import br.com.eightbitbazar.domain.user.UserId;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

    public User toDomain(UserEntity entity) {
        if (entity == null) {
            return null;
        }

        return new User(
            entity.getId() != null ? new UserId(entity.getId()) : null,
            entity.getEmail(),
            entity.getPassword(),
            entity.getNickname(),
            entity.getFullName(),
            entity.getPhone(),
            entity.getWhatsappLink(),
            Role.valueOf(entity.getRole()),
            entity.isSeller(),
            new Address(
                entity.getAddressStreet(),
                entity.getAddressCity(),
                entity.getAddressState(),
                entity.getAddressZip()
            ),
            entity.getCreatedAt(),
            entity.getDeletedAt()
        );
    }

    public UserEntity toEntity(User domain) {
        if (domain == null) {
            return null;
        }

        UserEntity entity = new UserEntity();
        if (domain.id() != null) {
            entity.setId(domain.id().value());
        }
        entity.setEmail(domain.email());
        entity.setPassword(domain.password());
        entity.setNickname(domain.nickname());
        entity.setFullName(domain.fullName());
        entity.setPhone(domain.phone());
        entity.setWhatsappLink(domain.whatsappLink());
        entity.setRole(domain.role().name());
        entity.setSeller(domain.isSeller());
        entity.setAddressStreet(domain.address().street());
        entity.setAddressCity(domain.address().city());
        entity.setAddressState(domain.address().state());
        entity.setAddressZip(domain.address().zip());
        entity.setCreatedAt(domain.createdAt());
        entity.setDeletedAt(domain.deletedAt());

        return entity;
    }
}
