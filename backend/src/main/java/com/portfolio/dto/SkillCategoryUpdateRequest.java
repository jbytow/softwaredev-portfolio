package com.portfolio.dto;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SkillCategoryUpdateRequest {
    @Size(max = 255, message = "English name must not exceed 255 characters")
    @Pattern(regexp = "(?s).*\\S.*", message = "English name must not be blank")
    private String nameEn;
    @Size(max = 255, message = "Polish name must not exceed 255 characters")
    @Pattern(regexp = "(?s).*\\S.*", message = "Polish name must not be blank")
    private String namePl;
    private Integer displayOrder;
}
