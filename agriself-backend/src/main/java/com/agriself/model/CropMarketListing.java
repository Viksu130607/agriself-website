package com.agriself.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "crop_market_listings")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CropMarketListing {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seller_farmer_id", nullable = false)
    private User sellerFarmer;

    @Column(nullable = false, length = 80)
    private String cropName; // e.g. Sharbati Wheat, Basmati 1121, Red Onions, Soybean

    @Column(length = 80)
    private String variety;

    @Column(nullable = false)
    private double quantityQuintals; // Quantity available for sale

    @Column(nullable = false, length = 80)
    private String qualityGrade; // Grade A+, Grade A, Organic Certified

    @Column(nullable = false)
    private double pricePerQuintal; // ₹ per quintal

    @Column(nullable = false, length = 150)
    private String farmLocation; // Nashik APMC Hub / Punjab Farm Gate

    @Column(length = 30)
    private String contactNumber;

    @Column(nullable = false, length = 30)
    private String status; // AVAILABLE, UNDER_NEGOTIATION, SOLD

    @Column(length = 255)
    private String qualityNotes; // Moisture content 11%, zero pesticide residue

    @Builder.Default
    private LocalDateTime listedAt = LocalDateTime.now();
}
