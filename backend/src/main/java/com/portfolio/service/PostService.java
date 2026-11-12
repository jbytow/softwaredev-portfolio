package com.portfolio.service;

import com.portfolio.dto.*;
import com.portfolio.entity.*;
import com.portfolio.repository.PostRepository;
import com.portfolio.util.SlugUtils;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PostService {

    private final PostRepository postRepository;
    private final MediaService mediaService;

    public List<PostDto> getAllPublishedPosts(String locale) {
        return postRepository.findAllPublished().stream()
                .map(post -> mapToDto(post, locale, View.PUBLIC_LIST))
                .toList();
    }

    public List<PostDto> getPostsByCategory(Category category, String locale) {
        return postRepository.findByCategoryAndPublishedTrueOrderByDisplayOrderAsc(category).stream()
                .map(post -> mapToDto(post, locale, View.PUBLIC_LIST))
                .toList();
    }

    public List<PostDto> getPostsByHashtag(String hashtag, String locale) {
        return postRepository.findByHashtagAndPublishedTrue(hashtag).stream()
                .map(post -> mapToDto(post, locale, View.PUBLIC_LIST))
                .toList();
    }

    public PostDto getPostBySlug(String slug, String locale) {
        // Public route: a draft answers exactly like a missing slug so a guessed or shared link reveals nothing.
        Post post = postRepository.findBySlug(slug)
                .filter(found -> Boolean.TRUE.equals(found.getPublished()))
                .orElseThrow(() -> new EntityNotFoundException("Post not found: " + slug));
        return mapToDto(post, locale, View.PUBLIC_DETAIL);
    }

    public PostDto getPostById(UUID id, String locale) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Post not found: " + id));
        return mapToDto(post, locale, View.ADMIN_DETAIL);
    }

    // Admin methods
    public List<PostDto> getAllPosts(String locale) {
        return postRepository.findAll().stream()
                .map(post -> mapToDto(post, locale, View.ADMIN_LIST))
                .toList();
    }

    public List<PostDto> getAllPostsByCategory(Category category, String locale) {
        return postRepository.findAllByCategory(category).stream()
                .map(post -> mapToDto(post, locale, View.ADMIN_LIST))
                .toList();
    }

    @Transactional
    public PostDto createPost(PostCreateRequest request, String locale) {
        String slugSource = request.getSlug() == null || request.getSlug().isBlank()
                ? request.getTitleEn()
                : request.getSlug();
        String slug = generateUniqueSlug(slugSource);

        Post post = Post.builder()
                .category(request.getCategory())
                .titleEn(request.getTitleEn())
                .titlePl(request.getTitlePl())
                .slug(slug)
                .excerptEn(request.getExcerptEn())
                .excerptPl(request.getExcerptPl())
                .contentEn(request.getContentEn())
                .contentPl(request.getContentPl())
                .featuredImage(request.getFeaturedImage())
                .githubUrl(request.getGithubUrl())
                .liveUrl(request.getLiveUrl())
                .published(request.getPublished() != null ? request.getPublished() : false)
                .hashtags(request.getHashtags() != null ? request.getHashtags().toArray(new String[0]) : new String[0])
                .displayOrder(request.getDisplayOrder() != null ? request.getDisplayOrder() :
                        postRepository.getMaxDisplayOrder(request.getCategory()) + 1)
                .caseStudyChallengeEn(request.getCaseStudyChallengeEn())
                .caseStudyChallengePl(request.getCaseStudyChallengePl())
                .caseStudySolutionEn(request.getCaseStudySolutionEn())
                .caseStudySolutionPl(request.getCaseStudySolutionPl())
                .caseStudyResultsEn(request.getCaseStudyResultsEn())
                .caseStudyResultsPl(request.getCaseStudyResultsPl())
                .caseStudyTestimonialEn(request.getCaseStudyTestimonialEn())
                .caseStudyTestimonialPl(request.getCaseStudyTestimonialPl())
                .caseStudyTestimonialAuthor(request.getCaseStudyTestimonialAuthor())
                .build();

        post = postRepository.save(post);
        return mapToDto(post, locale, View.ADMIN_DETAIL);
    }

    @Transactional
    public PostDto updatePost(UUID id, PostUpdateRequest request, String locale) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Post not found: " + id));

        if (request.getCategory() != null) {
            post.setCategory(request.getCategory());
        }
        if (request.getTitleEn() != null) {
            post.setTitleEn(request.getTitleEn());
        }
        if (request.getTitlePl() != null) {
            post.setTitlePl(request.getTitlePl());
        }
        // A cleared or resent slug keeps the current one, so a published URL never silently changes or empties.
        // The raw comparison comes first: legacy slugs stored before normalization must survive unrelated edits.
        if (request.getSlug() != null && !request.getSlug().isBlank()
                && !request.getSlug().equals(post.getSlug())) {
            String slug = SlugUtils.toSlug(request.getSlug());
            if (!slug.equals(post.getSlug())) {
                if (postRepository.existsBySlug(slug)) {
                    throw new IllegalArgumentException("Slug already in use");
                }
                post.setSlug(slug);
            }
        }
        if (request.getExcerptEn() != null) {
            post.setExcerptEn(request.getExcerptEn());
        }
        if (request.getExcerptPl() != null) {
            post.setExcerptPl(request.getExcerptPl());
        }
        if (request.getContentEn() != null) {
            post.setContentEn(request.getContentEn());
        }
        if (request.getContentPl() != null) {
            post.setContentPl(request.getContentPl());
        }
        if (request.getFeaturedImage() != null) {
            post.setFeaturedImage(request.getFeaturedImage());
        }
        if (request.getGithubUrl() != null) {
            post.setGithubUrl(request.getGithubUrl());
        }
        if (request.getLiveUrl() != null) {
            post.setLiveUrl(request.getLiveUrl());
        }
        if (request.getPublished() != null) {
            post.setPublished(request.getPublished());
        }
        if (request.getDisplayOrder() != null) {
            post.setDisplayOrder(request.getDisplayOrder());
        }
        if (request.getHashtags() != null) {
            post.setHashtags(request.getHashtags().toArray(new String[0]));
        }
        // Case Study fields
        if (request.getCaseStudyChallengeEn() != null) {
            post.setCaseStudyChallengeEn(request.getCaseStudyChallengeEn());
        }
        if (request.getCaseStudyChallengePl() != null) {
            post.setCaseStudyChallengePl(request.getCaseStudyChallengePl());
        }
        if (request.getCaseStudySolutionEn() != null) {
            post.setCaseStudySolutionEn(request.getCaseStudySolutionEn());
        }
        if (request.getCaseStudySolutionPl() != null) {
            post.setCaseStudySolutionPl(request.getCaseStudySolutionPl());
        }
        if (request.getCaseStudyResultsEn() != null) {
            post.setCaseStudyResultsEn(request.getCaseStudyResultsEn());
        }
        if (request.getCaseStudyResultsPl() != null) {
            post.setCaseStudyResultsPl(request.getCaseStudyResultsPl());
        }
        if (request.getCaseStudyTestimonialEn() != null) {
            post.setCaseStudyTestimonialEn(request.getCaseStudyTestimonialEn());
        }
        if (request.getCaseStudyTestimonialPl() != null) {
            post.setCaseStudyTestimonialPl(request.getCaseStudyTestimonialPl());
        }
        if (request.getCaseStudyTestimonialAuthor() != null) {
            post.setCaseStudyTestimonialAuthor(request.getCaseStudyTestimonialAuthor());
        }

        post = postRepository.save(post);
        return mapToDto(post, locale, View.ADMIN_DETAIL);
    }

    @Transactional
    public void deletePost(UUID id) {
        if (!postRepository.existsById(id)) {
            throw new EntityNotFoundException("Post not found: " + id);
        }
        mediaService.deleteAllForPost(id);
        postRepository.deleteById(id);
    }

    @Transactional
    public PostDto togglePublish(UUID id, String locale) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Post not found: " + id));
        post.setPublished(!post.getPublished());
        post = postRepository.save(post);
        return mapToDto(post, locale, View.ADMIN_DETAIL);
    }

    @Transactional
    public void reorderPosts(ReorderRequest request) {
        for (ReorderRequest.OrderItem item : request.getItems()) {
            postRepository.updateDisplayOrder(item.getId(), item.getDisplayOrder());
        }
    }

    private String generateUniqueSlug(String source) {
        String baseSlug = SlugUtils.toSlug(source);
        String slug = baseSlug;
        int counter = 1;

        while (postRepository.existsBySlug(slug)) {
            slug = baseSlug + "-" + counter++;
        }

        return slug;
    }

    /**
     * Public responses carry only the locale-resolved texts: the site never reads the raw *En/*Pl fields, and
     * lists never show case-study texts or content. Admin responses keep the raw fields the edit form needs.
     */
    private enum View {
        PUBLIC_LIST(false, false), PUBLIC_DETAIL(false, true), ADMIN_LIST(true, false), ADMIN_DETAIL(true, true);

        private final boolean admin;
        private final boolean detail;

        View(boolean admin, boolean detail) {
            this.admin = admin;
            this.detail = detail;
        }
    }

    // Checked per locale because the page shows only that locale's fields (no fallback between languages).
    private static boolean hasCaseStudy(Post post, String locale) {
        return Stream.of(post.getCaseStudyChallenge(locale), post.getCaseStudySolution(locale),
                        post.getCaseStudyResults(locale), post.getCaseStudyTestimonial(locale))
                .anyMatch(text -> text != null && !text.isBlank());
    }

    private PostDto mapToDto(Post post, String locale, View view) {
        PostDto.PostDtoBuilder builder = PostDto.builder()
                .id(post.getId())
                .category(post.getCategory())
                .categoryLabel(post.getCategory().getLabel(locale))
                .title(post.getTitle(locale))
                .slug(post.getSlug())
                .excerpt(post.getExcerpt(locale))
                .featuredImage(post.getFeaturedImage())
                .githubUrl(post.getGithubUrl())
                .liveUrl(post.getLiveUrl())
                .published(post.getPublished())
                .displayOrder(post.getDisplayOrder())
                .hashtags(post.getHashtags() != null ? Arrays.asList(post.getHashtags()) : new ArrayList<>())
                .createdAt(post.getCreatedAt())
                .updatedAt(post.getUpdatedAt())
                .hasCaseStudy(hasCaseStudy(post, locale));

        if (view.admin || view.detail) {
            builder.caseStudyChallenge(post.getCaseStudyChallenge(locale))
                    .caseStudySolution(post.getCaseStudySolution(locale))
                    .caseStudyResults(post.getCaseStudyResults(locale))
                    .caseStudyTestimonial(post.getCaseStudyTestimonial(locale))
                    .caseStudyTestimonialAuthor(post.getCaseStudyTestimonialAuthor());
        }

        if (view.admin) {
            builder.titleEn(post.getTitleEn())
                    .titlePl(post.getTitlePl())
                    .excerptEn(post.getExcerptEn())
                    .excerptPl(post.getExcerptPl())
                    .caseStudyChallengeEn(post.getCaseStudyChallengeEn())
                    .caseStudyChallengePl(post.getCaseStudyChallengePl())
                    .caseStudySolutionEn(post.getCaseStudySolutionEn())
                    .caseStudySolutionPl(post.getCaseStudySolutionPl())
                    .caseStudyResultsEn(post.getCaseStudyResultsEn())
                    .caseStudyResultsPl(post.getCaseStudyResultsPl())
                    .caseStudyTestimonialEn(post.getCaseStudyTestimonialEn())
                    .caseStudyTestimonialPl(post.getCaseStudyTestimonialPl());
        }

        if (view.detail) {
            builder.content(post.getContent(locale));
            if (view.admin) {
                builder.contentEn(post.getContentEn())
                        .contentPl(post.getContentPl());
            }

            // Map media ordered by displayOrder
            if (post.getMedia() != null && !post.getMedia().isEmpty()) {
                builder.media(post.getMedia().stream()
                        .sorted((a, b) -> {
                            int orderA = a.getDisplayOrder() != null ? a.getDisplayOrder() : 0;
                            int orderB = b.getDisplayOrder() != null ? b.getDisplayOrder() : 0;
                            return Integer.compare(orderA, orderB);
                        })
                        .map(media -> mapMediaToDto(media, locale))
                        .toList());
            }
        }

        return builder.build();
    }

    private MediaDto mapMediaToDto(Media media, String locale) {
        return MediaDto.builder()
                .id(media.getId())
                .postId(media.getPost() != null ? media.getPost().getId() : null)
                .type(media.getType())
                .filename(media.getFilename())
                .originalName(media.getOriginalName())
                .mimeType(media.getMimeType())
                .size(media.getSize())
                .url(media.getUrl())
                .altText(media.getAltText(locale))
                .altTextEn(media.getAltTextEn())
                .altTextPl(media.getAltTextPl())
                .displayOrder(media.getDisplayOrder())
                .videoUrl(media.getVideoUrl())
                .createdAt(media.getCreatedAt())
                .build();
    }
}
