package com.portfolio.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StatItemDto {
    private String icon;
    private String value;
    private String label;
    private String labelEn;
    private String labelPl;
}
