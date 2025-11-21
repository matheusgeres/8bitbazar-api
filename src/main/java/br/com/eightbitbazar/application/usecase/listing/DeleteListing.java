package br.com.eightbitbazar.application.usecase.listing;

import br.com.eightbitbazar.application.port.in.DeleteListingUseCase;
import br.com.eightbitbazar.application.port.out.EventPublisher;
import br.com.eightbitbazar.application.port.out.ListingRepository;
import br.com.eightbitbazar.domain.event.ListingDeletedEvent;
import br.com.eightbitbazar.domain.exception.BusinessException;
import br.com.eightbitbazar.domain.exception.NotFoundException;
import br.com.eightbitbazar.domain.listing.Listing;
import br.com.eightbitbazar.domain.listing.ListingId;
import br.com.eightbitbazar.domain.listing.ListingStatus;
import br.com.eightbitbazar.domain.user.UserId;

import java.time.LocalDateTime;

public class DeleteListing implements DeleteListingUseCase {

    private final ListingRepository listingRepository;
    private final EventPublisher eventPublisher;

    public DeleteListing(ListingRepository listingRepository, EventPublisher eventPublisher) {
        this.listingRepository = listingRepository;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public void execute(UserId sellerId, ListingId listingId) {
        Listing listing = listingRepository.findById(listingId)
            .filter(l -> !l.isDeleted())
            .orElseThrow(() -> new NotFoundException("Listing not found"));

        if (!listing.sellerId().equals(sellerId)) {
            throw new BusinessException("You can only delete your own listings");
        }

        Listing deletedListing = listing
            .withStatus(ListingStatus.DELETED)
            .withDeletedAt(LocalDateTime.now());

        listingRepository.save(deletedListing);

        eventPublisher.publish(new ListingDeletedEvent(
            listingId.value(),
            sellerId.value()
        ));
    }
}
