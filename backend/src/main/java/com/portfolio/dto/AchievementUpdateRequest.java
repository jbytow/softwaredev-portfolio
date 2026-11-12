package com.portfolio.dto;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AchievementUpdateRequest {
    @Size(max = 255, message = "English title must not exceed 255 characters")
    @Pattern(regexp = "(?s).*\\S.*", message = "English title must not be blank")
    private String titleEn;
    @Size(max = 255, message = "Polish title must not exceed 255 characters")
    @Pattern(regexp = "(?s).*\\S.*", message = "Polish title must not be blank")
    private String titlePl;
    private String descriptionEn;
    private String descriptionPl;
    @Size(max = 50, message = "Icon must not exceed 50 characters")
    private String icon;
    @Size(max = 10, message = "Year must not exceed 10 characters")
    private String year;
    private Integer displayOrder;
}
