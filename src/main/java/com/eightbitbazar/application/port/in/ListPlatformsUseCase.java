package com.eightbitbazar.application.port.in;

import com.eightbitbazar.domain.platform.Platform;

import java.util.List;

public interface ListPlatformsUseCase {

    List<Platform> execute();
}
