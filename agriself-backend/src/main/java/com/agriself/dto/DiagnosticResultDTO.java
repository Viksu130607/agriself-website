package com.agriself.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DiagnosticResultDTO {

    private Long id;
    private String cropName;
    private String diagnosisResult;
    private Double confidence;
    private String severity;
    private String immediateAction;
    private String biologicalSolution;
    private String chemicalOrSmartProtocol;
    private String imageUrl;
    private LocalDateTime createdAt;
}
