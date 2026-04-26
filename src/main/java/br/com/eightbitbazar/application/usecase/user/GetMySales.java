package br.com.eightbitbazar.application.usecase.user;

import br.com.eightbitbazar.application.port.in.GetMySalesUseCase;
import br.com.eightbitbazar.application.port.out.PurchaseRepository;
import br.com.eightbitbazar.domain.purchase.Purchase;
import br.com.eightbitbazar.domain.user.UserId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

public class GetMySales implements GetMySalesUseCase {

    private final PurchaseRepository purchaseRepository;
    private final UserTradeHistoryMapper tradeHistoryMapper;

    public GetMySales(PurchaseRepository purchaseRepository, UserTradeHistoryMapper tradeHistoryMapper) {
        this.purchaseRepository = purchaseRepository;
        this.tradeHistoryMapper = tradeHistoryMapper;
    }

    @Override
    public Page<UserTradeHistoryItemOutput> execute(UserId userId, int page, int size) {
        Page<Purchase> purchases = purchaseRepository.findBySellerId(
            userId,
            PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"))
        );
        return tradeHistoryMapper.map(purchases);
    }
}

