package com.portfolio.dto;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RpgStatUpdateRequest {
    @Size(max = 10, message = "Attribute code must not exceed 10 characters")
    @Pattern(regexp = "(?s).*\\S.*", message = "Attribute code must not be blank")
    private String attr;
    @Size(max = 255, message = "English label must not exceed 255 characters")
    @Pattern(regexp = "(?s).*\\S.*", message = "English label must not be blank")
    private String labelEn;
    @Size(max = 255, message = "Polish label must not exceed 255 characters")
    @Pattern(regexp = "(?s).*\\S.*", message = "Polish label must not be blank")
    private String labelPl;
    private Integer level;
    private Integer maxLevel;
    private List<String> skills;
    private Integer displayOrder;
}
