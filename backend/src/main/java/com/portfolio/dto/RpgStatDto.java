package com.portfolio.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RpgStatDto {
    private UUID id;
    private String attr;
    private String label;
    private String labelEn;
    private String labelPl;
    private Integer level;
    private Integer maxLevel;
    private List<String> skills;
    private Integer displayOrder;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
}
