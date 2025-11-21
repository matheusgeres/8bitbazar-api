package br.com.eightbitbazar.application.port.in;

import br.com.eightbitbazar.domain.user.UserId;

public interface DeleteUserUseCase {

    void execute(UserId userId);
}
