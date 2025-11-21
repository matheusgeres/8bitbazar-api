package br.com.eightbitbazar.application.port.out;

import br.com.eightbitbazar.domain.platform.Platform;

import java.util.List;
import java.util.Optional;

public interface PlatformRepository {

    Platform save(Platform platform);

    Optional<Platform> findById(Long id);

    Optional<Platform> findByName(String name);

    List<Platform> findAll();

    boolean existsByName(String name);
}
