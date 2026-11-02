package com.agriself.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "crop_listings")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CropListing {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "farmer_id", nullable = false)
    private User farmer;

    @Column(nullable = false, length = 100)
    private String cropName;

    @Column(length = 100)
    private String variety;

    @Column(nullable = false)
    private Double quantity;

    @Column(nullable = false, length = 30)
    private String unit; // kg, quintal, ton, crates

    @Column(nullable = false)
    private Double pricePerUnit; // in INR (₹)

    @Column(name = "harvest_date")
    private LocalDate harvestDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private ListingStatus status = ListingStatus.ACTIVE;

    @Column(length = 500)
    private String description;

    @Column(length = 150)
    private String location;

    @Column(length = 255)
    private String qualityGrade; // Grade A+, Organic Certified, Grade B

    @Builder.Default
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (status == null) {
            status = ListingStatus.ACTIVE;
        }
    }
}
