package com.portfolio.dto;

import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SoftSkillCreateRequest {

    @NotBlank(message = "English name is required")
    @Size(max = 255, message = "English name must not exceed 255 characters")
    private String nameEn;

    @NotBlank(message = "Polish name is required")
    @Size(max = 255, message = "Polish name must not exceed 255 characters")
    private String namePl;

    private String descriptionEn;
    private String descriptionPl;
    private String professionalUsageEn;
    private String professionalUsagePl;
    @Size(max = 100, message = "Icon must not exceed 100 characters")
    private String icon;
    private Integer level;
    private Integer displayOrder;
    private UUID categoryId;
}
