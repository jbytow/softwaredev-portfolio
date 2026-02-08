package com.portfolio.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@NamedEntityGraph(
    name = "SoftSkill.withCategory",
    attributeNodes = @NamedAttributeNode("category")
)
@Table(name = "soft_skills")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SoftSkill {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "name_en", nullable = false)
    private String nameEn;

    @Column(name = "name_pl", nullable = false)
    private String namePl;

    @Column(name = "description_en", columnDefinition = "TEXT")
    private String descriptionEn;

    @Column(name = "description_pl", columnDefinition = "TEXT")
    private String descriptionPl;

    @Column(name = "professional_usage_en", columnDefinition = "TEXT")
    private String professionalUsageEn;

    @Column(name = "professional_usage_pl", columnDefinition = "TEXT")
    private String professionalUsagePl;

    @Column(name = "icon")
    private String icon;

    @Column(name = "display_order")
    @Builder.Default
    private Integer displayOrder = 0;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private SkillCategory category;

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

    public String getName(String locale) {
        return "pl".equalsIgnoreCase(locale) ? namePl : nameEn;
    }

    public String getDescription(String locale) {
        return "pl".equalsIgnoreCase(locale) ? descriptionPl : descriptionEn;
    }

    public String getProfessionalUsage(String locale) {
        return "pl".equalsIgnoreCase(locale) ? professionalUsagePl : professionalUsageEn;
    }
}
