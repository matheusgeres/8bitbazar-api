package com.eightbitbazar.application.port.in;

import com.eightbitbazar.application.usecase.purchase.DirectPurchaseInput;
import com.eightbitbazar.application.usecase.purchase.DirectPurchaseOutput;
import com.eightbitbazar.domain.user.UserId;

public interface DirectPurchaseUseCase {

    DirectPurchaseOutput execute(UserId buyerId, DirectPurchaseInput input);
}
