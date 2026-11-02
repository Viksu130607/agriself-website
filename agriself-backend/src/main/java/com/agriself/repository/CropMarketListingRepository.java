package com.agriself.repository;

import com.agriself.model.CropMarketListing;
import com.agriself.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CropMarketListingRepository extends JpaRepository<CropMarketListing, Long> {
    List<CropMarketListing> findByStatusOrderByListedAtDesc(String status);
    List<CropMarketListing> findByCropNameContainingIgnoreCaseAndStatus(String cropName, String status);
    List<CropMarketListing> findBySellerFarmerOrderByListedAtDesc(User sellerFarmer);
}

