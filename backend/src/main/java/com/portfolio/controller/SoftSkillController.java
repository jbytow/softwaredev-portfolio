package com.portfolio.controller;

import com.portfolio.dto.ApiResponse;
import com.portfolio.dto.SoftSkillDto;
import com.portfolio.service.SoftSkillService;
import com.portfolio.util.LocaleUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/soft-skills")
@RequiredArgsConstructor
public class SoftSkillController {

    private final SoftSkillService softSkillService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<SoftSkillDto>>> getAllSoftSkills(
            @RequestHeader(value = "Accept-Language", defaultValue = "en") String locale) {

        List<SoftSkillDto> skills = softSkillService.getAllSoftSkills(LocaleUtils.extractLocale(locale));
        return ResponseEntity.ok(ApiResponse.success(skills));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<SoftSkillDto>> getSoftSkillById(
            @PathVariable UUID id,
            @RequestHeader(value = "Accept-Language", defaultValue = "en") String locale) {

        SoftSkillDto skill = softSkillService.getSoftSkillById(id, LocaleUtils.extractLocale(locale));
        return ResponseEntity.ok(ApiResponse.success(skill));
    }
}
