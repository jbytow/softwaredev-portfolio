package com.portfolio.dto;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import com.portfolio.entity.Category;
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
public class PostUpdateRequest {

    private Category category;
    @Size(max = 255, message = "English title must not exceed 255 characters")
    @Pattern(regexp = "(?s).*\\S.*", message = "English title must not be blank")
    private String titleEn;
    @Size(max = 255, message = "Polish title must not exceed 255 characters")
    @Pattern(regexp = "(?s).*\\S.*", message = "Polish title must not be blank")
    private String titlePl;
    @Size(max = 255, message = "Slug must not exceed 255 characters")
    private String slug;
    private String excerptEn;
    private String excerptPl;
    private Map<String, Object> contentEn;
    private Map<String, Object> contentPl;
    @Size(max = 500, message = "Featured image must not exceed 500 characters")
    private String featuredImage;
    @Size(max = 500, message = "GitHub URL must not exceed 500 characters")
    private String githubUrl;
    @Size(max = 500, message = "Live URL must not exceed 500 characters")
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
    @Size(max = 255, message = "Testimonial author must not exceed 255 characters")
    private String caseStudyTestimonialAuthor;
}
