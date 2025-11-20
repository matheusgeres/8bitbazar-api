package com.eightbitbazar.application.port.in;

import com.eightbitbazar.domain.user.UserId;

public interface DeleteUserUseCase {

    void execute(UserId userId);
}
