package com.eightbitbazar.adapter.out.search;

import com.eightbitbazar.application.port.out.ListingSearchRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.Query;
import org.springframework.stereotype.Repository;

import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.QueryBuilders;

import java.util.List;

@Repository
public class ElasticsearchListingSearchAdapter implements ListingSearchRepository {

    private final ElasticsearchOperations elasticsearchOperations;

    public ElasticsearchListingSearchAdapter(ElasticsearchOperations elasticsearchOperations) {
        this.elasticsearchOperations = elasticsearchOperations;
    }

    @Override
    public Page<ListingSearchResult> search(SearchCriteria criteria, Pageable pageable) {
        BoolQuery.Builder boolQuery = new BoolQuery.Builder();

        // Only active listings
        boolQuery.must(QueryBuilders.term(t -> t.field("status").value("ACTIVE")));

        // Full text search on name and description
        if (criteria.query() != null && !criteria.query().isBlank()) {
            boolQuery.must(QueryBuilders.multiMatch(m -> m
                .fields("name^2", "description")
                .query(criteria.query())
                .fuzziness("AUTO")
            ));
        }

        // Filter by type
        if (criteria.type() != null && !criteria.type().isBlank()) {
            boolQuery.filter(QueryBuilders.term(t -> t.field("type").value(criteria.type())));
        }

        // Filter by platform
        if (criteria.platformId() != null) {
            boolQuery.filter(QueryBuilders.term(t -> t.field("platformId").value(criteria.platformId())));
        }

        // Filter by manufacturer
        if (criteria.manufacturerId() != null) {
            boolQuery.filter(QueryBuilders.term(t -> t.field("manufacturerId").value(criteria.manufacturerId())));
        }

        Query query = NativeQuery.builder()
            .withQuery(q -> q.bool(boolQuery.build()))
            .withPageable(pageable)
            .build();

        SearchHits<ListingDocument> searchHits = elasticsearchOperations.search(query, ListingDocument.class);

        List<ListingSearchResult> results = searchHits.getSearchHits().stream()
            .map(SearchHit::getContent)
            .map(this::toSearchResult)
            .toList();

        return new PageImpl<>(results, pageable, searchHits.getTotalHits());
    }

    @Override
    public void index(ListingSearchResult listing) {
        ListingDocument document = toDocument(listing);
        elasticsearchOperations.save(document);
    }

    @Override
    public void delete(Long listingId) {
        elasticsearchOperations.delete(String.valueOf(listingId), ListingDocument.class);
    }

    private ListingSearchResult toSearchResult(ListingDocument doc) {
        return new ListingSearchResult(
            doc.getId(),
            doc.getName(),
            doc.getDescription(),
            doc.getPlatformId(),
            doc.getPlatformName(),
            doc.getManufacturerId(),
            doc.getManufacturerName(),
            doc.getCondition(),
            doc.getQuantity(),
            doc.getType(),
            doc.getPrice(),
            doc.getStartingPrice(),
            doc.getBuyNowPrice(),
            doc.getAuctionEndDate(),
            doc.getCashDiscountPercent(),
            doc.getStatus(),
            doc.getSellerId(),
            doc.getSellerNickname(),
            List.of(), // Images loaded separately
            doc.getCreatedAt()
        );
    }

    private ListingDocument toDocument(ListingSearchResult result) {
        return new ListingDocument(
            result.id(),
            result.name(),
            result.description(),
            result.platformId(),
            result.platformName(),
            result.manufacturerId(),
            result.manufacturerName(),
            result.condition(),
            result.quantity(),
            result.type(),
            result.price(),
            result.startingPrice(),
            result.buyNowPrice(),
            result.auctionEndDate(),
            result.cashDiscountPercent(),
            result.status(),
            result.sellerId(),
            result.sellerNickname(),
            result.createdAt()
        );
    }
}
