package com.portfolio.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
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
public class SiteSettingsUpdateRequest {
    @Size(max = 255, message = "English hero title must not exceed 255 characters")
    private String heroTitleEn;
    @Size(max = 255, message = "Polish hero title must not exceed 255 characters")
    private String heroTitlePl;
    private String heroSubtitleEn;
    private String heroSubtitlePl;
    private String aboutTextEn;
    private String aboutTextPl;
    @Size(max = 500, message = "Profile image must not exceed 500 characters")
    private String profileImage;
    @Email(message = "Email must be a valid address")
    @Size(max = 255, message = "Email must not exceed 255 characters")
    private String email;
    @Size(max = 50, message = "Phone must not exceed 50 characters")
    private String phone;
    private Map<String, String> socialLinks;
    private String metaDescriptionEn;
    private String metaDescriptionPl;
    @Size(max = 255, message = "English footer title must not exceed 255 characters")
    private String footerTitleEn;
    @Size(max = 255, message = "Polish footer title must not exceed 255 characters")
    private String footerTitlePl;
    private String footerTaglineEn;
    private String footerTaglinePl;
    @Size(max = 255, message = "Owner name must not exceed 255 characters")
    private String ownerName;
    @Size(max = 255, message = "Site name must not exceed 255 characters")
    private String siteName;
    @Size(max = 255, message = "English RPG class title must not exceed 255 characters")
    private String rpgClassTitleEn;
    @Size(max = 255, message = "Polish RPG class title must not exceed 255 characters")
    private String rpgClassTitlePl;
    private String[] aboutTagsEn;
    private String[] aboutTagsPl;
    private List<StatItemDto> statsItems;
}
