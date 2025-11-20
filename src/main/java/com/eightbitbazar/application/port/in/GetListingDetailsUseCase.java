package com.eightbitbazar.application.port.in;

import com.eightbitbazar.application.usecase.listing.ListingDetailsOutput;
import com.eightbitbazar.domain.listing.ListingId;

public interface GetListingDetailsUseCase {

    ListingDetailsOutput execute(ListingId listingId);
}
