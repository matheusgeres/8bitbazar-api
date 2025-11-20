package com.eightbitbazar.application.usecase.listing;

public record ListingSearchInput(
    String search,
    String type,
    Long platformId,
    Long manufacturerId,
    String sort,
    int page,
    int size
) {}
