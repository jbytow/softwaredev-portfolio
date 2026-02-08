package com.portfolio.controller;

import com.portfolio.dto.*;
import com.portfolio.entity.Category;
import com.portfolio.service.PostService;
import com.portfolio.util.LocaleUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin/posts")
@RequiredArgsConstructor
public class AdminPostController {

    private final PostService postService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<PostDto>>> getAllPosts(
            @RequestParam(required = false) Category category,
            @RequestHeader(value = "Accept-Language", defaultValue = "en") String locale) {

        List<PostDto> posts;
        if (category != null) {
            posts = postService.getAllPostsByCategory(category, LocaleUtils.extractLocale(locale));
        } else {
            posts = postService.getAllPosts(LocaleUtils.extractLocale(locale));
        }

        return ResponseEntity.ok(ApiResponse.success(posts));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<PostDto>> getPost(
            @PathVariable UUID id,
            @RequestHeader(value = "Accept-Language", defaultValue = "en") String locale) {

        PostDto post = postService.getPostById(id, LocaleUtils.extractLocale(locale));
        return ResponseEntity.ok(ApiResponse.success(post));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<PostDto>> createPost(
            @Valid @RequestBody PostCreateRequest request,
            @RequestHeader(value = "Accept-Language", defaultValue = "en") String locale) {

        PostDto post = postService.createPost(request, LocaleUtils.extractLocale(locale));
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(post, "Post created successfully"));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<PostDto>> updatePost(
            @PathVariable UUID id,
            @Valid @RequestBody PostUpdateRequest request,
            @RequestHeader(value = "Accept-Language", defaultValue = "en") String locale) {

        PostDto post = postService.updatePost(id, request, LocaleUtils.extractLocale(locale));
        return ResponseEntity.ok(ApiResponse.success(post, "Post updated successfully"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deletePost(@PathVariable UUID id) {
        postService.deletePost(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Post deleted successfully"));
    }

    @PatchMapping("/{id}/publish")
    public ResponseEntity<ApiResponse<PostDto>> togglePublish(
            @PathVariable UUID id,
            @RequestHeader(value = "Accept-Language", defaultValue = "en") String locale) {

        PostDto post = postService.togglePublish(id, LocaleUtils.extractLocale(locale));
        String message = post.getPublished() ? "Post published successfully" : "Post unpublished successfully";
        return ResponseEntity.ok(ApiResponse.success(post, message));
    }

    @PatchMapping("/reorder")
    public ResponseEntity<ApiResponse<Void>> reorderPosts(@RequestBody ReorderRequest request) {
        postService.reorderPosts(request);
        return ResponseEntity.ok(ApiResponse.success(null, "Posts reordered successfully"));
    }
}
