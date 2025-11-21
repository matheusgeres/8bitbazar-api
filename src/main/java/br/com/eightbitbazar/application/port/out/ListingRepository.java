package br.com.eightbitbazar.application.port.out;

import br.com.eightbitbazar.domain.listing.Listing;
import br.com.eightbitbazar.domain.listing.ListingId;
import br.com.eightbitbazar.domain.user.UserId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface ListingRepository {

    Listing save(Listing listing);

    Optional<Listing> findById(ListingId id);

    Page<Listing> findAll(ListingSearchCriteria criteria, Pageable pageable);

    Page<Listing> findBySellerId(UserId sellerId, Pageable pageable);

    record ListingSearchCriteria(
        String search,
        String type,
        Long platformId,
        Long manufacturerId
    ) {}
}
