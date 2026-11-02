package com.agriself.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DashboardStatsDTO {

    private long totalFarmers;
    private long totalBuyers;
    private long totalFarms;
    private double totalAcreage;
    private long activeCropListings;
    private long totalEquipmentBookings;
    private long totalAiScans;
    private Map<String, Long> soilDistribution;
    private double averageSoilMoisture;
    private double averageSoilPh;
}
