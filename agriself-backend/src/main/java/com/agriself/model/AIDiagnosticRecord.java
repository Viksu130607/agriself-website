package com.agriself.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "ai_diagnostic_records")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AIDiagnosticRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(nullable = false, length = 100)
    private String cropName;

    @Column(length = 300)
    private String symptomDescription;

    @Column(length = 300)
    private String imageUrl;

    @Column(nullable = false, length = 150)
    private String diagnosisResult;

    @Column(nullable = false)
    private Double confidence; // Percentage e.g. 97.4%

    @Column(length = 50)
    private String severity; // OPTIMAL, LOW, MODERATE, HIGH, SEVERE

    @Column(length = 1000)
    private String immediateAction;

    @Column(length = 1000)
    private String biologicalSolution;

    @Column(length = 1000)
    private String chemicalOrSmartProtocol;

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
