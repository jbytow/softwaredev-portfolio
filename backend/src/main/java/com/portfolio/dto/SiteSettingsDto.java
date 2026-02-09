package com.portfolio.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SiteSettingsDto {
    private String heroTitle;
    private String heroTitleEn;
    private String heroTitlePl;
    private String heroSubtitle;
    private String heroSubtitleEn;
    private String heroSubtitlePl;
    private String aboutText;
    private String aboutTextEn;
    private String aboutTextPl;
    private String profileImage;
    private String email;
    private String phone;
    private Map<String, String> socialLinks;
    private String metaDescription;
    private String metaDescriptionEn;
    private String metaDescriptionPl;
    private String footerTitle;
    private String footerTitleEn;
    private String footerTitlePl;
    private String footerTagline;
    private String footerTaglineEn;
    private String footerTaglinePl;
    private String ownerName;
    private String siteName;
    private String rpgClassTitle;
    private String rpgClassTitleEn;
    private String rpgClassTitlePl;
    private String[] aboutTags;
    private String[] aboutTagsEn;
    private String[] aboutTagsPl;
    private List<StatItemDto> statsItems;
}
