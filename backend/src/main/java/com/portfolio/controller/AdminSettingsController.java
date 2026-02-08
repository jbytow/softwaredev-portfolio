package com.portfolio.controller;

import com.portfolio.dto.ApiResponse;
import com.portfolio.dto.SiteSettingsDto;
import com.portfolio.dto.SiteSettingsUpdateRequest;
import com.portfolio.service.SiteSettingsService;
import com.portfolio.util.LocaleUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/settings")
@RequiredArgsConstructor
public class AdminSettingsController {

    private final SiteSettingsService siteSettingsService;

    @GetMapping
    public ResponseEntity<ApiResponse<SiteSettingsDto>> getSettings(
            @RequestHeader(value = "Accept-Language", defaultValue = "en") String locale) {

        SiteSettingsDto settings = siteSettingsService.getSettings(LocaleUtils.extractLocale(locale));
        return ResponseEntity.ok(ApiResponse.success(settings));
    }

    @PutMapping
    public ResponseEntity<ApiResponse<SiteSettingsDto>> updateSettings(
            @RequestBody SiteSettingsUpdateRequest request,
            @RequestHeader(value = "Accept-Language", defaultValue = "en") String locale) {

        SiteSettingsDto settings = siteSettingsService.updateSettings(request, LocaleUtils.extractLocale(locale));
        return ResponseEntity.ok(ApiResponse.success(settings, "Settings updated successfully"));
    }
}
