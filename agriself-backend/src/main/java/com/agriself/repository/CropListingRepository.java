package com.agriself.repository;

import com.agriself.model.CropListing;
import com.agriself.model.ListingStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CropListingRepository extends JpaRepository<CropListing, Long> {

    List<CropListing> findByCropNameContainingIgnoreCase(String crop);

    List<CropListing> findByStatus(ListingStatus status);

    List<CropListing> findByCropNameContainingIgnoreCaseAndStatus(String crop, ListingStatus status);

    List<CropListing> findByFarmerId(Long farmerId);

    List<CropListing> findByFarmerIdAndStatus(Long farmerId, ListingStatus status);
}
