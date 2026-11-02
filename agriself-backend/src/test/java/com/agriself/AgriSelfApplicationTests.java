package com.agriself;

import com.agriself.dto.FarmerAIRequestDTO;
import com.agriself.dto.FarmerAIResponseDTO;
import com.agriself.model.*;
import com.agriself.repository.*;
import com.agriself.security.InputSanitizer;
import com.agriself.service.AIDiagnosticService;
import com.agriself.service.FarmerAIEngine;
import com.agriself.service.RentalBookingEngine;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

@SpringBootTest
class AgriSelfApplicationTests {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private FarmFieldRepository farmFieldRepository;

    @Autowired
    private MandiPriceRepository mandiPriceRepository;

    @Autowired
    private CropMarketListingRepository cropMarketListingRepository;

    @Autowired
    private MachineryRepository machineryRepository;

    @Autowired
    private AuditLogRepository auditLogRepository;

    @Autowired
    private FarmerAIEngine farmerAIEngine;

    @Autowired
    private InputSanitizer inputSanitizer;

    @Autowired
    private RentalBookingEngine rentalBookingEngine;

    @Autowired
    private AIDiagnosticService diagnosticService;

    @Test
    @DisplayName("1. Context & Repositories Load Successfully")
    void contextLoads() {
        Assertions.assertNotNull(userRepository);
        Assertions.assertNotNull(farmFieldRepository);
        Assertions.assertNotNull(mandiPriceRepository);
        Assertions.assertNotNull(cropMarketListingRepository);
        Assertions.assertNotNull(machineryRepository);
        Assertions.assertNotNull(auditLogRepository);
        Assertions.assertNotNull(farmerAIEngine);
    }

    @Test
    @DisplayName("2. Verify 3 Distinct Role Accounts (Farmer, Businessman, Administrator)")
    void testThreeRoleAccounts() {
        // Farmer
        User farmer = userRepository.findByEmail("farmer@agriself.com").orElse(null);
        Assertions.assertNotNull(farmer);
        Assertions.assertEquals(UserRole.ROLE_FARMER, farmer.getRole());

        // Businessman / Consumer
        User buyer = userRepository.findByEmail("buyer@agriself.com").orElse(null);
        Assertions.assertNotNull(buyer);
        Assertions.assertEquals(UserRole.ROLE_BUSINESSMAN, buyer.getRole());

        // Company Administrator
        User admin = userRepository.findByEmail("admin@agriself.com").orElse(null);
        Assertions.assertNotNull(admin);
        Assertions.assertEquals(UserRole.ROLE_ADMIN, admin.getRole());
    }

    @Test
    @DisplayName("3. Verify Farmer Field Telemetry & Requirements")
    void testFarmerFieldTelemetry() {
        List<FarmField> fields = farmFieldRepository.findByFarmerEmail("farmer@agriself.com");
        Assertions.assertFalse(fields.isEmpty());
        FarmField primary = fields.get(0);
        Assertions.assertTrue(primary.getCurrentSoilMoisture() > 0);
        Assertions.assertTrue(primary.getSoilPh() > 5.0 && primary.getSoilPh() < 8.5);
        Assertions.assertNotNull(primary.getWaterRequirementLiters());
        Assertions.assertNotNull(primary.getIrrigationSchedule());
        Assertions.assertNotNull(primary.getFertilizerRecommendation());
    }

    @Test
    @DisplayName("4. Verify Businessman Market Prices & Crop Listings")
    void testBusinessmanMarketplace() {
        List<MandiPrice> prices = mandiPriceRepository.findAllByOrderByModalPricePerQuintalDesc();
        Assertions.assertFalse(prices.isEmpty());
        Assertions.assertTrue(prices.get(0).getModalPricePerQuintal() > 1000);

        List<CropMarketListing> listings = cropMarketListingRepository.findByStatusOrderByListedAtDesc("AVAILABLE");
        Assertions.assertFalse(listings.isEmpty());
        Assertions.assertTrue(listings.get(0).getQuantityQuintals() > 0);
        Assertions.assertTrue(listings.get(0).getPricePerQuintal() > 0);
    }

    @Test
    @DisplayName("5. Verify Administrator Machinery Fleet Monitoring")
    void testAdministratorMachineryMonitoring() {
        List<Machinery> machineries = machineryRepository.findAllByOrderByEquipmentCodeAsc();
        Assertions.assertFalse(machineries.isEmpty());

        long inUse = machineryRepository.countByStatus(MachineryStatus.IN_USE);
        long available = machineryRepository.countByStatus(MachineryStatus.AVAILABLE);
        long maintenance = machineryRepository.countByStatus(MachineryStatus.UNDER_MAINTENANCE);

        Assertions.assertTrue(inUse >= 1, "Must have active machines in use");
        Assertions.assertTrue(available >= 1, "Must have available machines in depot");
        Assertions.assertTrue(maintenance >= 1, "Must have machines under maintenance");
    }

    @Test
    @DisplayName("6. Verify Multi-Lingual Indian Farmer AI Advisory (Hindi, Marathi, Punjabi, Gujarati, etc.)")
    void testMultiLingualFarmerAI() {
        // Test Hindi
        FarmerAIResponseDTO hindiAdvice = farmerAIEngine.generateAdvice(
                FarmerAIRequestDTO.builder().language("hi").topic("PEST").cropName("गेहूं").build()
        );
        Assertions.assertNotNull(hindiAdvice);
        Assertions.assertEquals("hi", hindiAdvice.getLanguage());
        Assertions.assertTrue(hindiAdvice.getAdvice().contains("किसान भाई"));
        Assertions.assertFalse(hindiAdvice.getSimpleActionSteps().isEmpty());

        // Test Marathi
        FarmerAIResponseDTO marathiAdvice = farmerAIEngine.generateAdvice(
                FarmerAIRequestDTO.builder().language("mr").topic("WATER").cropName("कांदा").build()
        );
        Assertions.assertNotNull(marathiAdvice);
        Assertions.assertEquals("mr", marathiAdvice.getLanguage());
        Assertions.assertTrue(marathiAdvice.getAdvice().contains("शेतकरी"));

        // Test Punjabi
        FarmerAIResponseDTO punjabiAdvice = farmerAIEngine.generateAdvice(
                FarmerAIRequestDTO.builder().language("pa").topic("FERTILIZER").cropName("ਕਣਕ").build()
        );
        Assertions.assertNotNull(punjabiAdvice);
        Assertions.assertEquals("pa", punjabiAdvice.getLanguage());
        Assertions.assertTrue(punjabiAdvice.getAdvice().contains("ਕਿਸਾਨ ਵੀਰੋ"));

        // Test English
        FarmerAIResponseDTO englishAdvice = farmerAIEngine.generateAdvice(
                FarmerAIRequestDTO.builder().language("en").topic("GENERAL").cropName("Wheat").build()
        );
        Assertions.assertNotNull(englishAdvice);
        Assertions.assertEquals("en", englishAdvice.getLanguage());
        Assertions.assertTrue(englishAdvice.getAdvice().contains("Farmer Friend"));
    }

    @Test
    @DisplayName("7. Verify Input Sanitizer Protects Against XSS")
    void testInputSanitizerXSSProtection() {
        String dangerousInput = "<script>alert('hacked')</script>John Doe<img src='x' onerror='stealCookie()'>";
        String sanitized = inputSanitizer.sanitize(dangerousInput);
        Assertions.assertFalse(sanitized.contains("<script>"));
        Assertions.assertFalse(sanitized.contains("onerror"));
        Assertions.assertFalse(sanitized.contains("alert"));
        Assertions.assertTrue(sanitized.contains("John Doe"));
    }

    @Test
    @DisplayName("8. Verify Farmer Crop Marketplace Listings Query")
    void testFarmerCropMarketplaceListings() {
        User farmer = userRepository.findByEmail("farmer@agriself.com").orElse(null);
        Assertions.assertNotNull(farmer);
        List<CropMarketListing> farmerListings = cropMarketListingRepository.findBySellerFarmerOrderByListedAtDesc(farmer);
        Assertions.assertFalse(farmerListings.isEmpty());
        CropMarketListing first = farmerListings.get(0);
        Assertions.assertEquals("AVAILABLE", first.getStatus());
        Assertions.assertTrue(first.getPricePerQuintal() > 0);
    }

    @Test
    @DisplayName("9. Verify Native Pure-Java Agronomic Vision AI Engine Classifications")
    void testAgronomicVisionAIEngine() {
        // Test Early Blight
        var blightResult = diagnosticService.runJavaAgronomicVisionEngine(null, "Tomato", "Concentric dark brown rings on leaf");
        Assertions.assertEquals("Early Blight (Alternaria solani)", blightResult.getDiagnosisResult());
        Assertions.assertTrue(blightResult.getConfidence() > 90.0);
        Assertions.assertNotNull(blightResult.getBiologicalSolution());

        // Test Powdery Mildew
        var mildewResult = diagnosticService.runJavaAgronomicVisionEngine(null, "Cucumber", "White talcum powder coating");
        Assertions.assertEquals("Powdery Mildew (Erysiphe cichoracearum)", mildewResult.getDiagnosisResult());

        // Test Healthy Foliage
        var healthyResult = diagnosticService.runJavaAgronomicVisionEngine(null, "Rice", "Vibrant green healthy optimal leaf");
        Assertions.assertEquals("Healthy Crop (Optimal Vigour)", healthyResult.getDiagnosisResult());
        Assertions.assertEquals("OPTIMAL", healthyResult.getSeverity());
    }

    @Test
    @DisplayName("10. Verify Machinery Catalog & Rental Booking Engine")
    void testMachineryCatalogAndRental() {
        var catalog = rentalBookingEngine.getAvailableMachineryCatalog();
        Assertions.assertFalse(catalog.isEmpty());
        Assertions.assertTrue(catalog.stream().anyMatch(m -> m.get("name").toString().contains("John Deere")));
        Assertions.assertTrue(catalog.stream().anyMatch(m -> m.get("category").toString().equals("DRONE_SPRAYER")));
    }
}
