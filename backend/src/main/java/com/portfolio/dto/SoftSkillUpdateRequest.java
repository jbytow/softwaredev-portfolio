package com.portfolio.dto;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
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
    @Size(max = 255, message = "English name must not exceed 255 characters")
    @Pattern(regexp = "(?s).*\\S.*", message = "English name must not be blank")
    private String nameEn;
    @Size(max = 255, message = "Polish name must not exceed 255 characters")
    @Pattern(regexp = "(?s).*\\S.*", message = "Polish name must not be blank")
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
