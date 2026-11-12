package com.portfolio.controller;

import com.portfolio.config.TestSecurityConfig;
import com.portfolio.dto.AchievementDto;
import com.portfolio.dto.ExperienceDto;
import com.portfolio.dto.InterestDto;
import com.portfolio.dto.MediaDto;
import com.portfolio.dto.PostDto;
import com.portfolio.dto.RpgStatDto;
import com.portfolio.dto.SiteSettingsDto;
import com.portfolio.dto.SkillCategoryWithSkillsDto;
import com.portfolio.dto.SoftSkillDto;
import com.portfolio.dto.StatItemDto;
import com.portfolio.entity.Category;
import com.portfolio.entity.MediaType;
import com.portfolio.security.JwtService;
import com.portfolio.service.AchievementService;
import com.portfolio.service.ContactService;
import com.portfolio.service.ExperienceService;
import com.portfolio.service.InterestService;
import com.portfolio.service.MediaService;
import com.portfolio.service.PostService;
import com.portfolio.service.RpgStatService;
import com.portfolio.service.SiteSettingsService;
import com.portfolio.service.SkillCategoryService;
import com.portfolio.service.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.RequestBuilder;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Pins the JSON the frontend reads from the public endpoints (and the admin media page),
 * so a change of the Jackson version or its defaults cannot alter field names, date formats,
 * explicit nulls or the envelope unnoticed. Property order is not part of the contract.
 */
@WebMvcTest(
        controllers = {
                PostController.class,
                ExperienceController.class,
                InterestController.class,
                SkillCategoryController.class,
                SiteSettingsController.class,
                AchievementController.class,
                RpgStatController.class,
                AdminMediaController.class,
                ContactController.class
        },
        excludeAutoConfiguration = {
                org.springframework.boot.security.oauth2.client.autoconfigure.OAuth2ClientAutoConfiguration.class
        }
)
@Import(TestSecurityConfig.class)
@ActiveProfiles("test")
@DisplayName("JSON wire format")
class JsonWireFormatTest {

    private static final UUID ID = UUID.fromString("00000000-0000-0000-0000-000000000001");
    private static final UUID OTHER_ID = UUID.fromString("00000000-0000-0000-0000-000000000002");
    private static final OffsetDateTime CREATED = OffsetDateTime.of(2026, 11, 12, 10, 30, 0, 0, ZoneOffset.ofHours(1));
    private static final OffsetDateTime UPDATED = OffsetDateTime.of(2026, 11, 12, 9, 15, 30, 123_000_000, ZoneOffset.UTC);

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PostService postService;

    @MockitoBean
    private ExperienceService experienceService;

    @MockitoBean
    private InterestService interestService;

    @MockitoBean
    private SkillCategoryService skillCategoryService;

    @MockitoBean
    private SiteSettingsService siteSettingsService;

    @MockitoBean
    private AchievementService achievementService;

    @MockitoBean
    private RpgStatService rpgStatService;

    @MockitoBean
    private MediaService mediaService;

    @MockitoBean
    private ContactService contactService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private UserService userService;

    private void assertBody(RequestBuilder request, int expectedStatus, String expectedJson, JSONCompareMode mode)
            throws Exception {
        String body = mockMvc.perform(request)
                .andExpect(status().is(expectedStatus))
                .andReturn().getResponse().getContentAsString();
        JSONAssert.assertEquals(expectedJson, body, mode);
    }

    private static MediaDto sampleMedia() {
        return MediaDto.builder()
                .id(OTHER_ID)
                .postId(ID)
                .type(MediaType.IMAGE)
                .filename("abc.jpg")
                .originalName("photo.jpg")
                .mimeType("image/jpeg")
                .size(2048L)
                .url("/api/media/images/abc.jpg")
                .altText("Alt")
                .altTextEn("Alt")
                .displayOrder(0)
                .createdAt(CREATED)
                .build();
    }

    private static final String MEDIA_JSON = """
            {"id":"00000000-0000-0000-0000-000000000002","postId":"00000000-0000-0000-0000-000000000001",
             "type":"IMAGE","filename":"abc.jpg","originalName":"photo.jpg","mimeType":"image/jpeg","size":2048,
             "url":"/api/media/images/abc.jpg","altText":"Alt","altTextEn":"Alt","altTextPl":null,"displayOrder":0,
             "videoUrl":null,"createdAt":"2026-11-12T10:30:00+01:00"}
            """;

    @Test
    @DisplayName("post detail keeps field names, ISO timestamps with their offset, JSONB content and explicit nulls")
    void postDetail() throws Exception {
        Map<String, Object> content = new LinkedHashMap<>();
        content.put("type", "doc");
        content.put("content", List.of(Map.of("type", "paragraph", "attrs", Map.of("level", 2))));
        PostDto post = PostDto.builder()
                .id(ID)
                .category(Category.PERSONAL_PROJECT)
                .categoryLabel("Personal Project")
                .title("Sample Post")
                .titleEn("Sample Post")
                .slug("sample-post")
                .excerpt("Short")
                .content(content)
                .githubUrl("https://github.com/example/repo")
                .published(true)
                .displayOrder(3)
                .hashtags(List.of("java", "spring"))
                .createdAt(CREATED)
                .updatedAt(UPDATED)
                .hasCaseStudy(false)
                .media(List.of(sampleMedia()))
                .build();
        given(postService.getPostBySlug("sample-post", "en")).willReturn(post);

        assertBody(get("/api/posts/sample-post"), 200, """
                {"success":true,"data":{
                  "id":"00000000-0000-0000-0000-000000000001","category":"PERSONAL_PROJECT",
                  "categoryLabel":"Personal Project","title":"Sample Post","titleEn":"Sample Post","titlePl":null,
                  "slug":"sample-post","excerpt":"Short","excerptEn":null,"excerptPl":null,
                  "content":{"type":"doc","content":[{"type":"paragraph","attrs":{"level":2}}]},
                  "contentEn":null,"contentPl":null,"featuredImage":null,
                  "githubUrl":"https://github.com/example/repo","liveUrl":null,"published":true,"displayOrder":3,
                  "hashtags":["java","spring"],
                  "createdAt":"2026-11-12T10:30:00+01:00","updatedAt":"2026-11-12T09:15:30.123Z",
                  "hasCaseStudy":false,
                  "caseStudyChallenge":null,"caseStudyChallengeEn":null,"caseStudyChallengePl":null,
                  "caseStudySolution":null,"caseStudySolutionEn":null,"caseStudySolutionPl":null,
                  "caseStudyResults":null,"caseStudyResultsEn":null,"caseStudyResultsPl":null,
                  "caseStudyTestimonial":null,"caseStudyTestimonialEn":null,"caseStudyTestimonialPl":null,
                  "caseStudyTestimonialAuthor":null,
                  "media":[%s]}}
                """.formatted(MEDIA_JSON), JSONCompareMode.STRICT);
    }

    @Test
    @DisplayName("experiences keep LocalDate as yyyy-MM-dd and lists of achievements")
    void experiences() throws Exception {
        ExperienceDto experience = ExperienceDto.builder()
                .id(ID)
                .title("Developer")
                .titleEn("Developer")
                .company("ACME")
                .startDate(LocalDate.of(2024, 3, 1))
                .description("Built things")
                .achievements(List.of("Shipped v1"))
                .achievementsEn(List.of("Shipped v1"))
                .displayOrder(0)
                .createdAt(CREATED)
                .build();
        given(experienceService.getAllExperiences("en")).willReturn(List.of(experience));

        assertBody(get("/api/experiences"), 200, """
                {"success":true,"data":[{
                  "id":"00000000-0000-0000-0000-000000000001","title":"Developer","titleEn":"Developer","titlePl":null,
                  "company":"ACME","startDate":"2024-03-01","endDate":null,
                  "description":"Built things","descriptionEn":null,"descriptionPl":null,
                  "achievements":["Shipped v1"],"achievementsEn":["Shipped v1"],"achievementsPl":null,
                  "displayOrder":0,"createdAt":"2026-11-12T10:30:00+01:00","updatedAt":null}]}
                """, JSONCompareMode.STRICT);
    }

    @Test
    @DisplayName("interests keep their image slots and timestamps")
    void interests() throws Exception {
        InterestDto interest = InterestDto.builder()
                .id(ID)
                .title("Climbing")
                .titleEn("Climbing")
                .titlePl("Wspinaczka")
                .image1("/api/media/images/a.jpg")
                .displayOrder(1)
                .createdAt(CREATED)
                .updatedAt(UPDATED)
                .build();
        given(interestService.getAll("en")).willReturn(List.of(interest));

        assertBody(get("/api/interests"), 200, """
                {"success":true,"data":[{
                  "id":"00000000-0000-0000-0000-000000000001","title":"Climbing","titleEn":"Climbing",
                  "titlePl":"Wspinaczka","image1":"/api/media/images/a.jpg","image2":null,"image3":null,
                  "displayOrder":1,"createdAt":"2026-11-12T10:30:00+01:00","updatedAt":"2026-11-12T09:15:30.123Z"}]}
                """, JSONCompareMode.STRICT);
    }

    @Test
    @DisplayName("skill categories nest their skills with all fields")
    void skillCategoriesWithSkills() throws Exception {
        SoftSkillDto skill = SoftSkillDto.builder()
                .id(OTHER_ID)
                .name("Java")
                .nameEn("Java")
                .icon("code")
                .level(5)
                .displayOrder(0)
                .categoryId(ID)
                .categoryName("Backend")
                .createdAt(CREATED)
                .build();
        SkillCategoryWithSkillsDto category = SkillCategoryWithSkillsDto.builder()
                .id(ID)
                .name("Backend")
                .nameEn("Backend")
                .displayOrder(0)
                .skills(List.of(skill))
                .createdAt(CREATED)
                .build();
        given(skillCategoryService.getAllCategoriesWithSkills("en")).willReturn(List.of(category));

        assertBody(get("/api/skill-categories/with-skills"), 200, """
                {"success":true,"data":[{
                  "id":"00000000-0000-0000-0000-000000000001","name":"Backend","nameEn":"Backend","namePl":null,
                  "displayOrder":0,
                  "skills":[{"id":"00000000-0000-0000-0000-000000000002","name":"Java","nameEn":"Java","namePl":null,
                    "description":null,"descriptionEn":null,"descriptionPl":null,
                    "professionalUsage":null,"professionalUsageEn":null,"professionalUsagePl":null,
                    "icon":"code","level":5,"displayOrder":0,
                    "categoryId":"00000000-0000-0000-0000-000000000001","categoryName":"Backend",
                    "createdAt":"2026-11-12T10:30:00+01:00","updatedAt":null}],
                  "createdAt":"2026-11-12T10:30:00+01:00","updatedAt":null}]}
                """, JSONCompareMode.STRICT);
    }

    @Test
    @DisplayName("site settings keep the RPG class title, social links map, tag arrays and stat items")
    void siteSettings() throws Exception {
        Map<String, String> socialLinks = new LinkedHashMap<>();
        socialLinks.put("github", "https://github.com/example");
        socialLinks.put("linkedin", "https://linkedin.com/in/example");
        SiteSettingsDto settings = SiteSettingsDto.builder()
                .heroTitle("Hi")
                .heroTitleEn("Hi")
                .heroTitlePl("Czesc")
                .email("me@example.org")
                .socialLinks(socialLinks)
                .ownerName("Jan")
                .siteName("Portfolio")
                .rpgClassTitle("Backend Paladin")
                .rpgClassTitleEn("Backend Paladin")
                .rpgClassTitlePl("Paladyn Backendu")
                .aboutTags(new String[]{"java", "spring"})
                .aboutTagsEn(new String[]{"java", "spring"})
                .statsItems(List.of(StatItemDto.builder().icon("star").value("10+").label("Years")
                        .labelEn("Years").build()))
                .build();
        given(siteSettingsService.getSettings("en")).willReturn(settings);

        assertBody(get("/api/settings"), 200, """
                {"success":true,"data":{
                  "heroTitle":"Hi","heroTitleEn":"Hi","heroTitlePl":"Czesc",
                  "heroSubtitle":null,"heroSubtitleEn":null,"heroSubtitlePl":null,
                  "aboutText":null,"aboutTextEn":null,"aboutTextPl":null,
                  "profileImage":null,"email":"me@example.org","phone":null,
                  "socialLinks":{"github":"https://github.com/example","linkedin":"https://linkedin.com/in/example"},
                  "metaDescription":null,"metaDescriptionEn":null,"metaDescriptionPl":null,
                  "footerTitle":null,"footerTitleEn":null,"footerTitlePl":null,
                  "footerTagline":null,"footerTaglineEn":null,"footerTaglinePl":null,
                  "ownerName":"Jan","siteName":"Portfolio",
                  "rpgClassTitle":"Backend Paladin","rpgClassTitleEn":"Backend Paladin",
                  "rpgClassTitlePl":"Paladyn Backendu",
                  "aboutTags":["java","spring"],"aboutTagsEn":["java","spring"],"aboutTagsPl":null,
                  "statsItems":[{"icon":"star","value":"10+","label":"Years","labelEn":"Years","labelPl":null}]}}
                """, JSONCompareMode.STRICT);
    }

    @Test
    @DisplayName("achievements keep the year as a string and timestamps")
    void achievements() throws Exception {
        AchievementDto achievement = AchievementDto.builder()
                .id(ID)
                .title("Hackathon")
                .titleEn("Hackathon")
                .icon("trophy")
                .year("2025")
                .displayOrder(0)
                .createdAt(CREATED)
                .updatedAt(UPDATED)
                .build();
        given(achievementService.getAll("en")).willReturn(List.of(achievement));

        assertBody(get("/api/achievements"), 200, """
                {"success":true,"data":[{
                  "id":"00000000-0000-0000-0000-000000000001","title":"Hackathon","titleEn":"Hackathon",
                  "titlePl":null,"description":null,"descriptionEn":null,"descriptionPl":null,
                  "icon":"trophy","year":"2025","displayOrder":0,
                  "createdAt":"2026-11-12T10:30:00+01:00","updatedAt":"2026-11-12T09:15:30.123Z"}]}
                """, JSONCompareMode.STRICT);
    }

    @Test
    @DisplayName("RPG stats keep levels and the skills list")
    void rpgStats() throws Exception {
        RpgStatDto stat = RpgStatDto.builder()
                .id(ID)
                .attr("STR")
                .label("Backend")
                .labelEn("Backend")
                .level(8)
                .maxLevel(10)
                .skills(List.of("Java", "SQL"))
                .displayOrder(0)
                .createdAt(CREATED)
                .build();
        given(rpgStatService.getAll("en")).willReturn(List.of(stat));

        assertBody(get("/api/rpg-stats"), 200, """
                {"success":true,"data":[{
                  "id":"00000000-0000-0000-0000-000000000001","attr":"STR","label":"Backend","labelEn":"Backend",
                  "labelPl":null,"level":8,"maxLevel":10,"skills":["Java","SQL"],"displayOrder":0,
                  "createdAt":"2026-11-12T10:30:00+01:00","updatedAt":null}]}
                """, JSONCompareMode.STRICT);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("the admin media page keeps the paging fields the admin panel reads")
    void adminMediaPage() throws Exception {
        Pageable pageable = PageRequest.of(0, 20);
        given(mediaService.getAllMedia(any(Pageable.class), any()))
                .willReturn(new PageImpl<>(List.of(sampleMedia()), pageable, 1));

        // Lenient: only the fields the frontend's PaginatedResponse declares are the contract
        assertBody(get("/api/admin/media"), 200, """
                {"success":true,"data":{"content":[%s],
                  "totalElements":1,"totalPages":1,"size":20,"number":0,"first":true,"last":true}}
                """.formatted(MEDIA_JSON), JSONCompareMode.LENIENT);
    }

    @Test
    @DisplayName("a validation failure keeps the error envelope with a field map and no data")
    void validationErrorEnvelope() throws Exception {
        assertBody(post("/api/contact")
                        .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Anna","email":"not-an-email","subject":"Hello","message":"Hi"}
                                """),
                400, """
                {"success":false,"message":"Validation failed","errors":{"email":"Invalid email format"}}
                """, JSONCompareMode.STRICT);
    }
}
