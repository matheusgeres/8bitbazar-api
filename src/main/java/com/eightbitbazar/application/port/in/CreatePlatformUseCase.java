package com.eightbitbazar.application.port.in;

import com.eightbitbazar.domain.platform.Platform;

public interface CreatePlatformUseCase {

    Platform execute(String name);
}
