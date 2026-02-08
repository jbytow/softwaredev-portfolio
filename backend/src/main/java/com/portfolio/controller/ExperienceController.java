package com.portfolio.controller;

import com.portfolio.dto.ApiResponse;
import com.portfolio.dto.ExperienceDto;
import com.portfolio.service.ExperienceService;
import com.portfolio.util.LocaleUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/experiences")
@RequiredArgsConstructor
public class ExperienceController {

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
}
