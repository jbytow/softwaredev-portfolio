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
public class SkillCategoryWithSkillsDto {
    private UUID id;
    private String name;
    private String nameEn;
    private String namePl;
    private Integer displayOrder;
    private List<SoftSkillDto> skills;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
}
