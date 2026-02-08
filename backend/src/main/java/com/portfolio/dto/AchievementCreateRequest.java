package com.portfolio.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AchievementCreateRequest {
    @NotBlank(message = "English title is required")
    private String titleEn;

    @NotBlank(message = "Polish title is required")
    private String titlePl;

    private String descriptionEn;
    private String descriptionPl;
    private String icon;
    private String year;
    private Integer displayOrder;
}
