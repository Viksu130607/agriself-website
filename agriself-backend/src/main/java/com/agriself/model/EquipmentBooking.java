package com.agriself.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "equipment_bookings", indexes = {
    @Index(name = "idx_equipment_times", columnList = "equipmentName, startTime, endTime")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EquipmentBooking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, length = 120)
    private String equipmentName;

    @Column(nullable = false, length = 50)
    private String category; // TRACTOR, HARVESTER, ROTAVATOR, DRONE_SPRAYER

    @Column(name = "start_time", nullable = false)
    private LocalDateTime startTime;

    @Column(name = "end_time", nullable = false)
    private LocalDateTime endTime;

    @Column(nullable = false)
    private Double hourlyRate; // in INR (₹/hr)

    @Column(nullable = false)
    private Double totalPrice; // in INR (₹)

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private BookingStatus status = BookingStatus.CONFIRMED;

    @Column(length = 200)
    private String farmLocation;

    @Column(length = 300)
    private String notes;

    @Builder.Default
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (status == null) {
            status = BookingStatus.CONFIRMED;
        }
    }
}
