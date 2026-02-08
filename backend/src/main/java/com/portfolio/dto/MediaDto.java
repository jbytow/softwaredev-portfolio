package com.portfolio.dto;

import com.portfolio.entity.MediaType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MediaDto {
    private UUID id;
    private UUID postId;
    private MediaType type;
    private String filename;
    private String originalName;
    private String mimeType;
    private Long size;
    private String url;
    private String altText;
    private String altTextEn;
    private String altTextPl;
    private Integer displayOrder;
    private String videoUrl;
    private OffsetDateTime createdAt;
}
