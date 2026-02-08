package com.portfolio.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExperienceUpdateRequest {
    private String titleEn;
    private String titlePl;
    private String company;
    private LocalDate startDate;
    private LocalDate endDate;
    private String descriptionEn;
    private String descriptionPl;
    private List<String> achievementsEn;
    private List<String> achievementsPl;
    private Integer displayOrder;
}
