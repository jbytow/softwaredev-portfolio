package com.portfolio.controller;

import com.portfolio.dto.ApiResponse;
import com.portfolio.dto.PostDto;
import com.portfolio.entity.Category;
import com.portfolio.service.PostService;
import com.portfolio.util.LocaleUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<PostDto>>> getPosts(
            @RequestParam(required = false) Category category,
            @RequestParam(required = false) String hashtag,
            @RequestHeader(value = "Accept-Language", defaultValue = "en") String locale) {

        List<PostDto> posts;
        if (hashtag != null && !hashtag.isBlank()) {
            posts = postService.getPostsByHashtag(hashtag, LocaleUtils.extractLocale(locale));
        } else if (category != null) {
            posts = postService.getPostsByCategory(category, LocaleUtils.extractLocale(locale));
        } else {
            posts = postService.getAllPublishedPosts(LocaleUtils.extractLocale(locale));
        }

        return ResponseEntity.ok(ApiResponse.success(posts));
    }

    @GetMapping("/hashtags")
    public ResponseEntity<ApiResponse<List<String>>> getAllHashtags() {
        List<String> hashtags = postService.getAllHashtags();
        return ResponseEntity.ok(ApiResponse.success(hashtags));
    }

    @GetMapping("/paged")
    public ResponseEntity<ApiResponse<Page<PostDto>>> getPostsPaged(
            @RequestParam(required = false) Category category,
            @PageableDefault(size = 10) Pageable pageable,
            @RequestHeader(value = "Accept-Language", defaultValue = "en") String locale) {

        Page<PostDto> posts;
        if (category != null) {
            posts = postService.getPostsByCategory(category, pageable, LocaleUtils.extractLocale(locale));
        } else {
            posts = postService.getPublishedPosts(pageable, LocaleUtils.extractLocale(locale));
        }

        return ResponseEntity.ok(ApiResponse.success(posts));
    }

    @GetMapping("/{slug}")
    public ResponseEntity<ApiResponse<PostDto>> getPost(
            @PathVariable String slug,
            @RequestHeader(value = "Accept-Language", defaultValue = "en") String locale) {

        PostDto post = postService.getPostBySlug(slug, LocaleUtils.extractLocale(locale));
        return ResponseEntity.ok(ApiResponse.success(post));
    }
}
