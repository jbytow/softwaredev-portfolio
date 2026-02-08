package com.portfolio.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AchievementUpdateRequest {
    private String titleEn;
    private String titlePl;
    private String descriptionEn;
    private String descriptionPl;
    private String icon;
    private String year;
    private Integer displayOrder;
}
