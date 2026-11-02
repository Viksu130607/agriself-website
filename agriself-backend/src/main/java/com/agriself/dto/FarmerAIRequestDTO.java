package com.agriself.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FarmerAIRequestDTO {

    @NotBlank(message = "Language code is required")
    @Size(max = 20)
    private String language; // hi, mr, pa, gu, te, ta, bn, en

    @Size(max = 100)
    private String cropName; // e.g. Wheat, Rice, Mustard, Cotton, Tomato, Onion

    @Size(max = 50)
    private String topic; // PEST, WATER, FERTILIZER, WEATHER, GENERAL

    @Size(max = 500, message = "Question must not exceed 500 characters")
    private String question; // Custom farmer question
}
