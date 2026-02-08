package com.portfolio.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.portfolio.config.CategoryConverter;

@Entity
@Table(name = "posts")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Post {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Convert(converter = CategoryConverter.class)
    @Column(name = "category", nullable = false)
    private Category category;

    @Column(name = "title_en", nullable = false)
    private String titleEn;

    @Column(name = "title_pl", nullable = false)
    private String titlePl;

    @Column(name = "slug", nullable = false, unique = true)
    private String slug;

    @Column(name = "excerpt_en", columnDefinition = "TEXT")
    private String excerptEn;

    @Column(name = "excerpt_pl", columnDefinition = "TEXT")
    private String excerptPl;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "content_en", columnDefinition = "jsonb")
    private Map<String, Object> contentEn;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "content_pl", columnDefinition = "jsonb")
    private Map<String, Object> contentPl;

    @Column(name = "featured_image")
    private String featuredImage;

    @Column(name = "github_url")
    private String githubUrl;

    @Column(name = "live_url")
    private String liveUrl;

    @Column(name = "published")
    @Builder.Default
    private Boolean published = false;

    @Column(name = "display_order")
    @Builder.Default
    private Integer displayOrder = 0;

    @Column(name = "hashtags", columnDefinition = "text[]")
    private String[] hashtags = new String[0];

    // Case Study fields
    @Column(name = "case_study_challenge_en", columnDefinition = "TEXT")
    private String caseStudyChallengeEn;

    @Column(name = "case_study_challenge_pl", columnDefinition = "TEXT")
    private String caseStudyChallengePl;

    @Column(name = "case_study_solution_en", columnDefinition = "TEXT")
    private String caseStudySolutionEn;

    @Column(name = "case_study_solution_pl", columnDefinition = "TEXT")
    private String caseStudySolutionPl;

    @Column(name = "case_study_results_en", columnDefinition = "TEXT")
    private String caseStudyResultsEn;

    @Column(name = "case_study_results_pl", columnDefinition = "TEXT")
    private String caseStudyResultsPl;

    @Column(name = "case_study_testimonial_en", columnDefinition = "TEXT")
    private String caseStudyTestimonialEn;

    @Column(name = "case_study_testimonial_pl", columnDefinition = "TEXT")
    private String caseStudyTestimonialPl;

    @Column(name = "case_study_testimonial_author")
    private String caseStudyTestimonialAuthor;

    @Column(name = "created_at")
    private OffsetDateTime createdAt;

    @Column(name = "updated_at")
    private OffsetDateTime updatedAt;

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL)
    @Builder.Default
    private List<Media> media = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        createdAt = OffsetDateTime.now();
        updatedAt = OffsetDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = OffsetDateTime.now();
    }

    public String getTitle(String locale) {
        return "pl".equalsIgnoreCase(locale) ? titlePl : titleEn;
    }

    public String getExcerpt(String locale) {
        return "pl".equalsIgnoreCase(locale) ? excerptPl : excerptEn;
    }

    public Map<String, Object> getContent(String locale) {
        return "pl".equalsIgnoreCase(locale) ? contentPl : contentEn;
    }

    public String getCaseStudyChallenge(String locale) {
        return "pl".equalsIgnoreCase(locale) ? caseStudyChallengePl : caseStudyChallengeEn;
    }

    public String getCaseStudySolution(String locale) {
        return "pl".equalsIgnoreCase(locale) ? caseStudySolutionPl : caseStudySolutionEn;
    }

    public String getCaseStudyResults(String locale) {
        return "pl".equalsIgnoreCase(locale) ? caseStudyResultsPl : caseStudyResultsEn;
    }

    public String getCaseStudyTestimonial(String locale) {
        return "pl".equalsIgnoreCase(locale) ? caseStudyTestimonialPl : caseStudyTestimonialEn;
    }

    public boolean hasCaseStudy() {
        return (caseStudyChallengeEn != null && !caseStudyChallengeEn.isBlank()) ||
               (caseStudySolutionEn != null && !caseStudySolutionEn.isBlank()) ||
               (caseStudyResultsEn != null && !caseStudyResultsEn.isBlank());
    }
}
