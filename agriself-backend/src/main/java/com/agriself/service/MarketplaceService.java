package com.agriself.service;

import com.agriself.dto.CropListingDTO;
import com.agriself.model.CropListing;
import com.agriself.model.ListingStatus;
import com.agriself.model.User;
import com.agriself.repository.CropListingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class MarketplaceService {

    private final CropListingRepository cropListingRepository;

    @Transactional(readOnly = true)
    public List<CropListing> fetchActiveListings(String crop) {
        if (crop != null && !crop.trim().isEmpty()) {
            return cropListingRepository.findByCropNameContainingIgnoreCaseAndStatus(crop.trim(), ListingStatus.ACTIVE);
        }
        return cropListingRepository.findByStatus(ListingStatus.ACTIVE);
    }

    @Transactional(readOnly = true)
    public List<CropListing> fetchAllListings() {
        return cropListingRepository.findAll();
    }

    @Transactional
    public CropListing publishListing(CropListing newListing) {
        if (newListing.getStatus() == null) {
            newListing.setStatus(ListingStatus.ACTIVE);
        }
        log.info("Publishing new marketplace crop listing: {} by farmer {}", newListing.getCropName(),
                newListing.getFarmer() != null ? newListing.getFarmer().getFullName() : "Anonymous");
        return cropListingRepository.save(newListing);
    }

    @Transactional
    public CropListing publishListingFromDTO(CropListingDTO dto, User farmer) {
        CropListing listing = CropListing.builder()
                .farmer(farmer)
                .cropName(dto.getCropName())
                .variety(dto.getVariety())
                .quantity(dto.getQuantity())
                .unit(dto.getUnit())
                .pricePerUnit(dto.getPricePerUnit())
                .harvestDate(dto.getHarvestDate())
                .description(dto.getDescription())
                .location(dto.getLocation() != null ? dto.getLocation() : farmer.getLocation())
                .qualityGrade(dto.getQualityGrade() != null ? dto.getQualityGrade() : "Grade A Premium")
                .status(ListingStatus.ACTIVE)
                .build();
        return cropListingRepository.save(listing);
    }

    @Transactional(readOnly = true)
    public CropListing getListingById(Long id) {
        return cropListingRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Crop listing not found with id: " + id));
    }

    @Transactional
    public CropListing purchaseOrReserveListing(Long id, Double quantityRequested) {
        CropListing listing = getListingById(id);
        if (listing.getStatus() != ListingStatus.ACTIVE) {
            throw new IllegalStateException("Listing is no longer active for purchase (status: " + listing.getStatus() + ")");
        }

        if (quantityRequested != null && quantityRequested < listing.getQuantity()) {
            listing.setQuantity(listing.getQuantity() - quantityRequested);
        } else {
            listing.setStatus(ListingStatus.SOLD);
            listing.setQuantity(0.0);
        }

        return cropListingRepository.save(listing);
    }

    @Transactional
    public CropListing updateListingStatus(Long id, ListingStatus newStatus) {
        CropListing listing = getListingById(id);
        listing.setStatus(newStatus);
        return cropListingRepository.save(listing);
    }

    @Transactional(readOnly = true)
    public List<CropListing> getFarmerListings(Long farmerId) {
        return cropListingRepository.findByFarmerId(farmerId);
    }
}
