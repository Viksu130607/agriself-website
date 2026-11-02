package com.agriself.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "mandi_prices")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MandiPrice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 80)
    private String commodity; // Wheat, Basmati Rice, Soybean, Cotton, Tomato, Red Onion, Mustard

    @Column(nullable = false, length = 80)
    private String state; // Maharashtra, Punjab, Madhya Pradesh, Gujarat, Haryana

    @Column(nullable = false, length = 100)
    private String mandiName; // APMC Nashik, Khanna Mandi, Indore Mandi

    @Column(nullable = false)
    private double minPricePerQuintal; // ₹

    @Column(nullable = false)
    private double maxPricePerQuintal; // ₹

    @Column(nullable = false)
    private double modalPricePerQuintal; // ₹ (Benchmark average)

    @Column(nullable = false, length = 20)
    private String priceTrend; // RISING, FALLING, STABLE

    @Column(nullable = false)
    private double percentageChange; // e.g. +3.2% or -1.5%

    @Builder.Default
    private LocalDate reportDate = LocalDate.now();
}
