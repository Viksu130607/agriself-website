package com.agriself.controller;

import com.agriself.dto.CropListingDTO;
import com.agriself.model.CropListing;
import com.agriself.model.ListingStatus;
import com.agriself.model.User;
import com.agriself.security.UserDetailsImpl;
import com.agriself.service.AuthService;
import com.agriself.service.MarketplaceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/marketplace")
@RequiredArgsConstructor
public class MarketplaceController {

    private final MarketplaceService marketplaceService;
    private final AuthService authService;

    /**
     * Exact endpoint signature as specified in Section 6 Blueprint:
     * @GetMapping("/listings") public ResponseEntity<List<CropListing>>
     * getListings(@RequestParam(required = false) String crop)
     */
    @GetMapping("/listings")
    public ResponseEntity<List<CropListing>> getListings(@RequestParam(required = false) String crop) {
        List<CropListing> listings = marketplaceService.fetchActiveListings(crop);
        return ResponseEntity.ok(listings);
    }

    /**
     * Exact endpoint signature as specified in Section 6 Blueprint:
     * @PostMapping("/listings") public ResponseEntity<CropListing>
     * createListing(@RequestBody CropListing newListing)
     */
    @PostMapping("/listings")
    public ResponseEntity<CropListing> createListing(
            @RequestBody CropListing newListing,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        if (newListing.getFarmer() == null && userDetails != null) {
            newListing.setFarmer(userDetails.getUser());
        }
        CropListing saved = marketplaceService.publishListing(newListing);
        return ResponseEntity.status(201).body(saved);
    }

    /**
     * DTO-based listing publication for structured frontend form submissions
     */
    @PostMapping("/publish")
    public ResponseEntity<CropListing> publishListingDTO(
            @Valid @RequestBody CropListingDTO dto,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        User farmer = userDetails != null ? userDetails.getUser() : authService.getUserByPhoneNumber("9876543210"); // fallback
                                                                                                                    // to
                                                                                                                    // demo
                                                                                                                    // farmer
        CropListing saved = marketplaceService.publishListingFromDTO(dto, farmer);
        return ResponseEntity.status(201).body(saved);
    }

    @GetMapping("/listings/{id}")
    public ResponseEntity<CropListing> getListingById(@PathVariable Long id) {
        return ResponseEntity.ok(marketplaceService.getListingById(id));
    }

    @PostMapping("/listings/{id}/buy")
    public ResponseEntity<Map<String, Object>> buyCrop(
            @PathVariable Long id,
            @RequestBody(required = false) Map<String, Object> payload,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        Double quantity = null;
        if (payload != null && payload.containsKey("quantity")) {
            quantity = Double.parseDouble(payload.get("quantity").toString());
        }
        CropListing updated = marketplaceService.purchaseOrReserveListing(id, quantity);
        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Order confirmed! Cooperative logistics dispatched.",
                "listing", updated));
    }
}
