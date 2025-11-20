package com.eightbitbazar.application.usecase.listing;

import com.eightbitbazar.application.port.in.SearchListingsUseCase;
import com.eightbitbazar.application.port.out.ListingImageRepository;
import com.eightbitbazar.application.port.out.ListingSearchRepository;
import com.eightbitbazar.application.port.out.ListingSearchRepository.ListingSearchResult;
import com.eightbitbazar.application.port.out.ListingSearchRepository.SearchCriteria;
import com.eightbitbazar.domain.listing.ListingId;
import com.eightbitbazar.domain.listing.ListingImage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.List;

public class SearchListingsWithElasticsearch implements SearchListingsUseCase {

    private final ListingSearchRepository listingSearchRepository;
    private final ListingImageRepository listingImageRepository;

    public SearchListingsWithElasticsearch(
        ListingSearchRepository listingSearchRepository,
        ListingImageRepository listingImageRepository
    ) {
        this.listingSearchRepository = listingSearchRepository;
        this.listingImageRepository = listingImageRepository;
    }

    @Override
    public Page<ListingSummaryOutput> execute(ListingSearchInput input) {
        Sort sort = parseSort(input.sort());
        Pageable pageable = PageRequest.of(input.page(), input.size(), sort);

        SearchCriteria criteria = new SearchCriteria(
            input.search(),
            input.type(),
            input.platformId(),
            input.manufacturerId()
        );

        Page<ListingSearchResult> results = listingSearchRepository.search(criteria, pageable);

        return results.map(this::toSummaryOutput);
    }

    private ListingSummaryOutput toSummaryOutput(ListingSearchResult result) {
        List<String> imageUrls = listingImageRepository.findByListingId(new ListingId(result.id()))
            .stream()
            .map(ListingImage::url)
            .toList();

        return new ListingSummaryOutput(
            result.id(),
            result.name(),
            result.platformName(),
            result.manufacturerName(),
            result.condition(),
            result.type(),
            result.price(),
            result.startingPrice(),
            result.auctionEndDate(),
            result.cashDiscountPercent(),
            result.status(),
            imageUrls,
            result.sellerNickname(),
            result.createdAt()
        );
    }

    private Sort parseSort(String sort) {
        if (sort == null) {
            return Sort.by(Sort.Direction.DESC, "createdAt");
        }
        return switch (sort) {
            case "price_asc" -> Sort.by(Sort.Direction.ASC, "price");
            case "price_desc" -> Sort.by(Sort.Direction.DESC, "price");
            case "ending_soon" -> Sort.by(Sort.Direction.ASC, "auctionEndDate");
            default -> Sort.by(Sort.Direction.DESC, "createdAt");
        };
    }
}
