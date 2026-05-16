package br.com.eightbitbazar.adapter.in.scheduling;

import br.com.eightbitbazar.application.port.in.CloseExpiredAuctionsUseCase;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@ConditionalOnProperty(prefix = "app.auctions.closing", name = "enabled", havingValue = "true")
public class AuctionClosingScheduler {

    private final CloseExpiredAuctionsUseCase closeExpiredAuctionsUseCase;

    public AuctionClosingScheduler(CloseExpiredAuctionsUseCase closeExpiredAuctionsUseCase) {
        this.closeExpiredAuctionsUseCase = closeExpiredAuctionsUseCase;
    }

    @Scheduled(fixedDelayString = "${app.auctions.closing.fixed-delay:60000}")
    public void closeExpiredAuctions() {
        log.info("scheduler.auction_closing.started");
        try {
            int processed = closeExpiredAuctionsUseCase.execute();
            log.atInfo()
                .addKeyValue("processed", processed)
                .log("scheduler.auction_closing.finished");
        } catch (Exception e) {
            log.atError()
                .addKeyValue("error", e.getMessage() != null ? e.getMessage() : e.toString())
                .setCause(e)
                .log("scheduler.auction_closing.failed");
            throw e;
        }
    }
}
