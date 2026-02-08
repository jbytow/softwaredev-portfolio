package com.portfolio.controller;

import com.portfolio.dto.ApiResponse;
import com.portfolio.dto.RpgStatDto;
import com.portfolio.service.RpgStatService;
import com.portfolio.util.LocaleUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/rpg-stats")
@RequiredArgsConstructor
public class RpgStatController {

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
}
