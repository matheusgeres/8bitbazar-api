package com.eightbitbazar.application.usecase.admin;

import com.eightbitbazar.application.port.in.CreatePlatformUseCase;
import com.eightbitbazar.application.port.out.PlatformRepository;
import com.eightbitbazar.domain.exception.BusinessException;
import com.eightbitbazar.domain.platform.Platform;

import java.time.LocalDateTime;

public class CreatePlatform implements CreatePlatformUseCase {

    private final PlatformRepository platformRepository;

    public CreatePlatform(PlatformRepository platformRepository) {
        this.platformRepository = platformRepository;
    }

    @Override
    public Platform execute(String name) {
        if (name == null || name.isBlank()) {
            throw new BusinessException("Platform name is required");
        }

        if (platformRepository.existsByName(name)) {
            throw new BusinessException("Platform already exists");
        }

        Platform platform = new Platform(null, name, LocalDateTime.now());
        return platformRepository.save(platform);
    }
}
