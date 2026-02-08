package com.portfolio.controller;

import com.portfolio.dto.*;
import com.portfolio.service.ExperienceService;
import com.portfolio.util.LocaleUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin/experiences")
@RequiredArgsConstructor
public class AdminExperienceController {

    private final ExperienceService experienceService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<ExperienceDto>>> getAllExperiences(
            @RequestHeader(value = "Accept-Language", defaultValue = "en") String locale) {

        List<ExperienceDto> experiences = experienceService.getAllExperiences(LocaleUtils.extractLocale(locale));
        return ResponseEntity.ok(ApiResponse.success(experiences));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ExperienceDto>> getExperienceById(
            @PathVariable UUID id,
            @RequestHeader(value = "Accept-Language", defaultValue = "en") String locale) {

        ExperienceDto experience = experienceService.getExperienceById(id, LocaleUtils.extractLocale(locale));
        return ResponseEntity.ok(ApiResponse.success(experience));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<ExperienceDto>> createExperience(
            @Valid @RequestBody ExperienceCreateRequest request,
            @RequestHeader(value = "Accept-Language", defaultValue = "en") String locale) {

        ExperienceDto experience = experienceService.createExperience(request, LocaleUtils.extractLocale(locale));
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(experience, "Experience created successfully"));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ExperienceDto>> updateExperience(
            @PathVariable UUID id,
            @Valid @RequestBody ExperienceUpdateRequest request,
            @RequestHeader(value = "Accept-Language", defaultValue = "en") String locale) {

        ExperienceDto experience = experienceService.updateExperience(id, request, LocaleUtils.extractLocale(locale));
        return ResponseEntity.ok(ApiResponse.success(experience, "Experience updated successfully"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteExperience(@PathVariable UUID id) {
        experienceService.deleteExperience(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Experience deleted successfully"));
    }

    @PatchMapping("/reorder")
    public ResponseEntity<ApiResponse<Void>> reorderExperiences(@RequestBody ReorderRequest request) {
        experienceService.reorderExperiences(request);
        return ResponseEntity.ok(ApiResponse.success(null, "Experiences reordered successfully"));
    }
}
