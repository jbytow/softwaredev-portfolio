package com.portfolio.dto;

import com.portfolio.entity.Category;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PostDto {
    private UUID id;
    private Category category;
    private String categoryLabel;
    private String title;
    private String titleEn;
    private String titlePl;
    private String slug;
    private String excerpt;
    private String excerptEn;
    private String excerptPl;
    private Map<String, Object> content;
    private Map<String, Object> contentEn;
    private Map<String, Object> contentPl;
    private String featuredImage;
    private String githubUrl;
    private String liveUrl;
    private Boolean published;
    private Integer displayOrder;
    private List<String> hashtags;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;

    // Case Study fields
    private Boolean hasCaseStudy;
    private String caseStudyChallenge;
    private String caseStudyChallengeEn;
    private String caseStudyChallengePl;
    private String caseStudySolution;
    private String caseStudySolutionEn;
    private String caseStudySolutionPl;
    private String caseStudyResults;
    private String caseStudyResultsEn;
    private String caseStudyResultsPl;
    private String caseStudyTestimonial;
    private String caseStudyTestimonialEn;
    private String caseStudyTestimonialPl;
    private String caseStudyTestimonialAuthor;

    // Related media
    private List<MediaDto> media;
}
