package br.com.eightbitbazar.application.usecase.admin;

import br.com.eightbitbazar.application.port.in.ListPlatformsUseCase;
import br.com.eightbitbazar.application.port.out.PlatformRepository;
import br.com.eightbitbazar.domain.platform.Platform;

import java.util.List;

public class ListPlatforms implements ListPlatformsUseCase {

    private final PlatformRepository platformRepository;

    public ListPlatforms(PlatformRepository platformRepository) {
        this.platformRepository = platformRepository;
    }

    @Override
    public List<Platform> execute() {
        return platformRepository.findAll();
    }
}
