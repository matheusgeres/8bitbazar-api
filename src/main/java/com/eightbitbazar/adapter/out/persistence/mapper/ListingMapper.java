package com.eightbitbazar.adapter.out.persistence.mapper;

import com.eightbitbazar.adapter.out.persistence.entity.ListingEntity;
import com.eightbitbazar.adapter.out.persistence.entity.ListingImageEntity;
import com.eightbitbazar.domain.listing.*;
import com.eightbitbazar.domain.user.UserId;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ListingMapper {

    public Listing toDomain(ListingEntity entity) {
        if (entity == null) {
            return null;
        }

        List<ListingImage> images = entity.getImages().stream()
            .map(img -> new ListingImage(
                img.getId(),
                entity.getId() != null ? new ListingId(entity.getId()) : null,
                img.getUrl(),
                img.getPosition()
            ))
            .toList();

        return new Listing(
            entity.getId() != null ? new ListingId(entity.getId()) : null,
            new UserId(entity.getSellerId()),
            entity.getName(),
            entity.getDescription(),
            entity.getPlatformId(),
            entity.getManufacturerId(),
            ItemCondition.valueOf(entity.getItemCondition()),
            entity.getQuantity(),
            ListingType.valueOf(entity.getType()),
            entity.getPrice(),
            entity.getStartingPrice(),
            entity.getBuyNowPrice(),
            entity.getAuctionEndDate(),
            entity.getCashDiscountPercent(),
            ListingStatus.valueOf(entity.getStatus()),
            images,
            entity.getCreatedAt(),
            entity.getUpdatedAt(),
            entity.getDeletedAt()
        );
    }

    public ListingEntity toEntity(Listing domain) {
        if (domain == null) {
            return null;
        }

        ListingEntity entity = new ListingEntity();
        if (domain.id() != null) {
            entity.setId(domain.id().value());
        }
        entity.setSellerId(domain.sellerId().value());
        entity.setName(domain.name());
        entity.setDescription(domain.description());
        entity.setPlatformId(domain.platformId());
        entity.setManufacturerId(domain.manufacturerId());
        entity.setItemCondition(domain.condition().name());
        entity.setQuantity(domain.quantity());
        entity.setType(domain.type().name());
        entity.setPrice(domain.price());
        entity.setStartingPrice(domain.startingPrice());
        entity.setBuyNowPrice(domain.buyNowPrice());
        entity.setAuctionEndDate(domain.auctionEndDate());
        entity.setCashDiscountPercent(domain.cashDiscountPercent());
        entity.setStatus(domain.status().name());
        entity.setCreatedAt(domain.createdAt());
        entity.setUpdatedAt(domain.updatedAt());
        entity.setDeletedAt(domain.deletedAt());

        List<ListingImageEntity> imageEntities = domain.images().stream()
            .map(img -> {
                ListingImageEntity imgEntity = new ListingImageEntity();
                imgEntity.setId(img.id());
                imgEntity.setListing(entity);
                imgEntity.setUrl(img.url());
                imgEntity.setPosition(img.position());
                return imgEntity;
            })
            .toList();
        entity.setImages(imageEntities);

        return entity;
    }
}
