package br.com.eightbitbazar.application.port.in;

import br.com.eightbitbazar.application.usecase.user.UserTradeHistoryItemOutput;
import br.com.eightbitbazar.domain.user.UserId;
import org.springframework.data.domain.Page;

public interface GetMySalesUseCase {

    Page<UserTradeHistoryItemOutput> execute(UserId userId, int page, int size);
}
