package com.portfolio.dto;

import com.portfolio.entity.Category;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PostCreateRequest {

    @NotNull(message = "Category is required")
    private Category category;

    @NotBlank(message = "English title is required")
    private String titleEn;

    @NotBlank(message = "Polish title is required")
    private String titlePl;

    private String slug;

    private String excerptEn;
    private String excerptPl;

    private Map<String, Object> contentEn;
    private Map<String, Object> contentPl;

    private String featuredImage;
    private String githubUrl;
    private String liveUrl;
    private Boolean published;
    private Integer displayOrder;
    private List<String> hashtags;

    // Case Study fields
    private String caseStudyChallengeEn;
    private String caseStudyChallengePl;
    private String caseStudySolutionEn;
    private String caseStudySolutionPl;
    private String caseStudyResultsEn;
    private String caseStudyResultsPl;
    private String caseStudyTestimonialEn;
    private String caseStudyTestimonialPl;
    private String caseStudyTestimonialAuthor;
}
