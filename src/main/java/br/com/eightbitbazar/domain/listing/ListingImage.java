package br.com.eightbitbazar.domain.listing;

public record ListingImage(
    Long id,
    ListingId listingId,
    String url,
    int position
) {
    public ListingImage {
        if (url == null || url.isBlank()) {
            throw new IllegalArgumentException("URL is required");
        }
        if (position < 0) {
            throw new IllegalArgumentException("Position must be non-negative");
        }
    }
}
