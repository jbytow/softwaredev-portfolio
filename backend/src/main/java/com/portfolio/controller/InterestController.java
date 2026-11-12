package com.portfolio.controller;

import com.portfolio.dto.ApiResponse;
import com.portfolio.dto.InterestDto;
import com.portfolio.service.InterestService;
import com.portfolio.util.LocaleUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/interests")
@RequiredArgsConstructor
public class InterestController {

    private final InterestService interestService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<InterestDto>>> getAll(
            @RequestHeader(value = "Accept-Language", defaultValue = "en") String locale) {

        List<InterestDto> interests = interestService.getAll(LocaleUtils.extractLocale(locale));
        return ResponseEntity.ok(ApiResponse.success(interests));
    }
}
