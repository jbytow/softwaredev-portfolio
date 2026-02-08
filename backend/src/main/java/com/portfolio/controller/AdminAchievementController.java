package com.portfolio.controller;

import com.portfolio.dto.*;
import com.portfolio.service.AchievementService;
import com.portfolio.util.LocaleUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin/achievements")
@RequiredArgsConstructor
public class AdminAchievementController {

    private final AchievementService achievementService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<AchievementDto>>> getAll(
            @RequestHeader(value = "Accept-Language", defaultValue = "en") String locale) {

        List<AchievementDto> achievements = achievementService.getAll(LocaleUtils.extractLocale(locale));
        return ResponseEntity.ok(ApiResponse.success(achievements));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<AchievementDto>> getById(
            @PathVariable UUID id,
            @RequestHeader(value = "Accept-Language", defaultValue = "en") String locale) {

        AchievementDto achievement = achievementService.getById(id, LocaleUtils.extractLocale(locale));
        return ResponseEntity.ok(ApiResponse.success(achievement));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<AchievementDto>> create(
            @Valid @RequestBody AchievementCreateRequest request,
            @RequestHeader(value = "Accept-Language", defaultValue = "en") String locale) {

        AchievementDto achievement = achievementService.create(request, LocaleUtils.extractLocale(locale));
        return ResponseEntity.ok(ApiResponse.success(achievement, "Achievement created successfully"));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<AchievementDto>> update(
            @PathVariable UUID id,
            @RequestBody AchievementUpdateRequest request,
            @RequestHeader(value = "Accept-Language", defaultValue = "en") String locale) {

        AchievementDto achievement = achievementService.update(id, request, LocaleUtils.extractLocale(locale));
        return ResponseEntity.ok(ApiResponse.success(achievement, "Achievement updated successfully"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        achievementService.delete(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Achievement deleted successfully"));
    }

    @PatchMapping("/reorder")
    public ResponseEntity<ApiResponse<Void>> reorder(@RequestBody ReorderRequest request) {
        achievementService.reorder(request);
        return ResponseEntity.ok(ApiResponse.success(null, "Achievements reordered successfully"));
    }
}
