package com.portfolio.controller;

import com.portfolio.dto.ApiResponse;
import com.portfolio.dto.SkillCategoryWithSkillsDto;
import com.portfolio.service.SkillCategoryService;
import com.portfolio.util.LocaleUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/skill-categories")
@RequiredArgsConstructor
public class SkillCategoryController {

    private final SkillCategoryService skillCategoryService;

    @GetMapping("/with-skills")
    public ResponseEntity<ApiResponse<List<SkillCategoryWithSkillsDto>>> getAllCategoriesWithSkills(
            @RequestHeader(value = "Accept-Language", defaultValue = "en") String locale) {

        List<SkillCategoryWithSkillsDto> categories = skillCategoryService.getAllCategoriesWithSkills(LocaleUtils.extractLocale(locale));
        return ResponseEntity.ok(ApiResponse.success(categories));
    }
}
