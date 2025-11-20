package com.eightbitbazar.application.port.in;

import com.eightbitbazar.domain.listing.ListingId;
import com.eightbitbazar.domain.user.UserId;

import java.io.InputStream;
import java.util.List;

public interface UploadListingImageUseCase {

    List<String> execute(UserId userId, ListingId listingId, List<ImageUpload> images);

    record ImageUpload(
        String filename,
        InputStream inputStream,
        String contentType,
        long size
    ) {}
}
