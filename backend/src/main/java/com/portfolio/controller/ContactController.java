package com.portfolio.controller;

import com.portfolio.dto.ApiResponse;
import com.portfolio.dto.ContactRequest;
import com.portfolio.service.ContactService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/contact")
@RequiredArgsConstructor
@Slf4j
public class ContactController {

    private final ContactService contactService;

    @PostMapping
    public ResponseEntity<ApiResponse<Void>> submitContact(@Valid @RequestBody ContactRequest request,
                                                           HttpServletRequest httpRequest) {
        // Same answer as a real submission, so a bot cannot tell it was caught
        if (request.getWebsite() != null && !request.getWebsite().isBlank()) {
            log.warn("Contact honeypot triggered from {}", clientAddress(httpRequest));
            return ResponseEntity.ok(ApiResponse.success(null));
        }
        contactService.sendContactEmail(request);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    // Same rule as GlobalExceptionHandler: CF-Connecting-IP is set by Cloudflare, not by the client
    private static String clientAddress(HttpServletRequest request) {
        String cloudflareAddress = request.getHeader("CF-Connecting-IP");
        if (cloudflareAddress != null && !cloudflareAddress.isBlank()) {
            return cloudflareAddress.trim();
        }
        return request.getRemoteAddr();
    }
}
