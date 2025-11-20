package com.eightbitbazar.application.port.in;

import com.eightbitbazar.application.usecase.bid.PlaceBidInput;
import com.eightbitbazar.application.usecase.bid.PlaceBidOutput;
import com.eightbitbazar.domain.user.UserId;

public interface PlaceBidUseCase {

    PlaceBidOutput execute(UserId userId, PlaceBidInput input);
}
