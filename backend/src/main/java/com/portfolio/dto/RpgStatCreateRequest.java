package com.portfolio.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RpgStatCreateRequest {
    @NotBlank(message = "Attribute code is required")
    private String attr;

    @NotBlank(message = "English label is required")
    private String labelEn;

    @NotBlank(message = "Polish label is required")
    private String labelPl;

    private Integer level;
    private Integer maxLevel;
    private List<String> skills;
    private Integer displayOrder;
}
