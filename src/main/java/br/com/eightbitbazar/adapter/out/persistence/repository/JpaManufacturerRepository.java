package br.com.eightbitbazar.adapter.out.persistence.repository;

import br.com.eightbitbazar.adapter.out.persistence.entity.ManufacturerEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface JpaManufacturerRepository extends JpaRepository<ManufacturerEntity, Long> {

    Optional<ManufacturerEntity> findByName(String name);

    boolean existsByName(String name);
}
