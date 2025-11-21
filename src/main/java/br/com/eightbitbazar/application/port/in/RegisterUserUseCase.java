package br.com.eightbitbazar.application.port.in;

import br.com.eightbitbazar.application.usecase.user.RegisterUserInput;
import br.com.eightbitbazar.application.usecase.user.RegisterUserOutput;

public interface RegisterUserUseCase {

    RegisterUserOutput execute(RegisterUserInput input);
}
