package br.com.eightbitbazar.adapter.out.persistence;

import br.com.eightbitbazar.adapter.out.persistence.entity.ManufacturerEntity;
import br.com.eightbitbazar.adapter.out.persistence.repository.JpaManufacturerRepository;
import br.com.eightbitbazar.application.port.out.ManufacturerRepository;
import br.com.eightbitbazar.domain.manufacturer.Manufacturer;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class ManufacturerRepositoryAdapter implements ManufacturerRepository {

    private final JpaManufacturerRepository jpaManufacturerRepository;

    public ManufacturerRepositoryAdapter(JpaManufacturerRepository jpaManufacturerRepository) {
        this.jpaManufacturerRepository = jpaManufacturerRepository;
    }

    @Override
    public Manufacturer save(Manufacturer manufacturer) {
        ManufacturerEntity entity = toEntity(manufacturer);
        ManufacturerEntity savedEntity = jpaManufacturerRepository.save(entity);
        return toDomain(savedEntity);
    }

    @Override
    public Optional<Manufacturer> findById(Long id) {
        return jpaManufacturerRepository.findById(id).map(this::toDomain);
    }

    @Override
    public Optional<Manufacturer> findByName(String name) {
        return jpaManufacturerRepository.findByName(name).map(this::toDomain);
    }

    @Override
    public List<Manufacturer> findAll() {
        return jpaManufacturerRepository.findAll().stream().map(this::toDomain).toList();
    }

    @Override
    public boolean existsByName(String name) {
        return jpaManufacturerRepository.existsByName(name);
    }

    private Manufacturer toDomain(ManufacturerEntity entity) {
        return new Manufacturer(entity.getId(), entity.getName(), entity.getCreatedAt());
    }

    private ManufacturerEntity toEntity(Manufacturer domain) {
        ManufacturerEntity entity = new ManufacturerEntity();
        entity.setId(domain.id());
        entity.setName(domain.name());
        entity.setCreatedAt(domain.createdAt());
        return entity;
    }
}
