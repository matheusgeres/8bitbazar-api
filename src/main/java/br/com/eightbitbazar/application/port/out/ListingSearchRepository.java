package br.com.eightbitbazar.application.port.out;

import br.com.eightbitbazar.domain.listing.Listing;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public interface ListingSearchRepository {

    Page<ListingSearchResult> search(SearchCriteria criteria, Pageable pageable);

    void index(ListingSearchResult listing);

    void delete(Long listingId);

    record SearchCriteria(
        String query,
        String type,
        Long platformId,
        Long manufacturerId
    ) {}

    record ListingSearchResult(
        Long id,
        String name,
        String description,
        Long platformId,
        String platformName,
        Long manufacturerId,
        String manufacturerName,
        String condition,
        Integer quantity,
        String type,
        BigDecimal price,
        BigDecimal startingPrice,
        BigDecimal buyNowPrice,
        Instant auctionEndDate,
        BigDecimal cashDiscountPercent,
        String status,
        Long sellerId,
        String sellerNickname,
        List<String> imageUrls,
        Instant createdAt
    ) {}
}
