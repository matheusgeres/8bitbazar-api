package br.com.eightbitbazar.application.port.in;

import br.com.eightbitbazar.application.usecase.user.UserProfileOutput;
import br.com.eightbitbazar.domain.user.UserId;

public interface GetUserProfileUseCase {

    UserProfileOutput execute(UserId userId);
}
