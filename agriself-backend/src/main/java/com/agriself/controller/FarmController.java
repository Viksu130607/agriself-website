package com.agriself.controller;

import com.agriself.model.Farm;
import com.agriself.model.User;
import com.agriself.security.UserDetailsImpl;
import com.agriself.service.AuthService;
import com.agriself.service.FarmService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/farms")
@RequiredArgsConstructor
public class FarmController {

    private final FarmService farmService;
    private final AuthService authService;

    @GetMapping
    public ResponseEntity<List<Farm>> getAllFarms() {
        return ResponseEntity.ok(farmService.getAllFarms());
    }

    @GetMapping("/my-farms")
    public ResponseEntity<List<Farm>> getMyFarms(@AuthenticationPrincipal UserDetailsImpl userDetails) {
        Long farmerId = userDetails != null ? userDetails.getUser().getId() : 1L;
        return ResponseEntity.ok(farmService.getFarmsByFarmerId(farmerId));
    }

    @PostMapping
    public ResponseEntity<Farm> registerFarm(
            @RequestBody Farm farm,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        User farmer = userDetails != null ? userDetails.getUser() : authService.getUserByPhoneNumber("9876543210");
        return ResponseEntity.status(201).body(farmService.registerFarm(farm, farmer));
    }

    @GetMapping("/soil-distribution")
    public ResponseEntity<Map<String, Long>> getSoilDistribution() {
        return ResponseEntity.ok(farmService.getSoilTypeDistribution());
    }
}
