package com.eightbitbazar.application.port.in;

import com.eightbitbazar.application.usecase.user.UpdateUserProfileInput;
import com.eightbitbazar.application.usecase.user.UpdateUserProfileOutput;
import com.eightbitbazar.domain.user.UserId;

public interface UpdateUserProfileUseCase {

    UpdateUserProfileOutput execute(UserId userId, UpdateUserProfileInput input);
}
