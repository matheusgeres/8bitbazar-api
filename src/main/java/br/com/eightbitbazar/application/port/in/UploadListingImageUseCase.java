package br.com.eightbitbazar.application.port.in;

import br.com.eightbitbazar.domain.listing.ListingId;
import br.com.eightbitbazar.domain.user.UserId;

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
