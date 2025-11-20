package com.eightbitbazar.application.port.out;

import com.eightbitbazar.domain.bid.Bid;
import com.eightbitbazar.domain.listing.ListingId;

import java.util.List;
import java.util.Optional;

public interface BidRepository {

    Bid save(Bid bid);

    List<Bid> findByListingIdOrderByAmountDesc(ListingId listingId);

    Optional<Bid> findHighestBidByListingId(ListingId listingId);
}
