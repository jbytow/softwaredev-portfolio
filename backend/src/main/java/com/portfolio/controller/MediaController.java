package com.portfolio.controller;

import com.portfolio.dto.ApiResponse;
import com.portfolio.dto.MediaDto;
import com.portfolio.service.MediaService;
import com.portfolio.service.StorageService;
import com.portfolio.util.LocaleUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/media")
@RequiredArgsConstructor
public class MediaController {

    private final MediaService mediaService;
    private final StorageService storageService;

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<MediaDto>> getMediaInfo(
            @PathVariable UUID id,
            @RequestHeader(value = "Accept-Language", defaultValue = "en") String locale) {

        MediaDto media = mediaService.getMediaById(id, LocaleUtils.extractLocale(locale));
        return ResponseEntity.ok(ApiResponse.success(media));
    }

    @GetMapping("/{subDir}/{filename}")
    public ResponseEntity<Resource> serveMedia(
            @PathVariable String subDir,
            @PathVariable String filename) {

        String fullPath = subDir + "/" + filename;
        Resource resource = storageService.loadAsResource(fullPath);

        String contentType = determineContentType(filename);

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + filename + "\"")
                .body(resource);
    }

    private String determineContentType(String filename) {
        String extension = filename.substring(filename.lastIndexOf('.') + 1).toLowerCase();
        return switch (extension) {
            case "jpg", "jpeg" -> "image/jpeg";
            case "png" -> "image/png";
            case "gif" -> "image/gif";
            case "webp" -> "image/webp";
            case "mp4" -> "video/mp4";
            case "webm" -> "video/webm";
            case "pdf" -> "application/pdf";
            default -> "application/octet-stream";
        };
    }
}
