package com.eightbitbazar.application.port.in;

import com.eightbitbazar.domain.listing.ListingId;
import com.eightbitbazar.domain.user.UserId;

public interface DeleteListingUseCase {

    void execute(UserId sellerId, ListingId listingId);
}
