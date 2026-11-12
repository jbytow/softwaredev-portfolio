package com.portfolio.dto;

import jakarta.validation.constraints.Size;
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
    @Size(max = 255, message = "English title must not exceed 255 characters")
    private String titleEn;

    @NotBlank(message = "Polish title is required")
    @Size(max = 255, message = "Polish title must not exceed 255 characters")
    private String titlePl;

    private String descriptionEn;
    private String descriptionPl;
    @Size(max = 50, message = "Icon must not exceed 50 characters")
    private String icon;
    @Size(max = 10, message = "Year must not exceed 10 characters")
    private String year;
    private Integer displayOrder;
}
