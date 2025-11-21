package br.com.eightbitbazar.application.port.out;

import br.com.eightbitbazar.domain.listing.ListingId;
import br.com.eightbitbazar.domain.listing.ListingImage;

import java.util.List;

public interface ListingImageRepository {

    ListingImage save(ListingImage image);

    List<ListingImage> findByListingId(ListingId listingId);

    void deleteByListingId(ListingId listingId);
}
