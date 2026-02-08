package com.portfolio.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SoftSkillDto {
    private UUID id;
    private String name;
    private String nameEn;
    private String namePl;
    private String description;
    private String descriptionEn;
    private String descriptionPl;
    private String professionalUsage;
    private String professionalUsageEn;
    private String professionalUsagePl;
    private String icon;
    private Integer level;
    private Integer displayOrder;
    private UUID categoryId;
    private String categoryName;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
}
