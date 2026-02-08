package com.portfolio.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;

@Entity
@Table(name = "site_settings")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SiteSettings {

    @Id
    @Column(name = "id")
    @Builder.Default
    private Integer id = 1;

    @Column(name = "hero_title_en")
    private String heroTitleEn;

    @Column(name = "hero_title_pl")
    private String heroTitlePl;

    @Column(name = "hero_subtitle_en", columnDefinition = "TEXT")
    private String heroSubtitleEn;

    @Column(name = "hero_subtitle_pl", columnDefinition = "TEXT")
    private String heroSubtitlePl;

    @Column(name = "about_text_en", columnDefinition = "TEXT")
    private String aboutTextEn;

    @Column(name = "about_text_pl", columnDefinition = "TEXT")
    private String aboutTextPl;

    @Column(name = "profile_image")
    private String profileImage;

    @Column(name = "email")
    private String email;

    @Column(name = "phone")
    private String phone;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "social_links", columnDefinition = "jsonb")
    private Map<String, String> socialLinks;

    @Column(name = "meta_description_en", columnDefinition = "TEXT")
    private String metaDescriptionEn;

    @Column(name = "meta_description_pl", columnDefinition = "TEXT")
    private String metaDescriptionPl;

    @Column(name = "footer_title_en")
    private String footerTitleEn;

    @Column(name = "footer_title_pl")
    private String footerTitlePl;

    @Column(name = "footer_tagline_en", columnDefinition = "TEXT")
    private String footerTaglineEn;

    @Column(name = "footer_tagline_pl", columnDefinition = "TEXT")
    private String footerTaglinePl;

    @Column(name = "owner_name")
    private String ownerName;

    @Column(name = "site_name")
    private String siteName;

    @Column(name = "about_tags_en", columnDefinition = "TEXT[]")
    @JdbcTypeCode(SqlTypes.ARRAY)
    private String[] aboutTagsEn;

    @Column(name = "about_tags_pl", columnDefinition = "TEXT[]")
    @JdbcTypeCode(SqlTypes.ARRAY)
    private String[] aboutTagsPl;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "stats_items", columnDefinition = "jsonb")
    private List<Map<String, String>> statsItems;

    @Column(name = "updated_at")
    private OffsetDateTime updatedAt;

    @PreUpdate
    protected void onUpdate() {
        updatedAt = OffsetDateTime.now();
    }

    public String getHeroTitle(String locale) {
        return "pl".equalsIgnoreCase(locale) ? heroTitlePl : heroTitleEn;
    }

    public String getHeroSubtitle(String locale) {
        return "pl".equalsIgnoreCase(locale) ? heroSubtitlePl : heroSubtitleEn;
    }

    public String getAboutText(String locale) {
        return "pl".equalsIgnoreCase(locale) ? aboutTextPl : aboutTextEn;
    }

    public String getMetaDescription(String locale) {
        return "pl".equalsIgnoreCase(locale) ? metaDescriptionPl : metaDescriptionEn;
    }

    public String getFooterTitle(String locale) {
        return "pl".equalsIgnoreCase(locale) ? footerTitlePl : footerTitleEn;
    }

    public String getFooterTagline(String locale) {
        return "pl".equalsIgnoreCase(locale) ? footerTaglinePl : footerTaglineEn;
    }

    public String[] getAboutTags(String locale) {
        return "pl".equalsIgnoreCase(locale) ? aboutTagsPl : aboutTagsEn;
    }
}
