package br.com.eightbitbazar.adapter.out.search;

import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.math.BigDecimal;
import java.time.Instant;

@Document(indexName = "listings")
public record ListingDocument(
    @Id
    Long id,

    @Field(type = FieldType.Text, analyzer = "standard")
    String name,

    @Field(type = FieldType.Text, analyzer = "standard")
    String description,

    @Field(type = FieldType.Long)
    Long platformId,

    @Field(type = FieldType.Keyword)
    String platformName,

    @Field(type = FieldType.Long)
    Long manufacturerId,

    @Field(type = FieldType.Keyword)
    String manufacturerName,

    @Field(type = FieldType.Keyword)
    String condition,

    @Field(type = FieldType.Integer)
    Integer quantity,

    @Field(type = FieldType.Keyword)
    String type,

    @Field(type = FieldType.Double)
    BigDecimal price,

    @Field(type = FieldType.Double)
    BigDecimal startingPrice,

    @Field(type = FieldType.Double)
    BigDecimal buyNowPrice,

    @Field(type = FieldType.Date)
    Instant auctionEndDate,

    @Field(type = FieldType.Double)
    BigDecimal cashDiscountPercent,

    @Field(type = FieldType.Keyword)
    String status,

    @Field(type = FieldType.Long)
    Long sellerId,

    @Field(type = FieldType.Keyword)
    String sellerNickname,

    @Field(type = FieldType.Date)
    Instant createdAt,

    @Field(type = FieldType.Date)
    Instant deletedAt
) {}
