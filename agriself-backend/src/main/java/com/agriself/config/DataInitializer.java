package com.agriself.config;

import com.agriself.model.*;
import com.agriself.repository.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Seeds initial production-grade demonstration data for AgriSelf Platform:
 * 1. Administrator (Granted via Database Administrator Seeding)
 * 2. Farmer with Field & Telemetry Data
 * 3. Businessman with Crop Marketplace & Mandi Rates
 * 4. Company Machinery Fleet for Administrator Monitoring
 */
@Component
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final FarmFieldRepository farmFieldRepository;
    private final MandiPriceRepository mandiPriceRepository;
    private final CropMarketListingRepository cropMarketListingRepository;
    private final MachineryRepository machineryRepository;
    private final AuditLogRepository auditLogRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(UserRepository userRepository,
                           FarmFieldRepository farmFieldRepository,
                           MandiPriceRepository mandiPriceRepository,
                           CropMarketListingRepository cropMarketListingRepository,
                           MachineryRepository machineryRepository,
                           AuditLogRepository auditLogRepository,
                           PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.farmFieldRepository = farmFieldRepository;
        this.mandiPriceRepository = mandiPriceRepository;
        this.cropMarketListingRepository = cropMarketListingRepository;
        this.machineryRepository = machineryRepository;
        this.auditLogRepository = auditLogRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @SuppressWarnings("null")
    public void run(String... args) {
        if (userRepository.count() > 0) {
            log.info("AgriSelf database already initialized.");
            return;
        }

        log.info("🌱 Seeding AgriSelf Full-Stack Platform Initial Data...");

        // =========================================================================
        // 1. Seed Accounts (Hash Passwords with BCrypt strength 12)
        // =========================================================================
        // A. Company Administrator (Granted via DB Seeding, not self-registered)
        User adminUser = User.builder()
                .fullName("Rajesh Sharma (System Administrator)")
                .email("admin@agriself.com")
                .phoneNumber("9998887770")
                .password(passwordEncoder.encode("Admin@123"))
                .role(UserRole.ROLE_ADMIN)
                .location("AgriSelf Corporate Hub, Bengaluru")
                .enabled(true)
                .build();
        userRepository.save(adminUser);

        // B. Farmer
        User farmerUser = User.builder()
                .fullName("Ramesh Patel")
                .email("farmer@agriself.com")
                .phoneNumber("9876543210")
                .password(passwordEncoder.encode("Farmer@123"))
                .role(UserRole.ROLE_FARMER)
                .location("Nashik, Maharashtra")
                .enabled(true)
                .build();
        userRepository.save(farmerUser);

        // C. Businessman / Consumer
        User buyerUser = User.builder()
                .fullName("Vikram Singhania (Agro Traders & Consumer Goods)")
                .email("buyer@agriself.com")
                .phoneNumber("9811223344")
                .password(passwordEncoder.encode("Buyer@123"))
                .role(UserRole.ROLE_BUSINESSMAN)
                .location("APMC Vashi, Navi Mumbai")
                .enabled(true)
                .build();
        userRepository.save(buyerUser);

        // =========================================================================
        // 2. Seed Farmer Field Data (Field Telemetry & Requirements)
        // =========================================================================
        FarmField field1 = FarmField.builder()
                .farmer(farmerUser)
                .fieldName("Plot A - Sharbati Wheat Belt")
                .sizeInAcres(12.5)
                .soilType("Black Cotton (काली मिट्टी)")
                .currentSoilMoisture(43.8)
                .soilPh(6.8)
                .nitrogenPpm(142)
                .phosphorusPpm(44)
                .potassiumPpm(195)
                .currentCrop("Sharbati Wheat (शरबती गेहूं)")
                .cropGrowthStage("Tillering Stage (कल्ले फूटने की अवस्था)")
                .waterRequirementLiters(1250.0)
                .irrigationSchedule("Tomorrow 06:00 AM (Drip 45 Mins)")
                .fertilizerRecommendation("Urea 25kg/acre + Zinc 5kg after irrigation")
                .overallCropHealth("Optimal - Healthy Crop")
                .lastTelemetrySync(LocalDateTime.now())
                .build();
        farmFieldRepository.save(field1);

        FarmField field2 = FarmField.builder()
                .farmer(farmerUser)
                .fieldName("Plot B - Organic Mustard & Pulses")
                .sizeInAcres(6.0)
                .soilType("Alluvial Loam (दोमट मिट्टी)")
                .currentSoilMoisture(38.2)
                .soilPh(7.1)
                .nitrogenPpm(120)
                .phosphorusPpm(36)
                .potassiumPpm(170)
                .currentCrop("Yellow Mustard (पीली सरसों)")
                .cropGrowthStage("Pod Formation (फलियां बनने की अवस्था)")
                .waterRequirementLiters(800.0)
                .irrigationSchedule("Today 05:30 PM (Drip 30 Mins)")
                .fertilizerRecommendation("Potash spray (0:0:50) 1kg in 100L water")
                .overallCropHealth("Attention Needed: Low Soil Moisture")
                .lastTelemetrySync(LocalDateTime.now())
                .build();
        farmFieldRepository.save(field2);

        // =========================================================================
        // 3. Seed Live Mandi Rates Across India for Businessmen & Consumers
        // =========================================================================
        mandiPriceRepository.save(MandiPrice.builder()
                .commodity("Wheat (गेहूं - Sharbati)")
                .state("Madhya Pradesh")
                .mandiName("Sehore APMC Mandi")
                .minPricePerQuintal(2450.0)
                .maxPricePerQuintal(2850.0)
                .modalPricePerQuintal(2720.0)
                .priceTrend("RISING")
                .percentageChange(3.4)
                .reportDate(LocalDate.now())
                .build());

        mandiPriceRepository.save(MandiPrice.builder()
                .commodity("Basmati Rice 1121")
                .state("Punjab")
                .mandiName("Khanna Grain Market")
                .minPricePerQuintal(3800.0)
                .maxPricePerQuintal(4400.0)
                .modalPricePerQuintal(4250.0)
                .priceTrend("STABLE")
                .percentageChange(0.5)
                .reportDate(LocalDate.now())
                .build());

        mandiPriceRepository.save(MandiPrice.builder()
                .commodity("Yellow Soybean")
                .state("Maharashtra")
                .mandiName("Latur APMC")
                .minPricePerQuintal(4200.0)
                .maxPricePerQuintal(4650.0)
                .modalPricePerQuintal(4510.0)
                .priceTrend("RISING")
                .percentageChange(2.1)
                .reportDate(LocalDate.now())
                .build());

        mandiPriceRepository.save(MandiPrice.builder()
                .commodity("Cotton (कपास - Medium Staple)")
                .state("Gujarat")
                .mandiName("Rajkot APMC")
                .minPricePerQuintal(6800.0)
                .maxPricePerQuintal(7450.0)
                .modalPricePerQuintal(7200.0)
                .priceTrend("FALLING")
                .percentageChange(-1.2)
                .reportDate(LocalDate.now())
                .build());

        mandiPriceRepository.save(MandiPrice.builder()
                .commodity("Red Onion (लाल प्याज)")
                .state("Maharashtra")
                .mandiName("Lasalgaon Onion Mandi")
                .minPricePerQuintal(1800.0)
                .maxPricePerQuintal(2350.0)
                .modalPricePerQuintal(2150.0)
                .priceTrend("RISING")
                .percentageChange(4.8)
                .reportDate(LocalDate.now())
                .build());

        mandiPriceRepository.save(MandiPrice.builder()
                .commodity("Mustard Seed (सरसों)")
                .state("Rajasthan")
                .mandiName("Bharatpur Mandi")
                .minPricePerQuintal(5100.0)
                .maxPricePerQuintal(5550.0)
                .modalPricePerQuintal(5380.0)
                .priceTrend("STABLE")
                .percentageChange(0.2)
                .reportDate(LocalDate.now())
                .build());

        // =========================================================================
        // 4. Seed Crops on Sale (Direct From Farmers to Businessmen / Consumers)
        // =========================================================================
        cropMarketListingRepository.save(CropMarketListing.builder()
                .sellerFarmer(farmerUser)
                .cropName("Premium Sharbati Wheat")
                .variety("C-306 Golden Grain")
                .quantityQuintals(150.0)
                .qualityGrade("Grade A+ Export Quality")
                .pricePerQuintal(2750.0)
                .farmLocation("Dindori, Nashik (Maharashtra)")
                .contactNumber("9876543210")
                .status("AVAILABLE")
                .qualityNotes("Sun-dried, 11% moisture, zero chemical residues.")
                .listedAt(LocalDateTime.now().minusDays(2))
                .build());

        cropMarketListingRepository.save(CropMarketListing.builder()
                .sellerFarmer(farmerUser)
                .cropName("Fresh Red Onions")
                .variety("Garva Special")
                .quantityQuintals(85.0)
                .qualityGrade("Grade A Medium-Large")
                .pricePerQuintal(2100.0)
                .farmLocation("Lasalgaon Outskirts, Nashik")
                .contactNumber("9876543210")
                .status("AVAILABLE")
                .qualityNotes("Naturally cured skin, long shelf life.")
                .listedAt(LocalDateTime.now().minusDays(1))
                .build());

        cropMarketListingRepository.save(CropMarketListing.builder()
                .sellerFarmer(farmerUser)
                .cropName("Organic Yellow Soybean")
                .variety("JS 335")
                .quantityQuintals(200.0)
                .qualityGrade("Certified Organic")
                .pricePerQuintal(4550.0)
                .farmLocation("Amravati Farm Gate")
                .contactNumber("9876543210")
                .status("AVAILABLE")
                .qualityNotes("High protein 41%, oil content 19.5%.")
                .listedAt(LocalDateTime.now().minusHours(18))
                .build());

        // =========================================================================
        // 5. Seed Machinery Fleet for Company Administrator Monitoring
        // =========================================================================
        machineryRepository.save(Machinery.builder()
                .equipmentCode("MCH-TR-101")
                .name("John Deere 5050D Autonomous Tractor")
                .category("Tractor")
                .status(MachineryStatus.IN_USE)
                .assignedLocation("Plot A - Nashik Wheat Belt")
                .assignedToUser("Ramesh Patel (Farmer)")
                .operatingHours(420.5)
                .fuelOrBatteryPercent(78)
                .lastServiceDate(LocalDate.now().minusMonths(1))
                .nextServiceDate(LocalDate.now().plusMonths(2))
                .healthNotes("Engine RPM and hydraulic pressure nominal. GPS RTK calibrated.")
                .build());

        machineryRepository.save(Machinery.builder()
                .equipmentCode("MCH-DR-201")
                .name("AgriSelf Hexacopter Autonomous Drone Sprayer (16L)")
                .category("Drone Sprayer")
                .status(MachineryStatus.IN_USE)
                .assignedLocation("Plot B - Mustard Field")
                .assignedToUser("Ramesh Patel (Farmer)")
                .operatingHours(85.0)
                .fuelOrBatteryPercent(65)
                .lastServiceDate(LocalDate.now().minusWeeks(2))
                .nextServiceDate(LocalDate.now().plusWeeks(6))
                .healthNotes("Multispectral LiDAR camera online. Spray nozzles calibrated.")
                .build());

        machineryRepository.save(Machinery.builder()
                .equipmentCode("MCH-RV-301")
                .name("Shaktiman Regular Smart Rotavator (7 Feet)")
                .category("Rotavator")
                .status(MachineryStatus.AVAILABLE)
                .assignedLocation("Central Agro Depot - Nashik")
                .assignedToUser("Unassigned (In Depot)")
                .operatingHours(142.0)
                .fuelOrBatteryPercent(95)
                .lastServiceDate(LocalDate.now().minusMonths(2))
                .nextServiceDate(LocalDate.now().plusMonths(1))
                .healthNotes("Blades inspected and sharpened. Ready for deployment.")
                .build());

        machineryRepository.save(Machinery.builder()
                .equipmentCode("MCH-DP-401")
                .name("Solar Sub-Surface Smart Drip Pump (5 HP)")
                .category("Drip Pump")
                .status(MachineryStatus.IN_USE)
                .assignedLocation("Main Reservoir - Station 1")
                .assignedToUser("Automated Smart Drip Grid")
                .operatingHours(890.0)
                .fuelOrBatteryPercent(92)
                .lastServiceDate(LocalDate.now().minusMonths(3))
                .nextServiceDate(LocalDate.now().plusWeeks(3))
                .healthNotes("Solar MPPT controller active. Water flow rate 120 L/min.")
                .build());

        machineryRepository.save(Machinery.builder()
                .equipmentCode("MCH-HV-501")
                .name("Preet 987 Self-Propelled Combine Harvester")
                .category("Harvester")
                .status(MachineryStatus.AVAILABLE)
                .assignedLocation("Central Agro Depot - Pune")
                .assignedToUser("Unassigned (In Depot)")
                .operatingHours(310.5)
                .fuelOrBatteryPercent(88)
                .lastServiceDate(LocalDate.now().minusWeeks(3))
                .nextServiceDate(LocalDate.now().plusMonths(3))
                .healthNotes("Cutter bar and threshing drum cleaned and greased.")
                .build());

        machineryRepository.save(Machinery.builder()
                .equipmentCode("MCH-LL-601")
                .name("Laser Land Leveler Pro GNSS")
                .category("Laser Leveler")
                .status(MachineryStatus.UNDER_MAINTENANCE)
                .assignedLocation("Technical Service Bay 2 - Nashik")
                .assignedToUser("Workshop Technicians")
                .operatingHours(510.0)
                .fuelOrBatteryPercent(35)
                .lastServiceDate(LocalDate.now().minusMonths(4))
                .nextServiceDate(LocalDate.now().plusDays(4))
                .healthNotes("Hydraulic valve seal replacement underway. Scheduled completion in 48h.")
                .build());

        // Initial Audit Log
        auditLogRepository.save(AuditLog.builder()
                .eventType("SYSTEM_INITIALIZED")
                .username("SYSTEM")
                .details("AgriSelf secure full-stack platform seeded with seed users, field telemetry, Mandi rates, and fleet machinery.")
                .build());

        log.info("✅ AgriSelf platform data seeded successfully.");
    }
}
