package com.portfolio.controller;

import com.portfolio.dto.AchievementDto;
import com.portfolio.dto.ApiResponse;
import com.portfolio.service.AchievementService;
import com.portfolio.util.LocaleUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/achievements")
@RequiredArgsConstructor
public class AchievementController {

    private final AchievementService achievementService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<AchievementDto>>> getAll(
            @RequestHeader(value = "Accept-Language", defaultValue = "en") String locale) {

        List<AchievementDto> achievements = achievementService.getAll(LocaleUtils.extractLocale(locale));
        return ResponseEntity.ok(ApiResponse.success(achievements));
    }
}
