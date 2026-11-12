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
public class InterestUpdateRequest {
    @Size(max = 255, message = "English title must not exceed 255 characters")
    @Pattern(regexp = "(?s).*\\S.*", message = "English title must not be blank")
    private String titleEn;
    @Size(max = 255, message = "Polish title must not exceed 255 characters")
    @Pattern(regexp = "(?s).*\\S.*", message = "Polish title must not be blank")
    private String titlePl;
    @Size(max = 500, message = "Image 1 must not exceed 500 characters")
    private String image1;
    @Size(max = 500, message = "Image 2 must not exceed 500 characters")
    private String image2;
    @Size(max = 500, message = "Image 3 must not exceed 500 characters")
    private String image3;
    private Integer displayOrder;
}
