package com.portfolio.controller;

import com.portfolio.dto.*;
import com.portfolio.service.InterestService;
import com.portfolio.util.LocaleUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin/interests")
@RequiredArgsConstructor
public class AdminInterestController {

    private final InterestService interestService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<InterestDto>>> getAll(
            @RequestHeader(value = "Accept-Language", defaultValue = "en") String locale) {

        List<InterestDto> interests = interestService.getAll(LocaleUtils.extractLocale(locale));
        return ResponseEntity.ok(ApiResponse.success(interests));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<InterestDto>> getById(
            @PathVariable UUID id,
            @RequestHeader(value = "Accept-Language", defaultValue = "en") String locale) {

        InterestDto interest = interestService.getById(id, LocaleUtils.extractLocale(locale));
        return ResponseEntity.ok(ApiResponse.success(interest));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<InterestDto>> create(
            @Valid @RequestBody InterestCreateRequest request,
            @RequestHeader(value = "Accept-Language", defaultValue = "en") String locale) {

        InterestDto interest = interestService.create(request, LocaleUtils.extractLocale(locale));
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(interest));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<InterestDto>> update(
            @PathVariable UUID id,
            @Valid @RequestBody InterestUpdateRequest request,
            @RequestHeader(value = "Accept-Language", defaultValue = "en") String locale) {

        InterestDto interest = interestService.update(id, request, LocaleUtils.extractLocale(locale));
        return ResponseEntity.ok(ApiResponse.success(interest));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        interestService.delete(id);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @PatchMapping("/reorder")
    public ResponseEntity<ApiResponse<Void>> reorder(@Valid @RequestBody ReorderRequest request) {
        interestService.reorder(request);
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
