package com.agriself.controller;

import com.agriself.dto.EquipmentBookingDTO;
import com.agriself.model.EquipmentBooking;
import com.agriself.model.User;
import com.agriself.security.UserDetailsImpl;
import com.agriself.service.AuthService;
import com.agriself.service.RentalBookingEngine;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/rentals")
@RequiredArgsConstructor
public class RentalController {

    private final RentalBookingEngine rentalBookingEngine;
    private final AuthService authService;

    @GetMapping("/catalog")
    public ResponseEntity<List<Map<String, Object>>> getCatalog() {
        return ResponseEntity.ok(rentalBookingEngine.getAvailableMachineryCatalog());
    }

    @PostMapping("/book")
    public ResponseEntity<EquipmentBooking> bookEquipment(
            @Valid @RequestBody EquipmentBookingDTO dto,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        User user = userDetails != null ? userDetails.getUser() :
                authService.getUserByPhoneNumber("9876543210"); // fallback to default demo farmer
        EquipmentBooking booking = rentalBookingEngine.createBooking(dto, user);
        return ResponseEntity.status(201).body(booking);
    }

    @GetMapping("/my-bookings")
    public ResponseEntity<List<EquipmentBooking>> getMyBookings(@AuthenticationPrincipal UserDetailsImpl userDetails) {
        Long userId = userDetails != null ? userDetails.getUser().getId() : 1L;
        return ResponseEntity.ok(rentalBookingEngine.getUserBookings(userId));
    }

    @GetMapping("/all-bookings")
    public ResponseEntity<List<EquipmentBooking>> getAllBookings() {
        return ResponseEntity.ok(rentalBookingEngine.getAllBookings());
    }

    @PostMapping("/cancel/{id}")
    public ResponseEntity<EquipmentBooking> cancelBooking(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        Long userId = userDetails != null ? userDetails.getUser().getId() : 1L;
        return ResponseEntity.ok(rentalBookingEngine.cancelBooking(id, userId));
    }
}
