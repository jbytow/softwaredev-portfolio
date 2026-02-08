package com.portfolio.controller;

import com.portfolio.dto.ApiResponse;
import com.portfolio.dto.SiteSettingsDto;
import com.portfolio.service.SiteSettingsService;
import com.portfolio.util.LocaleUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/settings")
@RequiredArgsConstructor
public class SiteSettingsController {

    private final SiteSettingsService siteSettingsService;

    @GetMapping
    public ResponseEntity<ApiResponse<SiteSettingsDto>> getSettings(
            @RequestHeader(value = "Accept-Language", defaultValue = "en") String locale) {

        SiteSettingsDto settings = siteSettingsService.getSettings(LocaleUtils.extractLocale(locale));
        return ResponseEntity.ok(ApiResponse.success(settings));
    }
}
