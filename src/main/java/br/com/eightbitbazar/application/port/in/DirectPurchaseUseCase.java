package br.com.eightbitbazar.application.port.in;

import br.com.eightbitbazar.application.usecase.purchase.DirectPurchaseInput;
import br.com.eightbitbazar.application.usecase.purchase.DirectPurchaseOutput;
import br.com.eightbitbazar.domain.user.UserId;

public interface DirectPurchaseUseCase {

    DirectPurchaseOutput execute(UserId buyerId, DirectPurchaseInput input);
}
