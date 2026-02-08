package com.portfolio.service;

import com.portfolio.dto.SiteSettingsDto;
import com.portfolio.dto.SiteSettingsUpdateRequest;
import com.portfolio.dto.StatItemDto;
import com.portfolio.entity.SiteSettings;
import com.portfolio.repository.SiteSettingsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SiteSettingsService {

    private final SiteSettingsRepository siteSettingsRepository;

    public SiteSettingsDto getSettings(String locale) {
        SiteSettings settings = siteSettingsRepository.getSettings();
        return mapToDto(settings, locale);
    }

    @Transactional
    public SiteSettingsDto updateSettings(SiteSettingsUpdateRequest request, String locale) {
        SiteSettings settings = siteSettingsRepository.getSettings();

        if (request.getHeroTitleEn() != null) {
            settings.setHeroTitleEn(request.getHeroTitleEn());
        }
        if (request.getHeroTitlePl() != null) {
            settings.setHeroTitlePl(request.getHeroTitlePl());
        }
        if (request.getHeroSubtitleEn() != null) {
            settings.setHeroSubtitleEn(request.getHeroSubtitleEn());
        }
        if (request.getHeroSubtitlePl() != null) {
            settings.setHeroSubtitlePl(request.getHeroSubtitlePl());
        }
        if (request.getAboutTextEn() != null) {
            settings.setAboutTextEn(request.getAboutTextEn());
        }
        if (request.getAboutTextPl() != null) {
            settings.setAboutTextPl(request.getAboutTextPl());
        }
        if (request.getProfileImage() != null) {
            settings.setProfileImage(request.getProfileImage());
        }
        if (request.getEmail() != null) {
            settings.setEmail(request.getEmail());
        }
        if (request.getPhone() != null) {
            settings.setPhone(request.getPhone());
        }
        if (request.getSocialLinks() != null) {
            settings.setSocialLinks(request.getSocialLinks());
        }
        if (request.getMetaDescriptionEn() != null) {
            settings.setMetaDescriptionEn(request.getMetaDescriptionEn());
        }
        if (request.getMetaDescriptionPl() != null) {
            settings.setMetaDescriptionPl(request.getMetaDescriptionPl());
        }
        if (request.getFooterTitleEn() != null) {
            settings.setFooterTitleEn(request.getFooterTitleEn());
        }
        if (request.getFooterTitlePl() != null) {
            settings.setFooterTitlePl(request.getFooterTitlePl());
        }
        if (request.getFooterTaglineEn() != null) {
            settings.setFooterTaglineEn(request.getFooterTaglineEn());
        }
        if (request.getFooterTaglinePl() != null) {
            settings.setFooterTaglinePl(request.getFooterTaglinePl());
        }
        if (request.getOwnerName() != null) {
            settings.setOwnerName(request.getOwnerName());
        }
        if (request.getSiteName() != null) {
            settings.setSiteName(request.getSiteName());
        }
        if (request.getAboutTagsEn() != null) {
            settings.setAboutTagsEn(request.getAboutTagsEn());
        }
        if (request.getAboutTagsPl() != null) {
            settings.setAboutTagsPl(request.getAboutTagsPl());
        }
        if (request.getStatsItems() != null) {
            List<Map<String, String>> statsItemsMaps = new ArrayList<>();
            for (StatItemDto item : request.getStatsItems()) {
                Map<String, String> map = new HashMap<>();
                map.put("icon", item.getIcon());
                map.put("value", item.getValue());
                map.put("labelEn", item.getLabelEn());
                map.put("labelPl", item.getLabelPl());
                statsItemsMaps.add(map);
            }
            settings.setStatsItems(statsItemsMaps);
        }

        settings = siteSettingsRepository.save(settings);
        return mapToDto(settings, locale);
    }

    private SiteSettingsDto mapToDto(SiteSettings settings, String locale) {
        return SiteSettingsDto.builder()
                .heroTitle(settings.getHeroTitle(locale))
                .heroTitleEn(settings.getHeroTitleEn())
                .heroTitlePl(settings.getHeroTitlePl())
                .heroSubtitle(settings.getHeroSubtitle(locale))
                .heroSubtitleEn(settings.getHeroSubtitleEn())
                .heroSubtitlePl(settings.getHeroSubtitlePl())
                .aboutText(settings.getAboutText(locale))
                .aboutTextEn(settings.getAboutTextEn())
                .aboutTextPl(settings.getAboutTextPl())
                .profileImage(settings.getProfileImage())
                .email(settings.getEmail())
                .phone(settings.getPhone())
                .socialLinks(settings.getSocialLinks())
                .metaDescription(settings.getMetaDescription(locale))
                .metaDescriptionEn(settings.getMetaDescriptionEn())
                .metaDescriptionPl(settings.getMetaDescriptionPl())
                .footerTitle(settings.getFooterTitle(locale))
                .footerTitleEn(settings.getFooterTitleEn())
                .footerTitlePl(settings.getFooterTitlePl())
                .footerTagline(settings.getFooterTagline(locale))
                .footerTaglineEn(settings.getFooterTaglineEn())
                .footerTaglinePl(settings.getFooterTaglinePl())
                .ownerName(settings.getOwnerName())
                .siteName(settings.getSiteName())
                .aboutTags(settings.getAboutTags(locale))
                .aboutTagsEn(settings.getAboutTagsEn())
                .aboutTagsPl(settings.getAboutTagsPl())
                .statsItems(mapStatsItems(settings.getStatsItems(), locale))
                .build();
    }

    private List<StatItemDto> mapStatsItems(List<Map<String, String>> statsItems, String locale) {
        if (statsItems == null) {
            return new ArrayList<>();
        }
        List<StatItemDto> result = new ArrayList<>();
        for (Map<String, String> item : statsItems) {
            String label = "pl".equalsIgnoreCase(locale) ? item.get("labelPl") : item.get("labelEn");
            result.add(StatItemDto.builder()
                    .icon(item.get("icon"))
                    .value(item.get("value"))
                    .label(label)
                    .labelEn(item.get("labelEn"))
                    .labelPl(item.get("labelPl"))
                    .build());
        }
        return result;
    }
}
