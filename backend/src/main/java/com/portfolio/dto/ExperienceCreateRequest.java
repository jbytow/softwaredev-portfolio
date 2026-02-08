package com.portfolio.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
public class ExperienceCreateRequest {

    @NotBlank(message = "English title is required")
    private String titleEn;

    @NotBlank(message = "Polish title is required")
    private String titlePl;

    @NotBlank(message = "Company is required")
    private String company;

    @NotNull(message = "Start date is required")
    private LocalDate startDate;

    private LocalDate endDate;
    private String descriptionEn;
    private String descriptionPl;
    private List<String> achievementsEn;
    private List<String> achievementsPl;
    private Integer displayOrder;
}
