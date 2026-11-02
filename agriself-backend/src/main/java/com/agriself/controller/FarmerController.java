package com.agriself.controller;

import com.agriself.dto.DiagnosticResultDTO;
import com.agriself.dto.EquipmentBookingDTO;
import com.agriself.dto.FarmerAIRequestDTO;
import com.agriself.dto.FarmerAIResponseDTO;
import com.agriself.model.*;
import com.agriself.repository.AuditLogRepository;
import com.agriself.repository.CropMarketListingRepository;
import com.agriself.repository.FarmFieldRepository;
import com.agriself.repository.UserRepository;
import com.agriself.security.InputSanitizer;
import com.agriself.service.AIDiagnosticService;
import com.agriself.service.FarmerAIEngine;
import com.agriself.service.RentalBookingEngine;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/farmer")
@PreAuthorize("hasAuthority('ROLE_FARMER')")
@Slf4j
public class FarmerController {

    private final FarmFieldRepository farmFieldRepository;
    private final UserRepository userRepository;
    private final FarmerAIEngine farmerAIEngine;
    private final InputSanitizer inputSanitizer;
    private final CropMarketListingRepository cropMarketListingRepository;
    private final RentalBookingEngine rentalBookingEngine;
    private final AuditLogRepository auditLogRepository;
    private final AIDiagnosticService diagnosticService;

    public FarmerController(FarmFieldRepository farmFieldRepository,
                            UserRepository userRepository,
                            FarmerAIEngine farmerAIEngine,
                            InputSanitizer inputSanitizer,
                            CropMarketListingRepository cropMarketListingRepository,
                            RentalBookingEngine rentalBookingEngine,
                            AuditLogRepository auditLogRepository,
                            AIDiagnosticService diagnosticService) {
        this.farmFieldRepository = farmFieldRepository;
        this.userRepository = userRepository;
        this.farmerAIEngine = farmerAIEngine;
        this.inputSanitizer = inputSanitizer;
        this.cropMarketListingRepository = cropMarketListingRepository;
        this.rentalBookingEngine = rentalBookingEngine;
        this.auditLogRepository = auditLogRepository;
        this.diagnosticService = diagnosticService;
    }

    @GetMapping("/dashboard")
    public String showFarmerDashboard(Authentication auth, Model model) {
        String email = auth.getName();
        User farmer = userRepository.findByEmail(email).orElse(null);

        List<FarmField> fields = farmFieldRepository.findByFarmerEmail(email);
        if (fields.isEmpty() && farmer != null) {
            // Provide a default sample field for newly registered farmers
            FarmField demoField = FarmField.builder()
                    .farmer(farmer)
                    .fieldName("Main AgriSelf Field Plot")
                    .sizeInAcres(8.5)
                    .soilType("Black Cotton Soil")
                    .currentSoilMoisture(44.2)
                    .soilPh(6.7)
                    .nitrogenPpm(138)
                    .phosphorusPpm(42)
                    .potassiumPpm(190)
                    .currentCrop("Sharbati Wheat (गेहूं)")
                    .cropGrowthStage("Tillering (कल्ले फूटने की अवस्था)")
                    .waterRequirementLiters(1400.0)
                    .irrigationSchedule("Tomorrow 06:00 AM (Drip 45 Mins)")
                    .fertilizerRecommendation("Urea 25kg/acre + Zinc 5kg due in 2 days")
                    .overallCropHealth("Optimal - Healthy Vigour")
                    .lastTelemetrySync(LocalDateTime.now())
                    .build();
            demoField = farmFieldRepository.save(demoField);
            fields = List.of(demoField);
        }

        model.addAttribute("farmer", farmer);
        model.addAttribute("fields", fields);
        model.addAttribute("primaryField", !fields.isEmpty() ? fields.get(0) : null);

        // Machinery rental catalog & farmer's bookings
        model.addAttribute("machineryCatalog", rentalBookingEngine.getAvailableMachineryCatalog());
        model.addAttribute("myBookings", farmer != null ? rentalBookingEngine.getUserBookings(farmer.getId()) : List.of());

        // Farmer's crops on sale in marketplace
        model.addAttribute("myListings", farmer != null ? cropMarketListingRepository.findBySellerFarmerOrderByListedAtDesc(farmer) : List.of());

        // Initial default AI Advice in Hindi (farmer language)
        FarmerAIResponseDTO defaultAdvice = farmerAIEngine.generateAdvice(
                FarmerAIRequestDTO.builder().language("hi").topic("GENERAL").cropName("गेहूं").build()
        );
        model.addAttribute("initialAdvice", defaultAdvice);

        return "farmer/dashboard";
    }

    @PostMapping("/crop-listing")
    public String publishCropListing(
            @RequestParam("cropName") String cropName,
            @RequestParam(value = "variety", required = false) String variety,
            @RequestParam("quantityQuintals") double quantityQuintals,
            @RequestParam("pricePerQuintal") double pricePerQuintal,
            @RequestParam("qualityGrade") String qualityGrade,
            @RequestParam("farmLocation") String farmLocation,
            @RequestParam(value = "qualityNotes", required = false) String qualityNotes,
            Authentication auth,
            RedirectAttributes redirectAttributes) {

        String email = auth.getName();
        User farmer = userRepository.findByEmail(email).orElse(null);
        if (farmer == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Farmer account not found.");
            return "redirect:/farmer/dashboard";
        }

        if (quantityQuintals <= 0 || pricePerQuintal <= 0) {
            redirectAttributes.addFlashAttribute("errorMessage", "Quantity and Price must be positive values.");
            return "redirect:/farmer/dashboard";
        }

        CropMarketListing listing = CropMarketListing.builder()
                .sellerFarmer(farmer)
                .cropName(inputSanitizer.sanitize(cropName))
                .variety(variety != null ? inputSanitizer.sanitize(variety) : "Farm Gate Selection")
                .quantityQuintals(quantityQuintals)
                .pricePerQuintal(pricePerQuintal)
                .qualityGrade(inputSanitizer.sanitize(qualityGrade))
                .farmLocation(inputSanitizer.sanitize(farmLocation))
                .contactNumber(farmer.getPhoneNumber() != null ? farmer.getPhoneNumber() : "9876543210")
                .status("AVAILABLE")
                .qualityNotes(qualityNotes != null && !qualityNotes.isBlank() ? inputSanitizer.sanitize(qualityNotes) : "Certified farm harvest, low moisture.")
                .listedAt(LocalDateTime.now())
                .build();
        cropMarketListingRepository.save(listing);

        AuditLog audit = AuditLog.builder()
                .eventType("CROP_LISTED_FOR_SALE")
                .username(email)
                .details(String.format("Farmer listed %.1f Q of %s at ₹%.2f/Q in marketplace", quantityQuintals, cropName, pricePerQuintal))
                .build();
        auditLogRepository.save(audit);

        redirectAttributes.addFlashAttribute("successMessage",
                String.format("✅ Success! '%s' (%.1f Quintals at ₹%,.2f/Q) is now live in the AgriSelf Mandi Marketplace for buyers to purchase.",
                        cropName, quantityQuintals, pricePerQuintal));
        return "redirect:/farmer/dashboard";
    }

    @PostMapping("/rentals/book")
    public String bookMachinery(
            @RequestParam("equipmentName") String equipmentName,
            @RequestParam("category") String category,
            @RequestParam("hourlyRate") double hourlyRate,
            @RequestParam("hours") double hours,
            @RequestParam("farmLocation") String farmLocation,
            @RequestParam(value = "notes", required = false) String notes,
            Authentication auth,
            RedirectAttributes redirectAttributes) {

        String email = auth.getName();
        User farmer = userRepository.findByEmail(email).orElse(null);
        if (farmer == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Farmer account not found.");
            return "redirect:/farmer/dashboard";
        }

        if (hours <= 0) {
            redirectAttributes.addFlashAttribute("errorMessage", "Rental duration must be at least 1 hour.");
            return "redirect:/farmer/dashboard";
        }

        LocalDateTime startTime = LocalDateTime.now().plusHours(1);
        LocalDateTime endTime = startTime.plusHours((long) Math.max(1, hours));

        EquipmentBookingDTO dto = EquipmentBookingDTO.builder()
                .equipmentName(equipmentName)
                .category(category)
                .startTime(startTime)
                .endTime(endTime)
                .hourlyRate(hourlyRate)
                .farmLocation(farmLocation != null && !farmLocation.isBlank() ? inputSanitizer.sanitize(farmLocation) : farmer.getLocation())
                .notes(notes != null && !notes.isBlank() ? inputSanitizer.sanitize(notes) : "Farmer online self-booking")
                .build();

        try {
            EquipmentBooking booking = rentalBookingEngine.createBooking(dto, farmer);
            AuditLog audit = AuditLog.builder()
                    .eventType("EQUIPMENT_BOOKED")
                    .username(email)
                    .details(String.format("Booked %s for %.1f hours (Total: ₹%.2f)", equipmentName, hours, booking.getTotalPrice()))
                    .build();
            auditLogRepository.save(audit);

            redirectAttributes.addFlashAttribute("successMessage",
                    String.format("🚜 Equipment '%s' booked successfully! Reserved for %.0f hours (Total: ₹%,.2f). Depot team will coordinate dispatch.",
                            equipmentName, hours, booking.getTotalPrice()));
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Booking failed: " + e.getMessage());
        }
        return "redirect:/farmer/dashboard";
    }

    @PostMapping("/api/ai-advice")
    @ResponseBody
    public ResponseEntity<FarmerAIResponseDTO> getFarmerAIAdvice(@Valid @RequestBody FarmerAIRequestDTO request) {
        // Sanitize incoming prompt if any
        if (request.getQuestion() != null) {
            request.setQuestion(inputSanitizer.sanitize(request.getQuestion()));
        }
        FarmerAIResponseDTO response = farmerAIEngine.generateAdvice(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/api/scan-preset-leaf")
    @ResponseBody
    public ResponseEntity<DiagnosticResultDTO> scanPresetLeaf(@RequestBody Map<String, String> payload, Authentication auth) {
        String cropName = payload.getOrDefault("cropName", "Tomato");
        String symptom = payload.getOrDefault("symptom", "Concentric dark brown rings with chlorotic halo");
        String sampleImage = payload.getOrDefault("sampleImage", "/sample_data/sample_early_blight_leaf.png");

        DiagnosticResultDTO result = diagnosticService.runJavaAgronomicVisionEngine(null, cropName, symptom);
        result.setImageUrl(sampleImage);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/api/toggle-irrigation/{fieldId}")
    @ResponseBody
    public ResponseEntity<?> toggleDripIrrigation(@PathVariable Long fieldId, Authentication auth) {
        FarmField field = farmFieldRepository.findById(fieldId).orElse(null);
        if (field == null || !field.getFarmer().getEmail().equals(auth.getName())) {
            return ResponseEntity.badRequest().body("Field not found or unauthorized");
        }

        // Simulate moisture increase and schedule update
        field.setCurrentSoilMoisture(Math.min(85.0, field.getCurrentSoilMoisture() + 15.0));
        field.setWaterRequirementLiters(Math.max(0.0, field.getWaterRequirementLiters() - 600.0));
        field.setIrrigationSchedule("Active Now (Smart Drip Flowing: 35 mins remaining)");
        field.setLastTelemetrySync(LocalDateTime.now());
        farmFieldRepository.save(field);

        return ResponseEntity.ok(field);
    }
}
