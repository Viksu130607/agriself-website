package com.agriself.service;

import com.agriself.dto.EquipmentBookingDTO;
import com.agriself.model.BookingStatus;
import com.agriself.model.EquipmentBooking;
import com.agriself.model.User;
import com.agriself.repository.EquipmentBookingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class RentalBookingEngine {

    private final EquipmentBookingRepository bookingRepository;

    public List<Map<String, Object>> getAvailableMachineryCatalog() {
        return Arrays.asList(
                Map.of(
                        "name", "John Deere 5050D 4WD Tractor",
                        "category", "TRACTOR",
                        "hp", "50 HP",
                        "hourlyRate", 650.0,
                        "location", "Nashik Central Agri Hub",
                        "features", "Power steering, Dual Clutch, High Torque for heavy tilling",
                        "status", "AVAILABLE"
                ),
                Map.of(
                        "name", "CLAAS CROP TIGER 30 Combine Harvester",
                        "category", "HARVESTER",
                        "hp", "76 HP",
                        "hourlyRate", 1850.0,
                        "location", "Niphad Cooperative Yard",
                        "features", "Multi-crop threshing, Multi-terrain track, Minimal grain loss",
                        "status", "AVAILABLE"
                ),
                Map.of(
                        "name", "Shaktiman Semi-Champion Rotavator 6ft",
                        "category", "ROTAVATOR",
                        "hp", "45-60 HP Req.",
                        "hourlyRate", 450.0,
                        "location", "Dindori Agri Equipment Depot",
                        "features", "Boron steel blades, Multi-speed gearbox, Fine seedbed prep",
                        "status", "AVAILABLE"
                ),
                Map.of(
                        "name", "DJI Agras T40 Precision Drone Sprayer",
                        "category", "DRONE_SPRAYER",
                        "hp", "Dual Atomized Spraying",
                        "hourlyRate", 1200.0,
                        "location", "AgriSelf Regional Drone Hub",
                        "features", "40L payload, Active phased array radar, 50 acres/hour coverage",
                        "status", "AVAILABLE"
                ),
                Map.of(
                        "name", "Mahindra 575 DI Sarpanch Tractor",
                        "category", "TRACTOR",
                        "hp", "47 HP",
                        "hourlyRate", 580.0,
                        "location", "Sinnar Rural Center",
                        "features", "High fuel efficiency, Heavy hydraulic lift, Rugged build",
                        "status", "AVAILABLE"
                )
        );
    }

    /**
     * Executes transactional time-slot locking to eliminate double bookings.
     * Uses SERIALIZABLE isolation level and synchronized check.
     */
    @Transactional(isolation = Isolation.SERIALIZABLE)
    public synchronized EquipmentBooking createBooking(EquipmentBookingDTO dto, User user) {
        if (dto.getStartTime().isAfter(dto.getEndTime())) {
            throw new IllegalArgumentException("Start time must precede end time");
        }
        if (dto.getStartTime().isBefore(LocalDateTime.now().minusMinutes(5))) {
            throw new IllegalArgumentException("Booking start time cannot be in the past");
        }

        // Check for double booking collision
        List<EquipmentBooking> overlaps = bookingRepository.findOverlappingBookings(
                dto.getEquipmentName(), dto.getStartTime(), dto.getEndTime()
        );

        if (!overlaps.isEmpty()) {
            EquipmentBooking collision = overlaps.get(0);
            throw new IllegalStateException(String.format(
                    "Machinery '%s' is already booked from %s to %s. Please select a different time slot.",
                    dto.getEquipmentName(), collision.getStartTime(), collision.getEndTime()
            ));
        }

        long hours = Math.max(1, Duration.between(dto.getStartTime(), dto.getEndTime()).toHours());
        double totalPrice = hours * dto.getHourlyRate();

        EquipmentBooking booking = EquipmentBooking.builder()
                .user(user)
                .equipmentName(dto.getEquipmentName())
                .category(dto.getCategory())
                .startTime(dto.getStartTime())
                .endTime(dto.getEndTime())
                .hourlyRate(dto.getHourlyRate())
                .totalPrice(totalPrice)
                .status(BookingStatus.CONFIRMED)
                .farmLocation(dto.getFarmLocation() != null ? dto.getFarmLocation() : user.getLocation())
                .notes(dto.getNotes())
                .build();

        EquipmentBooking saved = bookingRepository.save(booking);
        log.info("Successfully reserved equipment {} for farmer {} from {} to {}",
                saved.getEquipmentName(), user.getFullName(), saved.getStartTime(), saved.getEndTime());
        return saved;
    }

    @Transactional(readOnly = true)
    public List<EquipmentBooking> getUserBookings(Long userId) {
        return bookingRepository.findByUserIdOrderByStartTimeDesc(userId);
    }

    @Transactional(readOnly = true)
    public List<EquipmentBooking> getFutureUserBookings(Long userId) {
        return bookingRepository.findByUserIdAndStartTimeAfter(userId, LocalDateTime.now());
    }

    @Transactional(readOnly = true)
    public List<EquipmentBooking> getAllBookings() {
        return bookingRepository.findAll();
    }

    @Transactional
    public EquipmentBooking cancelBooking(Long bookingId, Long userId) {
        EquipmentBooking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new IllegalArgumentException("Booking not found: " + bookingId));

        if (!booking.getUser().getId().equals(userId)) {
            throw new SecurityException("Unauthorized to cancel this booking");
        }

        booking.setStatus(BookingStatus.CANCELLED);
        return bookingRepository.save(booking);
    }
}
