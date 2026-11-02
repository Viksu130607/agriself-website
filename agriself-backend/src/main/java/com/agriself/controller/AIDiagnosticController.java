package com.agriself.controller;

import com.agriself.dto.DiagnosticResultDTO;
import com.agriself.model.AIDiagnosticRecord;
import com.agriself.model.User;
import com.agriself.security.UserDetailsImpl;
import com.agriself.service.AIDiagnosticService;
import com.agriself.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/diagnostic")
@RequiredArgsConstructor
@Slf4j
public class AIDiagnosticController {

    private final AIDiagnosticService diagnosticService;
    private final AuthService authService;

    @PostMapping(value = "/scan", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<DiagnosticResultDTO> scanLeaf(
            @RequestPart(value = "file", required = false) MultipartFile file,
            @RequestParam(value = "cropName", required = false) String cropName,
            @RequestParam(value = "symptom", required = false) String symptom,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        User user = userDetails != null ? userDetails.getUser() : null;
        DiagnosticResultDTO result = diagnosticService.diagnoseLeafImage(file, cropName, symptom, user);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/scan-preset")
    public ResponseEntity<DiagnosticResultDTO> scanPresetLeaf(
            @RequestBody Map<String, String> payload,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        String cropName = payload.getOrDefault("cropName", "Tomato");
        String symptom = payload.getOrDefault("symptom", "Dark concentric spots on leaves");
        User user = userDetails != null ? userDetails.getUser() : null;

        DiagnosticResultDTO result = diagnosticService.runJavaAgronomicVisionEngine(null, cropName, symptom);
        result.setImageUrl(payload.getOrDefault("sampleImage", "/sample_data/sample_early_blight_leaf.png"));
        return ResponseEntity.ok(result);
    }

    @GetMapping("/history")
    public ResponseEntity<List<AIDiagnosticRecord>> getHistory(
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        Long userId = userDetails != null ? userDetails.getUser().getId() : null;
        return ResponseEntity.ok(diagnosticService.getDiagnosticHistory(userId));
    }

    @GetMapping("/presets")
    public ResponseEntity<List<Map<String, String>>> getSamplePresets() {
        return ResponseEntity.ok(Arrays.asList(
                Map.of(
                        "id", "preset-1",
                        "name", "Tomato — Early Blight (Alternaria solani)",
                        "crop", "Tomato",
                        "symptoms", "Concentric dark brown rings with yellow chlorotic halos on lower foliage",
                        "sampleImage", "/sample_data/sample_early_blight_leaf.png"
                ),
                Map.of(
                        "id", "preset-2",
                        "name", "Potato — Late Blight (Phytophthora infestans)",
                        "crop", "Potato",
                        "symptoms", "Water-soaked lesions on leaf tips turning purplish-black with white mildew rim",
                        "sampleImage", "/sample_data/sample_late_blight_leaf.png"
                ),
                Map.of(
                        "id", "preset-3",
                        "name", "Cucurbits / Squash — Powdery Mildew",
                        "crop", "Cucumber",
                        "symptoms", "Talcum powder-like white fungal coating spreading over upper leaf surfaces",
                        "sampleImage", "/sample_data/sample_powdery_mildew_leaf.png"
                ),
                Map.of(
                        "id", "preset-4",
                        "name", "Wheat & Cereals — Nitrogen Deficiency",
                        "crop", "Wheat",
                        "symptoms", "Interveinal yellowing chlorosis on older leaves, stunted shoot elongation",
                        "sampleImage", "/sample_data/sample_nitrogen_deficiency_leaf.png"
                ),
                Map.of(
                        "id", "preset-5",
                        "name", "Healthy Crop Foliage (Optimal Vigour)",
                        "crop", "Rice / Cereal",
                        "symptoms", "Vibrant green leaves, uniform chlorophyll index, no lesion or blight spots",
                        "sampleImage", "/sample_data/sample_healthy_leaf.png"
                )
        ));
    }
}
