package com.agriself.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "farms")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Farm {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "farmer_id", nullable = false)
    private User farmer;

    @Column(nullable = false, length = 120)
    private String farmName;

    @Column(nullable = false, length = 150)
    private String location;

    @Column(nullable = false)
    private Double sizeInAcres;

    @Column(nullable = false, length = 50)
    private String soilType; // Black Cotton, Alluvial, Red Loam, Clayey, Sandy Loam

    @Column(length = 50)
    private String irrigationType; // Drip, Sprinkler, Flood, Rainfed

    @Column(length = 150)
    private String primaryCrops; // e.g. Wheat, Basmati Rice, Mustard

    @Column(nullable = false)
    @Builder.Default
    private Double currentSoilMoisture = 68.5; // Real-time IoT sensor telemetry (%)

    @Column(nullable = false)
    @Builder.Default
    private Double soilPh = 6.8; // IoT pH telemetry

    @Column(nullable = false)
    @Builder.Default
    private Integer soilNitrogen = 145; // ppm

    @Column(nullable = false)
    @Builder.Default
    private Integer soilPhosphorus = 42; // ppm

    @Column(nullable = false)
    @Builder.Default
    private Integer soilPotassium = 190; // ppm

    @Builder.Default
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }
}
