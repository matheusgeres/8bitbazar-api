package br.com.eightbitbazar.adapter.in.scheduling;

import br.com.eightbitbazar.application.port.in.CloseExpiredAuctionsUseCase;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class AuctionClosingSchedulerTest {

    @Test
    void shouldDelegateScheduledRunToCloseExpiredAuctionsUseCase() {
        CloseExpiredAuctionsUseCase closeExpiredAuctionsUseCase = mock(CloseExpiredAuctionsUseCase.class);
        AuctionClosingScheduler scheduler = new AuctionClosingScheduler(closeExpiredAuctionsUseCase);

        scheduler.closeExpiredAuctions();

        verify(closeExpiredAuctionsUseCase).execute();
    }
}
