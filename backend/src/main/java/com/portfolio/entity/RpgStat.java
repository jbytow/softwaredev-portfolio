package com.portfolio.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "rpg_stats")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RpgStat {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "attr", nullable = false, length = 10)
    private String attr;

    @Column(name = "label_en", nullable = false)
    private String labelEn;

    @Column(name = "label_pl", nullable = false)
    private String labelPl;

    @Column(name = "level")
    @Builder.Default
    private Integer level = 5;

    @Column(name = "max_level")
    @Builder.Default
    private Integer maxLevel = 10;

    @Column(name = "skills", columnDefinition = "TEXT")
    private String skills;

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

    public String getLabel(String locale) {
        return "pl".equalsIgnoreCase(locale) ? labelPl : labelEn;
    }
}
