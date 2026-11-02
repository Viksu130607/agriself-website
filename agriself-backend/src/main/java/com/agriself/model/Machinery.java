package com.agriself.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "machineries")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Machinery {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false, length = 30)
    private String equipmentCode; // e.g. MCH-TR-101

    @Column(nullable = false, length = 120)
    private String name; // e.g. John Deere 5050D Autonomous Tractor

    @Column(nullable = false, length = 60)
    private String category; // Tractor, Drone Sprayer, Harvester, Sub-Surface Drip Pump, Rotavator

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private MachineryStatus status; // AVAILABLE, IN_USE, UNDER_MAINTENANCE

    @Column(length = 150)
    private String assignedLocation; // Field 1 - Nashik Valley

    @Column(length = 100)
    private String assignedToUser; // Ramesh Patel (Farmer)

    @Column(nullable = false)
    private double operatingHours;

    @Column(nullable = false)
    private int fuelOrBatteryPercent; // 0 - 100%

    private LocalDate lastServiceDate;

    private LocalDate nextServiceDate;

    @Column(length = 255)
    private String healthNotes;

    @Builder.Default
    private LocalDateTime updatedAt = LocalDateTime.now();

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
