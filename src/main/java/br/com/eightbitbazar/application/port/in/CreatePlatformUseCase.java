package br.com.eightbitbazar.application.port.in;

import br.com.eightbitbazar.domain.platform.Platform;

public interface CreatePlatformUseCase {

    Platform execute(String name);
}
