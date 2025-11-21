package br.com.eightbitbazar.application.usecase.listing;

import br.com.eightbitbazar.application.port.in.SearchListingsUseCase;
import br.com.eightbitbazar.application.port.out.ListingRepository;
import br.com.eightbitbazar.application.port.out.ManufacturerRepository;
import br.com.eightbitbazar.application.port.out.PlatformRepository;
import br.com.eightbitbazar.application.port.out.UserRepository;
import br.com.eightbitbazar.domain.listing.Listing;
import br.com.eightbitbazar.domain.manufacturer.Manufacturer;
import br.com.eightbitbazar.domain.platform.Platform;
import br.com.eightbitbazar.domain.user.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.List;

public class SearchListings implements SearchListingsUseCase {

    private final ListingRepository listingRepository;
    private final UserRepository userRepository;
    private final PlatformRepository platformRepository;
    private final ManufacturerRepository manufacturerRepository;

    public SearchListings(
        ListingRepository listingRepository,
        UserRepository userRepository,
        PlatformRepository platformRepository,
        ManufacturerRepository manufacturerRepository
    ) {
        this.listingRepository = listingRepository;
        this.userRepository = userRepository;
        this.platformRepository = platformRepository;
        this.manufacturerRepository = manufacturerRepository;
    }

    @Override
    public Page<ListingSummaryOutput> execute(ListingSearchInput input) {
        Sort sort = parseSort(input.sort());
        Pageable pageable = PageRequest.of(input.page(), input.size(), sort);

        ListingRepository.ListingSearchCriteria criteria = new ListingRepository.ListingSearchCriteria(
            input.search(),
            input.type(),
            input.platformId(),
            input.manufacturerId()
        );

        Page<Listing> listings = listingRepository.findAll(criteria, pageable);

        return listings.map(this::toSummaryOutput);
    }

    private ListingSummaryOutput toSummaryOutput(Listing listing) {
        Platform platform = platformRepository.findById(listing.platformId()).orElse(null);
        Manufacturer manufacturer = manufacturerRepository.findById(listing.manufacturerId()).orElse(null);
        User seller = userRepository.findById(listing.sellerId()).orElse(null);

        List<String> imageUrls = listing.images().stream()
            .map(img -> img.url())
            .toList();

        return new ListingSummaryOutput(
            listing.id().value(),
            listing.name(),
            platform != null ? platform.name() : null,
            manufacturer != null ? manufacturer.name() : null,
            listing.condition().name(),
            listing.type().name(),
            listing.price(),
            listing.startingPrice(),
            listing.auctionEndDate(),
            listing.cashDiscountPercent(),
            listing.status().name(),
            imageUrls,
            seller != null ? seller.nickname() : null,
            listing.createdAt()
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
