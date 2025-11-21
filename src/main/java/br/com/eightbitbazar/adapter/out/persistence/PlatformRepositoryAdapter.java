package br.com.eightbitbazar.adapter.out.persistence;

import br.com.eightbitbazar.adapter.out.persistence.entity.PlatformEntity;
import br.com.eightbitbazar.adapter.out.persistence.repository.JpaPlatformRepository;
import br.com.eightbitbazar.application.port.out.PlatformRepository;
import br.com.eightbitbazar.domain.platform.Platform;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class PlatformRepositoryAdapter implements PlatformRepository {

    private final JpaPlatformRepository jpaPlatformRepository;

    public PlatformRepositoryAdapter(JpaPlatformRepository jpaPlatformRepository) {
        this.jpaPlatformRepository = jpaPlatformRepository;
    }

    @Override
    public Platform save(Platform platform) {
        PlatformEntity entity = toEntity(platform);
        PlatformEntity savedEntity = jpaPlatformRepository.save(entity);
        return toDomain(savedEntity);
    }

    @Override
    public Optional<Platform> findById(Long id) {
        return jpaPlatformRepository.findById(id).map(this::toDomain);
    }

    @Override
    public Optional<Platform> findByName(String name) {
        return jpaPlatformRepository.findByName(name).map(this::toDomain);
    }

    @Override
    public List<Platform> findAll() {
        return jpaPlatformRepository.findAll().stream().map(this::toDomain).toList();
    }

    @Override
    public boolean existsByName(String name) {
        return jpaPlatformRepository.existsByName(name);
    }

    private Platform toDomain(PlatformEntity entity) {
        return new Platform(entity.getId(), entity.getName(), entity.getCreatedAt());
    }

    private PlatformEntity toEntity(Platform domain) {
        PlatformEntity entity = new PlatformEntity();
        entity.setId(domain.id());
        entity.setName(domain.name());
        entity.setCreatedAt(domain.createdAt());
        return entity;
    }
}
