package br.com.eightbitbazar.adapter.in.scheduling;

import br.com.eightbitbazar.application.port.in.CloseExpiredAuctionsUseCase;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class AuctionClosingScheduler {

    private final CloseExpiredAuctionsUseCase closeExpiredAuctionsUseCase;

    public AuctionClosingScheduler(CloseExpiredAuctionsUseCase closeExpiredAuctionsUseCase) {
        this.closeExpiredAuctionsUseCase = closeExpiredAuctionsUseCase;
    }

    @Scheduled(fixedDelayString = "${app.auctions.closing.fixed-delay:60000}")
    public void closeExpiredAuctions() {
        closeExpiredAuctionsUseCase.execute();
    }
}
