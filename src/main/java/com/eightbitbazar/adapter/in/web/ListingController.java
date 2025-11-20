package com.eightbitbazar.adapter.in.web;

import com.eightbitbazar.adapter.in.web.dto.CreateListingRequest;
import com.eightbitbazar.adapter.in.web.dto.ListingResponse;
import com.eightbitbazar.application.port.in.*;
import com.eightbitbazar.application.usecase.listing.*;
import com.eightbitbazar.domain.listing.ListingId;
import com.eightbitbazar.domain.user.UserId;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/listings")
public class ListingController {

    private final CreateListingUseCase createListingUseCase;
    private final DeleteListingUseCase deleteListingUseCase;
    private final SearchListingsUseCase searchListingsUseCase;
    private final GetListingDetailsUseCase getListingDetailsUseCase;
    private final UploadListingImageUseCase uploadListingImageUseCase;

    public ListingController(
        CreateListingUseCase createListingUseCase,
        DeleteListingUseCase deleteListingUseCase,
        SearchListingsUseCase searchListingsUseCase,
        GetListingDetailsUseCase getListingDetailsUseCase,
        UploadListingImageUseCase uploadListingImageUseCase
    ) {
        this.createListingUseCase = createListingUseCase;
        this.deleteListingUseCase = deleteListingUseCase;
        this.searchListingsUseCase = searchListingsUseCase;
        this.getListingDetailsUseCase = getListingDetailsUseCase;
        this.uploadListingImageUseCase = uploadListingImageUseCase;
    }

    @GetMapping
    public ResponseEntity<Page<ListingSummaryOutput>> searchListings(
        @RequestParam(required = false) String search,
        @RequestParam(required = false) String type,
        @RequestParam(required = false) Long platformId,
        @RequestParam(required = false) Long manufacturerId,
        @RequestParam(defaultValue = "recent") String sort,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "20") int size
    ) {
        ListingSearchInput input = new ListingSearchInput(
            search, type, platformId, manufacturerId, sort, page, size
        );
        Page<ListingSummaryOutput> result = searchListingsUseCase.execute(input);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ListingDetailsOutput> getListingDetails(@PathVariable Long id) {
        ListingDetailsOutput output = getListingDetailsUseCase.execute(new ListingId(id));
        return ResponseEntity.ok(output);
    }

    @PostMapping
    public ResponseEntity<ListingResponse> createListing(
        @AuthenticationPrincipal Jwt jwt,
        @Valid @RequestBody CreateListingRequest request
    ) {
        UserId sellerId = new UserId(Long.parseLong(jwt.getSubject()));

        CreateListingInput input = new CreateListingInput(
            request.name(),
            request.description(),
            request.platformId(),
            request.manufacturerId(),
            request.condition(),
            request.quantity(),
            request.type(),
            request.price(),
            request.startingPrice(),
            request.buyNowPrice(),
            request.auctionEndDate(),
            request.cashDiscountPercent()
        );

        CreateListingOutput output = createListingUseCase.execute(sellerId, input);

        ListingResponse response = new ListingResponse(
            output.id(),
            output.name(),
            output.description(),
            output.platform(),
            output.manufacturer(),
            output.condition(),
            output.quantity(),
            output.type(),
            output.price(),
            output.startingPrice(),
            output.buyNowPrice(),
            output.auctionEndDate(),
            output.cashDiscountPercent(),
            output.status(),
            List.of(),
            output.createdAt()
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteListing(
        @AuthenticationPrincipal Jwt jwt,
        @PathVariable Long id
    ) {
        UserId sellerId = new UserId(Long.parseLong(jwt.getSubject()));
        deleteListingUseCase.execute(sellerId, new ListingId(id));
        return ResponseEntity.noContent().build();
    }

    @PostMapping(value = "/{id}/images", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<List<String>> uploadImages(
        @AuthenticationPrincipal Jwt jwt,
        @PathVariable Long id,
        @RequestParam("files") List<MultipartFile> files
    ) throws IOException {
        UserId userId = new UserId(Long.parseLong(jwt.getSubject()));
        ListingId listingId = new ListingId(id);

        List<UploadListingImageUseCase.ImageUpload> images = new ArrayList<>();
        for (MultipartFile file : files) {
            images.add(new UploadListingImageUseCase.ImageUpload(
                file.getOriginalFilename(),
                file.getInputStream(),
                file.getContentType(),
                file.getSize()
            ));
        }

        List<String> urls = uploadListingImageUseCase.execute(userId, listingId, images);
        return ResponseEntity.status(HttpStatus.CREATED).body(urls);
    }
}
