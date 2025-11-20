package com.eightbitbazar.adapter.out.persistence.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "listing_images")
public class ListingImageEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "listing_id", nullable = false)
    private ListingEntity listing;

    @Column(nullable = false)
    private String url;

    @Column(nullable = false)
    private int position;

    public ListingImageEntity() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public ListingEntity getListing() { return listing; }
    public void setListing(ListingEntity listing) { this.listing = listing; }

    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }

    public int getPosition() { return position; }
    public void setPosition(int position) { this.position = position; }
}
