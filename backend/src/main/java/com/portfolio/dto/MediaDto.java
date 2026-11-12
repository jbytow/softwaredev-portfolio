package com.portfolio.dto;

import com.portfolio.entity.Media;
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

    // Single mapping shared by the media library and post detail, so both return the same shape.
    public static MediaDto from(Media media, String locale) {
        return MediaDto.builder()
                .id(media.getId())
                .postId(media.getPost() != null ? media.getPost().getId() : null)
                .type(media.getType())
                .filename(media.getFilename())
                .originalName(media.getOriginalName())
                .mimeType(media.getMimeType())
                .size(media.getSize())
                .url(media.getUrl())
                .altText(media.getAltText(locale))
                .altTextEn(media.getAltTextEn())
                .altTextPl(media.getAltTextPl())
                .displayOrder(media.getDisplayOrder())
                .videoUrl(media.getVideoUrl())
                .createdAt(media.getCreatedAt())
                .build();
    }
}
