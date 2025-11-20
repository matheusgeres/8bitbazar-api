package com.eightbitbazar.application.port.in;

import com.eightbitbazar.application.usecase.user.UserProfileOutput;
import com.eightbitbazar.domain.user.UserId;

public interface GetUserProfileUseCase {

    UserProfileOutput execute(UserId userId);
}
