package com.portfolio.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InterestUpdateRequest {
    private String titleEn;
    private String titlePl;
    private String image1;
    private String image2;
    private String image3;
    private Integer displayOrder;
}
