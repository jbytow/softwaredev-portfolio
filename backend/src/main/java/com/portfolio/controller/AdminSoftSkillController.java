package com.portfolio.controller;

import com.portfolio.dto.*;
import com.portfolio.service.SoftSkillService;
import com.portfolio.util.LocaleUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin/soft-skills")
@RequiredArgsConstructor
public class AdminSoftSkillController {

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

    @PostMapping
    public ResponseEntity<ApiResponse<SoftSkillDto>> createSoftSkill(
            @Valid @RequestBody SoftSkillCreateRequest request,
            @RequestHeader(value = "Accept-Language", defaultValue = "en") String locale) {

        SoftSkillDto skill = softSkillService.createSoftSkill(request, LocaleUtils.extractLocale(locale));
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(skill, "Soft skill created successfully"));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<SoftSkillDto>> updateSoftSkill(
            @PathVariable UUID id,
            @Valid @RequestBody SoftSkillUpdateRequest request,
            @RequestHeader(value = "Accept-Language", defaultValue = "en") String locale) {

        SoftSkillDto skill = softSkillService.updateSoftSkill(id, request, LocaleUtils.extractLocale(locale));
        return ResponseEntity.ok(ApiResponse.success(skill, "Soft skill updated successfully"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteSoftSkill(@PathVariable UUID id) {
        softSkillService.deleteSoftSkill(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Soft skill deleted successfully"));
    }

    @PatchMapping("/reorder")
    public ResponseEntity<ApiResponse<Void>> reorderSoftSkills(@RequestBody ReorderRequest request) {
        softSkillService.reorderSoftSkills(request);
        return ResponseEntity.ok(ApiResponse.success(null, "Soft skills reordered successfully"));
    }
}
