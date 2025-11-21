package br.com.eightbitbazar.application.port.in;

import br.com.eightbitbazar.application.usecase.user.UpdateUserProfileInput;
import br.com.eightbitbazar.application.usecase.user.UpdateUserProfileOutput;
import br.com.eightbitbazar.domain.user.UserId;

public interface UpdateUserProfileUseCase {

    UpdateUserProfileOutput execute(UserId userId, UpdateUserProfileInput input);
}
