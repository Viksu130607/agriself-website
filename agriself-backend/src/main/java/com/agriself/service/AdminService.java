package com.agriself.service;

import com.agriself.dto.DashboardStatsDTO;
import com.agriself.model.Farm;
import com.agriself.model.ListingStatus;
import com.agriself.model.UserRole;
import com.agriself.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminService {

    private final UserRepository userRepository;
    private final FarmRepository farmRepository;
    private final CropListingRepository cropListingRepository;
    private final EquipmentBookingRepository equipmentBookingRepository;
    private final AIDiagnosticRecordRepository diagnosticRecordRepository;
    private final FarmService farmService;

    @Transactional(readOnly = true)
    public DashboardStatsDTO getPlatformDashboardStats() {
        long farmers = userRepository.countByRole(UserRole.ROLE_FARMER);
        long buyers = userRepository.countByRole(UserRole.ROLE_BUSINESSMAN);
        long farms = farmRepository.count();

        List<Farm> allFarms = farmRepository.findAll();
        double totalAcreage = allFarms.stream().mapToDouble(Farm::getSizeInAcres).sum();
        double avgMoisture = allFarms.isEmpty() ? 0 : allFarms.stream().mapToDouble(Farm::getCurrentSoilMoisture).average().orElse(0);
        double avgPh = allFarms.isEmpty() ? 0 : allFarms.stream().mapToDouble(Farm::getSoilPh).average().orElse(0);

        long activeListings = cropListingRepository.findByStatus(ListingStatus.ACTIVE).size();
        long totalBookings = equipmentBookingRepository.count();
        long aiScans = diagnosticRecordRepository.count();
        Map<String, Long> soilDistribution = farmService.getSoilTypeDistribution();

        return DashboardStatsDTO.builder()
                .totalFarmers(farmers)
                .totalBuyers(buyers)
                .totalFarms(farms)
                .totalAcreage(Math.round(totalAcreage * 10.0) / 10.0)
                .activeCropListings(activeListings)
                .totalEquipmentBookings(totalBookings)
                .totalAiScans(aiScans)
                .soilDistribution(soilDistribution)
                .averageSoilMoisture(Math.round(avgMoisture * 10.0) / 10.0)
                .averageSoilPh(Math.round(avgPh * 10.0) / 10.0)
                .build();
    }
}
