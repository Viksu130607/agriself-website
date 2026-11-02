package com.agriself.dto;

import lombok.*;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FarmerAIResponseDTO {

    private String language;
    private String languageName;
    private String warningTitle;
    private String warningBadge; // URGENT, ADVISORY, NORMAL
    private String advice;
    private List<String> simpleActionSteps;
    private String weatherNote;
    private String organicSolution;
}
