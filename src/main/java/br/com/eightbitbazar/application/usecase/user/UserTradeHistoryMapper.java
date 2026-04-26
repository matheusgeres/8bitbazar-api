package br.com.eightbitbazar.application.usecase.user;

import br.com.eightbitbazar.application.port.out.ListingRepository;
import br.com.eightbitbazar.domain.listing.Listing;
import br.com.eightbitbazar.domain.listing.ListingId;
import br.com.eightbitbazar.domain.purchase.Purchase;
import org.springframework.data.domain.Page;

import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

public class UserTradeHistoryMapper {

    private final ListingRepository listingRepository;

    public UserTradeHistoryMapper(ListingRepository listingRepository) {
        this.listingRepository = listingRepository;
    }

    public Page<UserTradeHistoryItemOutput> map(Page<Purchase> purchases) {
        Map<ListingId, Listing> listingsById = loadListings(purchases);
        return purchases.map(purchase -> toOutput(purchase, listingsById.get(purchase.listingId())));
    }

    private Map<ListingId, Listing> loadListings(Page<Purchase> purchases) {
        Set<ListingId> listingIds = purchases.getContent().stream()
            .map(Purchase::listingId)
            .collect(Collectors.toSet());

        return listingRepository.findByIds(listingIds).stream()
            .collect(Collectors.toMap(Listing::id, Function.identity()));
    }

    private UserTradeHistoryItemOutput toOutput(Purchase purchase, Listing listing) {
        return new UserTradeHistoryItemOutput(
            purchase.id(),
            purchase.listingId().value(),
            listing != null ? listing.name() : null,
            listing != null ? listing.type().name() : null,
            listing != null ? listing.status().name() : null,
            purchase.amount(),
            purchase.finalAmount(),
            purchase.paymentMethod() != null ? purchase.paymentMethod().name() : null,
            purchase.status().name(),
            purchase.createdAt()
        );
    }
}
