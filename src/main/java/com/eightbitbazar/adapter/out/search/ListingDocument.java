package com.eightbitbazar.adapter.out.search;

import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.math.BigDecimal;
import java.time.Instant;

@Document(indexName = "listings")
public class ListingDocument {

    @Id
    private Long id;

    @Field(type = FieldType.Text, analyzer = "standard")
    private String name;

    @Field(type = FieldType.Text, analyzer = "standard")
    private String description;

    @Field(type = FieldType.Long)
    private Long platformId;

    @Field(type = FieldType.Keyword)
    private String platformName;

    @Field(type = FieldType.Long)
    private Long manufacturerId;

    @Field(type = FieldType.Keyword)
    private String manufacturerName;

    @Field(type = FieldType.Keyword)
    private String condition;

    @Field(type = FieldType.Integer)
    private Integer quantity;

    @Field(type = FieldType.Keyword)
    private String type;

    @Field(type = FieldType.Double)
    private BigDecimal price;

    @Field(type = FieldType.Double)
    private BigDecimal startingPrice;

    @Field(type = FieldType.Double)
    private BigDecimal buyNowPrice;

    @Field(type = FieldType.Date)
    private Instant auctionEndDate;

    @Field(type = FieldType.Double)
    private BigDecimal cashDiscountPercent;

    @Field(type = FieldType.Keyword)
    private String status;

    @Field(type = FieldType.Long)
    private Long sellerId;

    @Field(type = FieldType.Keyword)
    private String sellerNickname;

    @Field(type = FieldType.Date)
    private Instant createdAt;

    public ListingDocument() {}

    public ListingDocument(
        Long id,
        String name,
        String description,
        Long platformId,
        String platformName,
        Long manufacturerId,
        String manufacturerName,
        String condition,
        Integer quantity,
        String type,
        BigDecimal price,
        BigDecimal startingPrice,
        BigDecimal buyNowPrice,
        Instant auctionEndDate,
        BigDecimal cashDiscountPercent,
        String status,
        Long sellerId,
        String sellerNickname,
        Instant createdAt
    ) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.platformId = platformId;
        this.platformName = platformName;
        this.manufacturerId = manufacturerId;
        this.manufacturerName = manufacturerName;
        this.condition = condition;
        this.quantity = quantity;
        this.type = type;
        this.price = price;
        this.startingPrice = startingPrice;
        this.buyNowPrice = buyNowPrice;
        this.auctionEndDate = auctionEndDate;
        this.cashDiscountPercent = cashDiscountPercent;
        this.status = status;
        this.sellerId = sellerId;
        this.sellerNickname = sellerNickname;
        this.createdAt = createdAt;
    }

    // Getters
    public Long getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public Long getPlatformId() { return platformId; }
    public String getPlatformName() { return platformName; }
    public Long getManufacturerId() { return manufacturerId; }
    public String getManufacturerName() { return manufacturerName; }
    public String getCondition() { return condition; }
    public Integer getQuantity() { return quantity; }
    public String getType() { return type; }
    public BigDecimal getPrice() { return price; }
    public BigDecimal getStartingPrice() { return startingPrice; }
    public BigDecimal getBuyNowPrice() { return buyNowPrice; }
    public Instant getAuctionEndDate() { return auctionEndDate; }
    public BigDecimal getCashDiscountPercent() { return cashDiscountPercent; }
    public String getStatus() { return status; }
    public Long getSellerId() { return sellerId; }
    public String getSellerNickname() { return sellerNickname; }
    public Instant getCreatedAt() { return createdAt; }

    // Setters
    public void setId(Long id) { this.id = id; }
    public void setName(String name) { this.name = name; }
    public void setDescription(String description) { this.description = description; }
    public void setPlatformId(Long platformId) { this.platformId = platformId; }
    public void setPlatformName(String platformName) { this.platformName = platformName; }
    public void setManufacturerId(Long manufacturerId) { this.manufacturerId = manufacturerId; }
    public void setManufacturerName(String manufacturerName) { this.manufacturerName = manufacturerName; }
    public void setCondition(String condition) { this.condition = condition; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }
    public void setType(String type) { this.type = type; }
    public void setPrice(BigDecimal price) { this.price = price; }
    public void setStartingPrice(BigDecimal startingPrice) { this.startingPrice = startingPrice; }
    public void setBuyNowPrice(BigDecimal buyNowPrice) { this.buyNowPrice = buyNowPrice; }
    public void setAuctionEndDate(Instant auctionEndDate) { this.auctionEndDate = auctionEndDate; }
    public void setCashDiscountPercent(BigDecimal cashDiscountPercent) { this.cashDiscountPercent = cashDiscountPercent; }
    public void setStatus(String status) { this.status = status; }
    public void setSellerId(Long sellerId) { this.sellerId = sellerId; }
    public void setSellerNickname(String sellerNickname) { this.sellerNickname = sellerNickname; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
