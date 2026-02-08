package com.portfolio.dto;

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
    private String attr;
    private String labelEn;
    private String labelPl;
    private Integer level;
    private Integer maxLevel;
    private List<String> skills;
    private Integer displayOrder;
}
