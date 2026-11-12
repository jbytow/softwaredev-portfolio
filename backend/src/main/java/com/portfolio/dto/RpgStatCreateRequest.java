package com.portfolio.dto;

import jakarta.validation.constraints.Size;
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
    @Size(max = 10, message = "Attribute code must not exceed 10 characters")
    private String attr;

    @NotBlank(message = "English label is required")
    @Size(max = 255, message = "English label must not exceed 255 characters")
    private String labelEn;

    @NotBlank(message = "Polish label is required")
    @Size(max = 255, message = "Polish label must not exceed 255 characters")
    private String labelPl;

    private Integer level;
    private Integer maxLevel;
    private List<String> skills;
    private Integer displayOrder;
}
