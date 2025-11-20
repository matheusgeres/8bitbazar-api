package com.eightbitbazar.config;

import com.eightbitbazar.application.port.in.*;
import com.eightbitbazar.application.port.out.*;
import com.eightbitbazar.application.usecase.admin.*;
import com.eightbitbazar.application.usecase.bid.*;
import com.eightbitbazar.application.usecase.listing.*;
import com.eightbitbazar.application.usecase.purchase.*;
import com.eightbitbazar.application.usecase.user.*;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class UseCaseConfig {

    @Bean
    public RegisterUserUseCase registerUserUseCase(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        return new RegisterUser(userRepository, passwordEncoder);
    }

    @Bean
    public GetUserProfileUseCase getUserProfileUseCase(UserRepository userRepository) {
        return new GetUserProfile(userRepository);
    }

    @Bean
    public UpdateUserProfileUseCase updateUserProfileUseCase(UserRepository userRepository) {
        return new UpdateUserProfile(userRepository);
    }

    @Bean
    public DeleteUserUseCase deleteUserUseCase(UserRepository userRepository) {
        return new DeleteUser(userRepository);
    }

    // Listing Use Cases
    @Bean
    public CreateListingUseCase createListingUseCase(
        ListingRepository listingRepository,
        UserRepository userRepository,
        PlatformRepository platformRepository,
        ManufacturerRepository manufacturerRepository,
        EventPublisher eventPublisher
    ) {
        return new CreateListing(
            listingRepository,
            userRepository,
            platformRepository,
            manufacturerRepository,
            eventPublisher
        );
    }

    @Bean
    public DeleteListingUseCase deleteListingUseCase(ListingRepository listingRepository, EventPublisher eventPublisher) {
        return new DeleteListing(listingRepository, eventPublisher);
    }

    @Bean
    @Primary
    @ConditionalOnBean(ListingSearchRepository.class)
    public SearchListingsUseCase searchListingsWithElasticsearch(
        ListingSearchRepository listingSearchRepository,
        ListingImageRepository listingImageRepository
    ) {
        return new SearchListingsWithElasticsearch(listingSearchRepository, listingImageRepository);
    }

    @Bean
    public SearchListingsUseCase searchListingsUseCase(
        ListingRepository listingRepository,
        UserRepository userRepository,
        PlatformRepository platformRepository,
        ManufacturerRepository manufacturerRepository
    ) {
        return new SearchListings(listingRepository, userRepository, platformRepository, manufacturerRepository);
    }

    @Bean
    public GetListingDetailsUseCase getListingDetailsUseCase(
        ListingRepository listingRepository,
        UserRepository userRepository,
        PlatformRepository platformRepository,
        ManufacturerRepository manufacturerRepository,
        BidRepository bidRepository
    ) {
        return new GetListingDetails(listingRepository, userRepository, platformRepository, manufacturerRepository, bidRepository);
    }

    @Bean
    public UploadListingImageUseCase uploadListingImageUseCase(
        ListingRepository listingRepository,
        ListingImageRepository listingImageRepository,
        ImageStorage imageStorage
    ) {
        return new UploadListingImage(listingRepository, listingImageRepository, imageStorage);
    }

    // Bid Use Cases
    @Bean
    public PlaceBidUseCase placeBidUseCase(
        BidRepository bidRepository,
        ListingRepository listingRepository,
        PurchaseRepository purchaseRepository,
        EventPublisher eventPublisher
    ) {
        return new PlaceBid(bidRepository, listingRepository, purchaseRepository, eventPublisher);
    }

    // Purchase Use Cases
    @Bean
    public DirectPurchaseUseCase directPurchaseUseCase(
        PurchaseRepository purchaseRepository,
        ListingRepository listingRepository,
        EventPublisher eventPublisher
    ) {
        return new DirectPurchase(purchaseRepository, listingRepository, eventPublisher);
    }

    // Admin Use Cases
    @Bean
    public CreatePlatformUseCase createPlatformUseCase(PlatformRepository platformRepository) {
        return new CreatePlatform(platformRepository);
    }

    @Bean
    public ListPlatformsUseCase listPlatformsUseCase(PlatformRepository platformRepository) {
        return new ListPlatforms(platformRepository);
    }

    @Bean
    public CreateManufacturerUseCase createManufacturerUseCase(ManufacturerRepository manufacturerRepository) {
        return new CreateManufacturer(manufacturerRepository);
    }

    @Bean
    public ListManufacturersUseCase listManufacturersUseCase(ManufacturerRepository manufacturerRepository) {
        return new ListManufacturers(manufacturerRepository);
    }
}
