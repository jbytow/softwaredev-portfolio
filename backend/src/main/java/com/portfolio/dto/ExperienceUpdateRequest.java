package com.portfolio.dto;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExperienceUpdateRequest {
    @Size(max = 255, message = "English title must not exceed 255 characters")
    @Pattern(regexp = "(?s).*\\S.*", message = "English title must not be blank")
    private String titleEn;
    @Size(max = 255, message = "Polish title must not exceed 255 characters")
    @Pattern(regexp = "(?s).*\\S.*", message = "Polish title must not be blank")
    private String titlePl;
    @Size(max = 255, message = "Company must not exceed 255 characters")
    @Pattern(regexp = "(?s).*\\S.*", message = "Company must not be blank")
    private String company;
    private LocalDate startDate;
    private LocalDate endDate;
    private String descriptionEn;
    private String descriptionPl;
    private List<String> achievementsEn;
    private List<String> achievementsPl;
    private Integer displayOrder;
}
