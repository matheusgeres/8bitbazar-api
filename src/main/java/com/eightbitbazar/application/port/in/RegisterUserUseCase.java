package com.eightbitbazar.application.port.in;

import com.eightbitbazar.application.usecase.user.RegisterUserInput;
import com.eightbitbazar.application.usecase.user.RegisterUserOutput;

public interface RegisterUserUseCase {

    RegisterUserOutput execute(RegisterUserInput input);
}
