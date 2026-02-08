package com.portfolio.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SkillCategoryCreateRequest {

    @NotBlank(message = "English name is required")
    private String nameEn;

    @NotBlank(message = "Polish name is required")
    private String namePl;

    private Integer displayOrder;
}
