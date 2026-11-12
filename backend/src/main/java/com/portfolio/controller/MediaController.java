package com.portfolio.controller;

import com.portfolio.service.StorageService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/api/media")
@RequiredArgsConstructor
public class MediaController {

    private final StorageService storageService;

    @GetMapping("/{subDir}/{filename}")
    public ResponseEntity<Resource> serveMedia(
            @PathVariable String subDir,
            @PathVariable String filename) {

        if (!StorageService.isMediaSubdirectory(subDir)) {
            throw new EntityNotFoundException("Media file not found: " + subDir + "/" + filename);
        }
        String fullPath = subDir + "/" + filename;
        Resource resource = storageService.loadAsResource(fullPath);

        String contentType = StorageService.contentTypeFor(filename);

        // Stored names are random UUIDs and a replaced file always gets a new name, so a
        // response can be cached forever; without this Spring Security sends no-store.
        return ResponseEntity.ok()
                .cacheControl(CacheControl.maxAge(365, TimeUnit.DAYS).cachePublic().immutable())
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + filename + "\"")
                .body(resource);
    }
}
