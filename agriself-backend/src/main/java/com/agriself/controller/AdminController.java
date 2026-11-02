package com.agriself.controller;

import com.agriself.dto.DashboardStatsDTO;
import com.agriself.model.CropListing;
import com.agriself.model.ListingStatus;
import com.agriself.model.User;
import com.agriself.model.UserRole;
import com.agriself.repository.CropListingRepository;
import com.agriself.repository.UserRepository;
import com.agriself.service.AdminService;
import com.agriself.service.MarketplaceService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;
    private final UserRepository userRepository;
    private final MarketplaceService marketplaceService;

    @GetMapping("/stats")
    public ResponseEntity<DashboardStatsDTO> getStats() {
        return ResponseEntity.ok(adminService.getPlatformDashboardStats());
    }

    @GetMapping("/farmers")
    public ResponseEntity<List<User>> getAllFarmers() {
        return ResponseEntity.ok(userRepository.findByRole(UserRole.ROLE_FARMER));
    }

    @GetMapping("/buyers")
    public ResponseEntity<List<User>> getAllBuyers() {
        return ResponseEntity.ok(userRepository.findByRole(UserRole.ROLE_BUSINESSMAN));
    }

    @GetMapping("/listings")
    public ResponseEntity<List<CropListing>> getAllListings() {
        return ResponseEntity.ok(marketplaceService.fetchAllListings());
    }

    @PutMapping("/listings/{id}/approve")
    public ResponseEntity<CropListing> approveListing(@PathVariable Long id) {
        return ResponseEntity.ok(marketplaceService.updateListingStatus(id, ListingStatus.ACTIVE));
    }
}
