package com.portfolio.entity;

import com.portfolio.config.MediaTypeConverter;
import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "media")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Media {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id")
    private Post post;

    @Convert(converter = MediaTypeConverter.class)
    @Column(name = "type", nullable = false)
    private MediaType type;

    @Column(name = "filename")
    private String filename;

    @Column(name = "original_name")
    private String originalName;

    @Column(name = "mime_type")
    private String mimeType;

    @Column(name = "size")
    private Long size;

    @Column(name = "url", nullable = false)
    private String url;

    @Column(name = "display_order")
    @Builder.Default
    private Integer displayOrder = 0;

    @Column(name = "video_url", length = 500)
    private String videoUrl;

    @Column(name = "alt_text_en")
    private String altTextEn;

    @Column(name = "alt_text_pl")
    private String altTextPl;

    @Column(name = "created_at")
    private OffsetDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = OffsetDateTime.now();
    }

    public String getAltText(String locale) {
        return "pl".equalsIgnoreCase(locale) ? altTextPl : altTextEn;
    }
}
