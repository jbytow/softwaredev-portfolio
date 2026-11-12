package com.portfolio.dto;

import jakarta.validation.constraints.Size;
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
    @Size(max = 255, message = "English name must not exceed 255 characters")
    private String nameEn;

    @NotBlank(message = "Polish name is required")
    @Size(max = 255, message = "Polish name must not exceed 255 characters")
    private String namePl;

    private Integer displayOrder;
}
