package br.com.eightbitbazar.application.port.in;

import br.com.eightbitbazar.application.usecase.listing.ListingDetailsOutput;
import br.com.eightbitbazar.domain.listing.ListingId;

public interface GetListingDetailsUseCase {

    ListingDetailsOutput execute(ListingId listingId);
}
