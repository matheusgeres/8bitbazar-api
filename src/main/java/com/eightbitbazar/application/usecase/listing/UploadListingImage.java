package com.eightbitbazar.application.usecase.listing;

import com.eightbitbazar.application.port.in.UploadListingImageUseCase;
import com.eightbitbazar.application.port.out.ImageStorage;
import com.eightbitbazar.application.port.out.ListingImageRepository;
import com.eightbitbazar.application.port.out.ListingRepository;
import com.eightbitbazar.domain.exception.BusinessException;
import com.eightbitbazar.domain.exception.NotFoundException;
import com.eightbitbazar.domain.listing.Listing;
import com.eightbitbazar.domain.listing.ListingId;
import com.eightbitbazar.domain.listing.ListingImage;
import com.eightbitbazar.domain.user.UserId;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class UploadListingImage implements UploadListingImageUseCase {

    private static final int MAX_IMAGES = 10;

    private final ListingRepository listingRepository;
    private final ListingImageRepository listingImageRepository;
    private final ImageStorage imageStorage;

    public UploadListingImage(
        ListingRepository listingRepository,
        ListingImageRepository listingImageRepository,
        ImageStorage imageStorage
    ) {
        this.listingRepository = listingRepository;
        this.listingImageRepository = listingImageRepository;
        this.imageStorage = imageStorage;
    }

    @Override
    public List<String> execute(UserId userId, ListingId listingId, List<ImageUpload> images) {
        Listing listing = listingRepository.findById(listingId)
            .filter(l -> !l.isDeleted())
            .orElseThrow(() -> new NotFoundException("Listing not found"));

        if (!listing.sellerId().equals(userId)) {
            throw new BusinessException("You can only upload images to your own listings");
        }

        List<ListingImage> existingImages = listingImageRepository.findByListingId(listingId);
        if (existingImages.size() + images.size() > MAX_IMAGES) {
            throw new BusinessException("Maximum " + MAX_IMAGES + " images allowed per listing");
        }

        List<String> uploadedUrls = new ArrayList<>();
        int position = existingImages.size();

        for (ImageUpload image : images) {
            String uniqueFilename = generateUniqueFilename(listingId, image.filename());
            String url = imageStorage.upload(uniqueFilename, image.inputStream(), image.contentType(), image.size());

            ListingImage listingImage = new ListingImage(null, listingId, url, position++);
            listingImageRepository.save(listingImage);

            uploadedUrls.add(url);
        }

        return uploadedUrls;
    }

    private String generateUniqueFilename(ListingId listingId, String originalFilename) {
        String extension = "";
        int dotIndex = originalFilename.lastIndexOf('.');
        if (dotIndex > 0) {
            extension = originalFilename.substring(dotIndex);
        }
        return "listings/" + listingId.value() + "/" + UUID.randomUUID() + extension;
    }
}
