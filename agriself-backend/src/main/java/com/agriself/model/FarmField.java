package com.agriself.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "farm_fields")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FarmField {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "farmer_id", nullable = false)
    private User farmer;

    @Column(nullable = false, length = 100)
    private String fieldName; // e.g. North Plot - Wheat Zone

    @Column(nullable = false)
    private double sizeInAcres;

    @Column(length = 60)
    private String soilType; // Black Cotton, Alluvial, Red Loam

    @Column(nullable = false)
    private double currentSoilMoisture; // e.g. 42.5%

    @Column(nullable = false)
    private double soilPh; // e.g. 6.8

    @Column(nullable = false)
    private int nitrogenPpm; // e.g. 140

    @Column(nullable = false)
    private int phosphorusPpm; // e.g. 45

    @Column(nullable = false)
    private int potassiumPpm; // e.g. 195

    @Column(length = 80)
    private String currentCrop; // Sharbati Wheat

    @Column(length = 80)
    private String cropGrowthStage; // Tillering / Vegetative

    @Column(nullable = false)
    private double waterRequirementLiters; // Required today

    @Column(length = 150)
    private String irrigationSchedule; // 06:00 AM (Drip 45 mins)

    @Column(length = 200)
    private String fertilizerRecommendation; // Urea 25kg/acre due in 3 days

    @Column(length = 50)
    private String overallCropHealth; // Optimal, Moderate Stress, Urgent Attention

    @Builder.Default
    private LocalDateTime lastTelemetrySync = LocalDateTime.now();
}
