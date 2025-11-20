package com.eightbitbazar.application.port.out;

import com.eightbitbazar.domain.listing.ListingId;
import com.eightbitbazar.domain.listing.ListingImage;

import java.util.List;

public interface ListingImageRepository {

    ListingImage save(ListingImage image);

    List<ListingImage> findByListingId(ListingId listingId);

    void deleteByListingId(ListingId listingId);
}
