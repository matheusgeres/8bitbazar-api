package br.com.eightbitbazar.application.port.in;

import br.com.eightbitbazar.domain.listing.ListingId;
import br.com.eightbitbazar.domain.user.UserId;

public interface DeleteListingUseCase {

    void execute(UserId sellerId, ListingId listingId);
}
