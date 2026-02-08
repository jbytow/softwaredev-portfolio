package com.portfolio.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "achievements")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Achievement {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "title_en", nullable = false)
    private String titleEn;

    @Column(name = "title_pl", nullable = false)
    private String titlePl;

    @Column(name = "description_en", columnDefinition = "TEXT")
    private String descriptionEn;

    @Column(name = "description_pl", columnDefinition = "TEXT")
    private String descriptionPl;

    @Column(name = "icon")
    private String icon;

    @Column(name = "year")
    private String year;

    @Column(name = "display_order")
    @Builder.Default
    private Integer displayOrder = 0;

    @Column(name = "created_at")
    private OffsetDateTime createdAt;

    @Column(name = "updated_at")
    private OffsetDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = OffsetDateTime.now();
        updatedAt = OffsetDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = OffsetDateTime.now();
    }

    public String getTitle(String locale) {
        return "pl".equalsIgnoreCase(locale) ? titlePl : titleEn;
    }

    public String getDescription(String locale) {
        return "pl".equalsIgnoreCase(locale) ? descriptionPl : descriptionEn;
    }
}
