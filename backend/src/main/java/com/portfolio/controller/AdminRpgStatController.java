package com.portfolio.controller;

import com.portfolio.dto.*;
import com.portfolio.service.RpgStatService;
import com.portfolio.util.LocaleUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin/rpg-stats")
@RequiredArgsConstructor
public class AdminRpgStatController {

    private final RpgStatService rpgStatService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<RpgStatDto>>> getAll(
            @RequestHeader(value = "Accept-Language", defaultValue = "en") String locale) {

        List<RpgStatDto> stats = rpgStatService.getAll(LocaleUtils.extractLocale(locale));
        return ResponseEntity.ok(ApiResponse.success(stats));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<RpgStatDto>> getById(
            @PathVariable UUID id,
            @RequestHeader(value = "Accept-Language", defaultValue = "en") String locale) {

        RpgStatDto stat = rpgStatService.getById(id, LocaleUtils.extractLocale(locale));
        return ResponseEntity.ok(ApiResponse.success(stat));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<RpgStatDto>> create(
            @Valid @RequestBody RpgStatCreateRequest request,
            @RequestHeader(value = "Accept-Language", defaultValue = "en") String locale) {

        RpgStatDto stat = rpgStatService.create(request, LocaleUtils.extractLocale(locale));
        return ResponseEntity.ok(ApiResponse.success(stat, "RPG stat created successfully"));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<RpgStatDto>> update(
            @PathVariable UUID id,
            @RequestBody RpgStatUpdateRequest request,
            @RequestHeader(value = "Accept-Language", defaultValue = "en") String locale) {

        RpgStatDto stat = rpgStatService.update(id, request, LocaleUtils.extractLocale(locale));
        return ResponseEntity.ok(ApiResponse.success(stat, "RPG stat updated successfully"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        rpgStatService.delete(id);
        return ResponseEntity.ok(ApiResponse.success(null, "RPG stat deleted successfully"));
    }

    @PatchMapping("/reorder")
    public ResponseEntity<ApiResponse<Void>> reorder(@RequestBody ReorderRequest request) {
        rpgStatService.reorder(request);
        return ResponseEntity.ok(ApiResponse.success(null, "RPG stats reordered successfully"));
    }
}
