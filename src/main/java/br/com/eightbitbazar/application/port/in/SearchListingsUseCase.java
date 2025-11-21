package br.com.eightbitbazar.application.port.in;

import br.com.eightbitbazar.application.usecase.listing.ListingSearchInput;
import br.com.eightbitbazar.application.usecase.listing.ListingSummaryOutput;
import org.springframework.data.domain.Page;

public interface SearchListingsUseCase {

    Page<ListingSummaryOutput> execute(ListingSearchInput input);
}
