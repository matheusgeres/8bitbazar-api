package br.com.eightbitbazar.application.port.in;

import br.com.eightbitbazar.domain.platform.Platform;

import java.util.List;

public interface ListPlatformsUseCase {

    List<Platform> execute();
}
