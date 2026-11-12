package com.portfolio.dto;

import com.portfolio.controller.AdminAchievementController;
import com.portfolio.controller.AdminExperienceController;
import com.portfolio.controller.AdminInterestController;
import com.portfolio.controller.AdminMediaController;
import com.portfolio.controller.AdminPostController;
import com.portfolio.controller.AdminRpgStatController;
import com.portfolio.controller.AdminSettingsController;
import com.portfolio.controller.AdminSkillCategoryController;
import com.portfolio.controller.AdminSoftSkillController;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Valid;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Admin request DTO validation")
class RequestDtoValidationTest {

    private static ValidatorFactory validatorFactory;
    private static Validator validator;

    @BeforeAll
    static void setUp() {
        validatorFactory = Validation.buildDefaultValidatorFactory();
        validator = validatorFactory.getValidator();
    }

    @AfterAll
    static void tearDown() {
        validatorFactory.close();
    }

    private static Set<String> violatedFields(Object dto) {
        return validator.validate(dto).stream()
                .map(ConstraintViolation::getPropertyPath)
                .map(Object::toString)
                .collect(Collectors.toSet());
    }

    /** The limit is inclusive: max chars pass, max + 1 chars fail on exactly that field. */
    private static <T> void assertSizeLimit(Supplier<T> newDto, BiConsumer<T, String> setter,
                                            String field, int max) {
        T atLimit = newDto.get();
        setter.accept(atLimit, "x".repeat(max));
        assertThat(violatedFields(atLimit)).as("%s at %d chars", field, max).doesNotContain(field);

        T overLimit = newDto.get();
        setter.accept(overLimit, "x".repeat(max + 1));
        assertThat(violatedFields(overLimit)).as("%s at %d chars", field, max + 1).contains(field);
    }

    /** Update DTOs: a field that is required on create must not be blank when present. */
    private static <T> void assertRejectsBlank(Supplier<T> newDto, BiConsumer<T, String> setter, String field) {
        for (String blank : List.of("", " ", "   ", "\t", "\n")) {
            T dto = newDto.get();
            setter.accept(dto, blank);
            assertThat(violatedFields(dto)).as("%s = %s", field, blank.isEmpty() ? "<empty>" : "<whitespace>")
                    .contains(field);
        }
        T padded = newDto.get();
        setter.accept(padded, " title\n");
        assertThat(violatedFields(padded)).as("%s with surrounding whitespace", field).doesNotContain(field);
    }

    @Test
    @DisplayName("PostCreateRequest and PostUpdateRequest mirror posts column limits")
    void postLimits() {
        assertSizeLimit(PostCreateRequest::new, PostCreateRequest::setTitleEn, "titleEn", 255);
        assertSizeLimit(PostCreateRequest::new, PostCreateRequest::setTitlePl, "titlePl", 255);
        assertSizeLimit(PostCreateRequest::new, PostCreateRequest::setSlug, "slug", 255);
        assertSizeLimit(PostCreateRequest::new, PostCreateRequest::setFeaturedImage, "featuredImage", 500);
        assertSizeLimit(PostCreateRequest::new, PostCreateRequest::setGithubUrl, "githubUrl", 500);
        assertSizeLimit(PostCreateRequest::new, PostCreateRequest::setLiveUrl, "liveUrl", 500);
        assertSizeLimit(PostUpdateRequest::new, PostUpdateRequest::setGithubUrl, "githubUrl", 500);
        assertSizeLimit(PostUpdateRequest::new, PostUpdateRequest::setLiveUrl, "liveUrl", 500);
        assertSizeLimit(PostCreateRequest::new, PostCreateRequest::setCaseStudyTestimonialAuthor,
                "caseStudyTestimonialAuthor", 255);

        assertSizeLimit(PostUpdateRequest::new, PostUpdateRequest::setTitleEn, "titleEn", 255);
        assertSizeLimit(PostUpdateRequest::new, PostUpdateRequest::setTitlePl, "titlePl", 255);
        assertSizeLimit(PostUpdateRequest::new, PostUpdateRequest::setSlug, "slug", 255);
        assertSizeLimit(PostUpdateRequest::new, PostUpdateRequest::setFeaturedImage, "featuredImage", 500);
        assertSizeLimit(PostUpdateRequest::new, PostUpdateRequest::setCaseStudyTestimonialAuthor,
                "caseStudyTestimonialAuthor", 255);
    }

    @Test
    @DisplayName("PostUpdateRequest rejects blank titles, accepts all-null")
    void postUpdateBlank() {
        assertRejectsBlank(PostUpdateRequest::new, PostUpdateRequest::setTitleEn, "titleEn");
        assertRejectsBlank(PostUpdateRequest::new, PostUpdateRequest::setTitlePl, "titlePl");
        assertThat(violatedFields(new PostUpdateRequest())).isEmpty();
    }

    @Test
    @DisplayName("Experience requests mirror experiences column limits and reject blank on update")
    void experience() {
        assertSizeLimit(ExperienceCreateRequest::new, ExperienceCreateRequest::setTitleEn, "titleEn", 255);
        assertSizeLimit(ExperienceCreateRequest::new, ExperienceCreateRequest::setTitlePl, "titlePl", 255);
        assertSizeLimit(ExperienceCreateRequest::new, ExperienceCreateRequest::setCompany, "company", 255);
        assertSizeLimit(ExperienceUpdateRequest::new, ExperienceUpdateRequest::setTitleEn, "titleEn", 255);
        assertSizeLimit(ExperienceUpdateRequest::new, ExperienceUpdateRequest::setTitlePl, "titlePl", 255);
        assertSizeLimit(ExperienceUpdateRequest::new, ExperienceUpdateRequest::setCompany, "company", 255);

        assertRejectsBlank(ExperienceUpdateRequest::new, ExperienceUpdateRequest::setTitleEn, "titleEn");
        assertRejectsBlank(ExperienceUpdateRequest::new, ExperienceUpdateRequest::setTitlePl, "titlePl");
        assertRejectsBlank(ExperienceUpdateRequest::new, ExperienceUpdateRequest::setCompany, "company");
        assertThat(violatedFields(new ExperienceUpdateRequest())).isEmpty();
    }

    @Test
    @DisplayName("Interest requests mirror interests column limits and reject blank on update")
    void interest() {
        assertSizeLimit(InterestCreateRequest::new, InterestCreateRequest::setTitleEn, "titleEn", 255);
        assertSizeLimit(InterestCreateRequest::new, InterestCreateRequest::setTitlePl, "titlePl", 255);
        assertSizeLimit(InterestCreateRequest::new, InterestCreateRequest::setImage1, "image1", 500);
        assertSizeLimit(InterestCreateRequest::new, InterestCreateRequest::setImage2, "image2", 500);
        assertSizeLimit(InterestCreateRequest::new, InterestCreateRequest::setImage3, "image3", 500);
        assertSizeLimit(InterestUpdateRequest::new, InterestUpdateRequest::setTitleEn, "titleEn", 255);
        assertSizeLimit(InterestUpdateRequest::new, InterestUpdateRequest::setTitlePl, "titlePl", 255);
        assertSizeLimit(InterestUpdateRequest::new, InterestUpdateRequest::setImage1, "image1", 500);
        assertSizeLimit(InterestUpdateRequest::new, InterestUpdateRequest::setImage2, "image2", 500);
        assertSizeLimit(InterestUpdateRequest::new, InterestUpdateRequest::setImage3, "image3", 500);

        assertRejectsBlank(InterestUpdateRequest::new, InterestUpdateRequest::setTitleEn, "titleEn");
        assertRejectsBlank(InterestUpdateRequest::new, InterestUpdateRequest::setTitlePl, "titlePl");
        assertThat(violatedFields(new InterestUpdateRequest())).isEmpty();
    }

    @Test
    @DisplayName("SkillCategory requests mirror skill_categories column limits and reject blank on update")
    void skillCategory() {
        assertSizeLimit(SkillCategoryCreateRequest::new, SkillCategoryCreateRequest::setNameEn, "nameEn", 255);
        assertSizeLimit(SkillCategoryCreateRequest::new, SkillCategoryCreateRequest::setNamePl, "namePl", 255);
        assertSizeLimit(SkillCategoryUpdateRequest::new, SkillCategoryUpdateRequest::setNameEn, "nameEn", 255);
        assertSizeLimit(SkillCategoryUpdateRequest::new, SkillCategoryUpdateRequest::setNamePl, "namePl", 255);

        assertRejectsBlank(SkillCategoryUpdateRequest::new, SkillCategoryUpdateRequest::setNameEn, "nameEn");
        assertRejectsBlank(SkillCategoryUpdateRequest::new, SkillCategoryUpdateRequest::setNamePl, "namePl");
        assertThat(violatedFields(new SkillCategoryUpdateRequest())).isEmpty();
    }

    @Test
    @DisplayName("SoftSkill requests mirror soft_skills column limits and reject blank on update")
    void softSkill() {
        assertSizeLimit(SoftSkillCreateRequest::new, SoftSkillCreateRequest::setNameEn, "nameEn", 255);
        assertSizeLimit(SoftSkillCreateRequest::new, SoftSkillCreateRequest::setNamePl, "namePl", 255);
        assertSizeLimit(SoftSkillCreateRequest::new, SoftSkillCreateRequest::setIcon, "icon", 100);
        assertSizeLimit(SoftSkillUpdateRequest::new, SoftSkillUpdateRequest::setNameEn, "nameEn", 255);
        assertSizeLimit(SoftSkillUpdateRequest::new, SoftSkillUpdateRequest::setNamePl, "namePl", 255);
        assertSizeLimit(SoftSkillUpdateRequest::new, SoftSkillUpdateRequest::setIcon, "icon", 100);

        assertRejectsBlank(SoftSkillUpdateRequest::new, SoftSkillUpdateRequest::setNameEn, "nameEn");
        assertRejectsBlank(SoftSkillUpdateRequest::new, SoftSkillUpdateRequest::setNamePl, "namePl");
        assertThat(violatedFields(new SoftSkillUpdateRequest())).isEmpty();
    }

    @Test
    @DisplayName("Achievement requests mirror achievements column limits and reject blank on update")
    void achievement() {
        assertSizeLimit(AchievementCreateRequest::new, AchievementCreateRequest::setTitleEn, "titleEn", 255);
        assertSizeLimit(AchievementCreateRequest::new, AchievementCreateRequest::setTitlePl, "titlePl", 255);
        assertSizeLimit(AchievementCreateRequest::new, AchievementCreateRequest::setIcon, "icon", 50);
        assertSizeLimit(AchievementCreateRequest::new, AchievementCreateRequest::setYear, "year", 10);
        assertSizeLimit(AchievementUpdateRequest::new, AchievementUpdateRequest::setTitleEn, "titleEn", 255);
        assertSizeLimit(AchievementUpdateRequest::new, AchievementUpdateRequest::setTitlePl, "titlePl", 255);
        assertSizeLimit(AchievementUpdateRequest::new, AchievementUpdateRequest::setIcon, "icon", 50);
        assertSizeLimit(AchievementUpdateRequest::new, AchievementUpdateRequest::setYear, "year", 10);

        assertRejectsBlank(AchievementUpdateRequest::new, AchievementUpdateRequest::setTitleEn, "titleEn");
        assertRejectsBlank(AchievementUpdateRequest::new, AchievementUpdateRequest::setTitlePl, "titlePl");
        assertThat(violatedFields(new AchievementUpdateRequest())).isEmpty();
    }

    @Test
    @DisplayName("RpgStat requests mirror rpg_stats column limits and reject blank on update")
    void rpgStat() {
        assertSizeLimit(RpgStatCreateRequest::new, RpgStatCreateRequest::setAttr, "attr", 10);
        assertSizeLimit(RpgStatCreateRequest::new, RpgStatCreateRequest::setLabelEn, "labelEn", 255);
        assertSizeLimit(RpgStatCreateRequest::new, RpgStatCreateRequest::setLabelPl, "labelPl", 255);
        assertSizeLimit(RpgStatUpdateRequest::new, RpgStatUpdateRequest::setAttr, "attr", 10);
        assertSizeLimit(RpgStatUpdateRequest::new, RpgStatUpdateRequest::setLabelEn, "labelEn", 255);
        assertSizeLimit(RpgStatUpdateRequest::new, RpgStatUpdateRequest::setLabelPl, "labelPl", 255);

        assertRejectsBlank(RpgStatUpdateRequest::new, RpgStatUpdateRequest::setAttr, "attr");
        assertRejectsBlank(RpgStatUpdateRequest::new, RpgStatUpdateRequest::setLabelEn, "labelEn");
        assertRejectsBlank(RpgStatUpdateRequest::new, RpgStatUpdateRequest::setLabelPl, "labelPl");
        assertThat(violatedFields(new RpgStatUpdateRequest())).isEmpty();
    }

    @Test
    @DisplayName("Create requests still reject missing required fields")
    void createStillRequiresFields() {
        assertThat(violatedFields(new PostCreateRequest())).contains("titleEn", "titlePl", "category");
        assertThat(violatedFields(new ExperienceCreateRequest())).contains("titleEn", "titlePl", "company", "startDate");
        assertThat(violatedFields(new InterestCreateRequest())).contains("titleEn", "titlePl");
        assertThat(violatedFields(new AchievementCreateRequest())).contains("titleEn", "titlePl");
        assertThat(violatedFields(new RpgStatCreateRequest())).contains("attr", "labelEn", "labelPl");
        assertThat(violatedFields(new SkillCategoryCreateRequest())).contains("nameEn", "namePl");
        assertThat(violatedFields(new SoftSkillCreateRequest())).contains("nameEn", "namePl");
    }

    @Test
    @DisplayName("SiteSettingsUpdateRequest mirrors site_settings column limits")
    void siteSettingsLimits() {
        assertSizeLimit(SiteSettingsUpdateRequest::new, SiteSettingsUpdateRequest::setHeroTitleEn, "heroTitleEn", 255);
        assertSizeLimit(SiteSettingsUpdateRequest::new, SiteSettingsUpdateRequest::setHeroTitlePl, "heroTitlePl", 255);
        assertSizeLimit(SiteSettingsUpdateRequest::new, SiteSettingsUpdateRequest::setFooterTitleEn, "footerTitleEn", 255);
        assertSizeLimit(SiteSettingsUpdateRequest::new, SiteSettingsUpdateRequest::setFooterTitlePl, "footerTitlePl", 255);
        assertSizeLimit(SiteSettingsUpdateRequest::new, SiteSettingsUpdateRequest::setOwnerName, "ownerName", 255);
        assertSizeLimit(SiteSettingsUpdateRequest::new, SiteSettingsUpdateRequest::setSiteName, "siteName", 255);
        assertSizeLimit(SiteSettingsUpdateRequest::new, SiteSettingsUpdateRequest::setProfileImage, "profileImage", 500);
        assertSizeLimit(SiteSettingsUpdateRequest::new, SiteSettingsUpdateRequest::setPhone, "phone", 50);
        assertSizeLimit(SiteSettingsUpdateRequest::new, SiteSettingsUpdateRequest::setRpgClassTitleEn, "rpgClassTitleEn", 255);
        assertSizeLimit(SiteSettingsUpdateRequest::new, SiteSettingsUpdateRequest::setRpgClassTitlePl, "rpgClassTitlePl", 255);
    }

    @Test
    @DisplayName("SiteSettingsUpdateRequest validates the e-mail format and length")
    void siteSettingsEmail() {
        SiteSettingsUpdateRequest valid = new SiteSettingsUpdateRequest();
        valid.setEmail("owner@example.com");
        assertThat(violatedFields(valid)).isEmpty();

        for (String invalid : List.of("not-an-email", "a@", "@b.pl", "a b@c.pl")) {
            SiteSettingsUpdateRequest dto = new SiteSettingsUpdateRequest();
            dto.setEmail(invalid);
            assertThat(violatedFields(dto)).as("email = %s", invalid).contains("email");
        }

        SiteSettingsUpdateRequest tooLong = new SiteSettingsUpdateRequest();
        tooLong.setEmail("a".repeat(250) + "@b.pl");
        assertThat(violatedFields(tooLong)).contains("email");

        assertThat(violatedFields(new SiteSettingsUpdateRequest())).isEmpty();
    }

    @Test
    @DisplayName("ReorderRequest requires a non-empty item list with id and displayOrder")
    void reorderRequest() {
        assertThat(violatedFields(new ReorderRequest())).contains("items");
        assertThat(violatedFields(ReorderRequest.builder().items(List.of()).build())).contains("items");

        ReorderRequest nullItem = new ReorderRequest();
        nullItem.setItems(Collections.singletonList(null));
        assertThat(violatedFields(nullItem)).isNotEmpty();

        ReorderRequest noId = ReorderRequest.builder()
                .items(List.of(new ReorderRequest.OrderItem(null, 1))).build();
        assertThat(violatedFields(noId)).contains("items[0].id");

        ReorderRequest noOrder = ReorderRequest.builder()
                .items(List.of(new ReorderRequest.OrderItem(UUID.randomUUID(), null))).build();
        assertThat(violatedFields(noOrder)).contains("items[0].displayOrder");

        ReorderRequest valid = ReorderRequest.builder()
                .items(List.of(new ReorderRequest.OrderItem(UUID.randomUUID(), 0))).build();
        assertThat(violatedFields(valid)).isEmpty();
    }

    @Test
    @DisplayName("ContactRequest honeypot field is optional and capped at 200 characters")
    void contactHoneypot() {
        assertSizeLimit(ContactRequest::new, ContactRequest::setWebsite, "website", 200);

        ContactRequest withoutHoneypot = new ContactRequest();
        withoutHoneypot.setWebsite(null);
        assertThat(violatedFields(withoutHoneypot)).doesNotContain("website");
        withoutHoneypot.setWebsite("");
        assertThat(violatedFields(withoutHoneypot)).doesNotContain("website");
    }

    @Test
    @DisplayName("Every admin @RequestBody parameter is annotated with @Valid")
    void adminControllersValidateRequestBodies() {
        List<Class<?>> controllers = List.of(
                AdminPostController.class, AdminExperienceController.class, AdminInterestController.class,
                AdminAchievementController.class, AdminRpgStatController.class, AdminSkillCategoryController.class,
                AdminSoftSkillController.class, AdminSettingsController.class, AdminMediaController.class);

        List<String> unvalidated = controllers.stream()
                .flatMap(controller -> Arrays.stream(controller.getDeclaredMethods()))
                .flatMap(method -> Arrays.stream(method.getParameters())
                        .filter(parameter -> parameter.isAnnotationPresent(RequestBody.class))
                        .filter(parameter -> !parameter.isAnnotationPresent(Valid.class))
                        .map(parameter -> method.getDeclaringClass().getSimpleName() + "." + method.getName()))
                .toList();

        assertThat(unvalidated).isEmpty();
    }
}
