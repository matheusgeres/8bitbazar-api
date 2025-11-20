package com.eightbitbazar.application.usecase.listing;

import com.eightbitbazar.application.port.in.GetListingDetailsUseCase;
import com.eightbitbazar.application.port.out.*;
import com.eightbitbazar.domain.bid.Bid;
import com.eightbitbazar.domain.exception.NotFoundException;
import com.eightbitbazar.domain.listing.Listing;
import com.eightbitbazar.domain.listing.ListingId;
import com.eightbitbazar.domain.manufacturer.Manufacturer;
import com.eightbitbazar.domain.platform.Platform;
import com.eightbitbazar.domain.user.User;

import java.util.List;

public class GetListingDetails implements GetListingDetailsUseCase {

    private final ListingRepository listingRepository;
    private final UserRepository userRepository;
    private final PlatformRepository platformRepository;
    private final ManufacturerRepository manufacturerRepository;
    private final BidRepository bidRepository;

    public GetListingDetails(
        ListingRepository listingRepository,
        UserRepository userRepository,
        PlatformRepository platformRepository,
        ManufacturerRepository manufacturerRepository,
        BidRepository bidRepository
    ) {
        this.listingRepository = listingRepository;
        this.userRepository = userRepository;
        this.platformRepository = platformRepository;
        this.manufacturerRepository = manufacturerRepository;
        this.bidRepository = bidRepository;
    }

    @Override
    public ListingDetailsOutput execute(ListingId listingId) {
        Listing listing = listingRepository.findById(listingId)
            .filter(l -> !l.isDeleted())
            .orElseThrow(() -> new NotFoundException("Listing not found"));

        Platform platform = platformRepository.findById(listing.platformId()).orElse(null);
        Manufacturer manufacturer = manufacturerRepository.findById(listing.manufacturerId()).orElse(null);
        User seller = userRepository.findById(listing.sellerId())
            .orElseThrow(() -> new NotFoundException("Seller not found"));

        List<String> imageUrls = listing.images().stream()
            .map(img -> img.url())
            .toList();

        List<Bid> bids = bidRepository.findByListingIdOrderByAmountDesc(listingId);
        List<ListingDetailsOutput.BidInfo> recentBids = bids.stream()
            .limit(10)
            .map(bid -> {
                User bidder = userRepository.findById(bid.userId()).orElse(null);
                return new ListingDetailsOutput.BidInfo(
                    bid.id(),
                    bidder != null ? bidder.nickname() : "Unknown",
                    bid.amount(),
                    bid.createdAt()
                );
            })
            .toList();

        ListingDetailsOutput.SellerInfo sellerInfo = new ListingDetailsOutput.SellerInfo(
            seller.id().value(),
            seller.nickname(),
            seller.whatsappLink()
        );

        return new ListingDetailsOutput(
            listing.id().value(),
            listing.name(),
            listing.description(),
            platform != null ? platform.name() : null,
            manufacturer != null ? manufacturer.name() : null,
            listing.condition().name(),
            listing.quantity(),
            listing.type().name(),
            listing.price(),
            listing.startingPrice(),
            listing.buyNowPrice(),
            listing.auctionEndDate(),
            listing.cashDiscountPercent(),
            listing.calculateCashPrice(),
            listing.status().name(),
            imageUrls,
            sellerInfo,
            recentBids,
            listing.createdAt()
        );
    }
}
