package br.com.eightbitbazar.application.port.in;

import br.com.eightbitbazar.application.usecase.bid.PlaceBidInput;
import br.com.eightbitbazar.application.usecase.bid.PlaceBidOutput;
import br.com.eightbitbazar.domain.user.UserId;

public interface PlaceBidUseCase {

    PlaceBidOutput execute(UserId userId, PlaceBidInput input);
}
