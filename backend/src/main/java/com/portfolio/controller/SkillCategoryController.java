package com.portfolio.controller;

import com.portfolio.dto.ApiResponse;
import com.portfolio.dto.SkillCategoryDto;
import com.portfolio.dto.SkillCategoryWithSkillsDto;
import com.portfolio.service.SkillCategoryService;
import com.portfolio.util.LocaleUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/skill-categories")
@RequiredArgsConstructor
public class SkillCategoryController {

    private final SkillCategoryService skillCategoryService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<SkillCategoryDto>>> getAllCategories(
            @RequestHeader(value = "Accept-Language", defaultValue = "en") String locale) {

        List<SkillCategoryDto> categories = skillCategoryService.getAllCategories(LocaleUtils.extractLocale(locale));
        return ResponseEntity.ok(ApiResponse.success(categories));
    }

    @GetMapping("/with-skills")
    public ResponseEntity<ApiResponse<List<SkillCategoryWithSkillsDto>>> getAllCategoriesWithSkills(
            @RequestHeader(value = "Accept-Language", defaultValue = "en") String locale) {

        List<SkillCategoryWithSkillsDto> categories = skillCategoryService.getAllCategoriesWithSkills(LocaleUtils.extractLocale(locale));
        return ResponseEntity.ok(ApiResponse.success(categories));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<SkillCategoryDto>> getCategoryById(
            @PathVariable UUID id,
            @RequestHeader(value = "Accept-Language", defaultValue = "en") String locale) {

        SkillCategoryDto category = skillCategoryService.getCategoryById(id, LocaleUtils.extractLocale(locale));
        return ResponseEntity.ok(ApiResponse.success(category));
    }
}
