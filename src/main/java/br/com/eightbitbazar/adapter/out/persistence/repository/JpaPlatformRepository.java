package br.com.eightbitbazar.adapter.out.persistence.repository;

import br.com.eightbitbazar.adapter.out.persistence.entity.PlatformEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface JpaPlatformRepository extends JpaRepository<PlatformEntity, Long> {

    Optional<PlatformEntity> findByName(String name);

    boolean existsByName(String name);
}
