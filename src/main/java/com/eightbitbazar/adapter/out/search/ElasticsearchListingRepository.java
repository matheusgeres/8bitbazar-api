package com.eightbitbazar.adapter.out.search;

import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

public interface ElasticsearchListingRepository extends ElasticsearchRepository<ListingDocument, Long> {
}
