package com.portfolio.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExperienceDto {
    private UUID id;
    private String title;
    private String titleEn;
    private String titlePl;
    private String company;
    private LocalDate startDate;
    private LocalDate endDate;
    private String description;
    private String descriptionEn;
    private String descriptionPl;
    private List<String> achievements;
    private List<String> achievementsEn;
    private List<String> achievementsPl;
    private Integer displayOrder;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
}
