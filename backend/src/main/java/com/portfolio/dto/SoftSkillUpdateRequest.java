package com.portfolio.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SoftSkillUpdateRequest {
    private String nameEn;
    private String namePl;
    private String descriptionEn;
    private String descriptionPl;
    private String professionalUsageEn;
    private String professionalUsagePl;
    private String icon;
    private Integer displayOrder;
    private UUID categoryId;
}
