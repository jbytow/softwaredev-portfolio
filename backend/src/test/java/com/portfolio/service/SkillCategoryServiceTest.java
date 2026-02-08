package com.portfolio.service;

import com.portfolio.dto.SkillCategoryCreateRequest;
import com.portfolio.dto.SkillCategoryDto;
import com.portfolio.dto.SkillCategoryUpdateRequest;
import com.portfolio.dto.SkillCategoryWithSkillsDto;
import com.portfolio.entity.SkillCategory;
import com.portfolio.entity.SoftSkill;
import com.portfolio.repository.SkillCategoryRepository;
import com.portfolio.repository.SoftSkillRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("SkillCategoryService")
class SkillCategoryServiceTest {

    @Mock
    private SkillCategoryRepository skillCategoryRepository;

    @Mock
    private SoftSkillRepository softSkillRepository;

    @InjectMocks
    private SkillCategoryService skillCategoryService;

    @Captor
    private ArgumentCaptor<SkillCategory> categoryCaptor;

    private SkillCategory sampleCategory;
    private UUID categoryId;

    @BeforeEach
    void setUp() {
        categoryId = UUID.randomUUID();
        sampleCategory = SkillCategory.builder()
                .id(categoryId)
                .nameEn("Content Creation")
                .namePl("Tworzenie Treści")
                .displayOrder(1)
                .createdAt(OffsetDateTime.now())
                .updatedAt(OffsetDateTime.now())
                .build();
    }

    @Nested
    @DisplayName("getAllCategories")
    class GetAllCategories {

        @Test
        @DisplayName("should return all categories ordered by displayOrder")
        void shouldReturnAllCategories() {
            SkillCategory category2 = SkillCategory.builder()
                    .id(UUID.randomUUID())
                    .nameEn("SEO")
                    .namePl("SEO")
                    .displayOrder(2)
                    .build();
            given(skillCategoryRepository.findAllByOrderByDisplayOrderAsc())
                    .willReturn(List.of(sampleCategory, category2));
            given(softSkillRepository.findAllByOrderByDisplayOrderAsc())
                    .willReturn(List.of());

            List<SkillCategoryDto> result = skillCategoryService.getAllCategories("en");

            assertThat(result).hasSize(2);
            assertThat(result.get(0).getName()).isEqualTo("Content Creation");
            assertThat(result.get(1).getName()).isEqualTo("SEO");
        }

        @Test
        @DisplayName("should use Polish name when locale is pl")
        void shouldUsePolishName() {
            given(skillCategoryRepository.findAllByOrderByDisplayOrderAsc())
                    .willReturn(List.of(sampleCategory));
            given(softSkillRepository.findAllByOrderByDisplayOrderAsc())
                    .willReturn(List.of());

            List<SkillCategoryDto> result = skillCategoryService.getAllCategories("pl");

            assertThat(result.get(0).getName()).isEqualTo("Tworzenie Treści");
        }

        @Test
        @DisplayName("should include skill count")
        void shouldIncludeSkillCount() {
            SoftSkill skill1 = SoftSkill.builder()
                    .id(UUID.randomUUID())
                    .category(sampleCategory)
                    .build();
            SoftSkill skill2 = SoftSkill.builder()
                    .id(UUID.randomUUID())
                    .category(sampleCategory)
                    .build();

            given(skillCategoryRepository.findAllByOrderByDisplayOrderAsc())
                    .willReturn(List.of(sampleCategory));
            given(softSkillRepository.findAllByOrderByDisplayOrderAsc())
                    .willReturn(List.of(skill1, skill2));

            List<SkillCategoryDto> result = skillCategoryService.getAllCategories("en");

            assertThat(result.get(0).getSkillCount()).isEqualTo(2);
        }
    }

    @Nested
    @DisplayName("getAllCategoriesWithSkills")
    class GetAllCategoriesWithSkills {

        @Test
        @DisplayName("should return categories with nested skills")
        void shouldReturnCategoriesWithSkills() {
            SoftSkill skill = SoftSkill.builder()
                    .id(UUID.randomUUID())
                    .nameEn("Writing")
                    .namePl("Pisanie")
                    .category(sampleCategory)
                    .displayOrder(1)
                    .build();

            given(skillCategoryRepository.findAllByOrderByDisplayOrderAsc())
                    .willReturn(List.of(sampleCategory));
            given(softSkillRepository.findAllByOrderByDisplayOrderAsc())
                    .willReturn(List.of(skill));

            List<SkillCategoryWithSkillsDto> result = skillCategoryService.getAllCategoriesWithSkills("en");

            assertThat(result).hasSize(1);
            assertThat(result.get(0).getName()).isEqualTo("Content Creation");
            assertThat(result.get(0).getSkills()).hasSize(1);
            assertThat(result.get(0).getSkills().get(0).getName()).isEqualTo("Writing");
        }

        @Test
        @DisplayName("should return empty skills list when category has no skills")
        void shouldReturnEmptySkillsList() {
            given(skillCategoryRepository.findAllByOrderByDisplayOrderAsc())
                    .willReturn(List.of(sampleCategory));
            given(softSkillRepository.findAllByOrderByDisplayOrderAsc())
                    .willReturn(List.of());

            List<SkillCategoryWithSkillsDto> result = skillCategoryService.getAllCategoriesWithSkills("en");

            assertThat(result.get(0).getSkills()).isEmpty();
        }
    }

    @Nested
    @DisplayName("getCategoryById")
    class GetCategoryById {

        @Test
        @DisplayName("should return category when found")
        void shouldReturnCategoryWhenFound() {
            given(skillCategoryRepository.findById(categoryId))
                    .willReturn(Optional.of(sampleCategory));
            given(softSkillRepository.findAllByOrderByDisplayOrderAsc())
                    .willReturn(List.of());

            SkillCategoryDto result = skillCategoryService.getCategoryById(categoryId, "en");

            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(categoryId);
            assertThat(result.getName()).isEqualTo("Content Creation");
        }

        @Test
        @DisplayName("should throw EntityNotFoundException when not found")
        void shouldThrowWhenNotFound() {
            UUID nonExistentId = UUID.randomUUID();
            given(skillCategoryRepository.findById(nonExistentId))
                    .willReturn(Optional.empty());

            assertThatThrownBy(() -> skillCategoryService.getCategoryById(nonExistentId, "en"))
                    .isInstanceOf(EntityNotFoundException.class)
                    .hasMessageContaining(nonExistentId.toString());
        }
    }

    @Nested
    @DisplayName("createCategory")
    class CreateCategory {

        @Test
        @DisplayName("should create category with provided data")
        void shouldCreateCategory() {
            SkillCategoryCreateRequest request = new SkillCategoryCreateRequest();
            request.setNameEn("AI Tools");
            request.setNamePl("Narzędzia AI");

            given(skillCategoryRepository.getMaxDisplayOrder()).willReturn(0);
            given(skillCategoryRepository.save(any(SkillCategory.class))).willAnswer(invocation -> {
                SkillCategory category = invocation.getArgument(0);
                category.setId(UUID.randomUUID());
                return category;
            });
            given(softSkillRepository.findAllByOrderByDisplayOrderAsc())
                    .willReturn(List.of());

            SkillCategoryDto result = skillCategoryService.createCategory(request, "en");

            verify(skillCategoryRepository).save(categoryCaptor.capture());
            SkillCategory saved = categoryCaptor.getValue();

            assertThat(saved.getNameEn()).isEqualTo("AI Tools");
            assertThat(saved.getNamePl()).isEqualTo("Narzędzia AI");
        }

        @Test
        @DisplayName("should auto-increment displayOrder when not provided")
        void shouldAutoIncrementDisplayOrder() {
            SkillCategoryCreateRequest request = new SkillCategoryCreateRequest();
            request.setNameEn("New Category");
            request.setNamePl("Nowa Kategoria");

            given(skillCategoryRepository.getMaxDisplayOrder()).willReturn(5);
            given(skillCategoryRepository.save(any(SkillCategory.class))).willAnswer(invocation -> {
                SkillCategory category = invocation.getArgument(0);
                category.setId(UUID.randomUUID());
                return category;
            });
            given(softSkillRepository.findAllByOrderByDisplayOrderAsc())
                    .willReturn(List.of());

            skillCategoryService.createCategory(request, "en");

            verify(skillCategoryRepository).save(categoryCaptor.capture());
            assertThat(categoryCaptor.getValue().getDisplayOrder()).isEqualTo(6);
        }
    }

    @Nested
    @DisplayName("updateCategory")
    class UpdateCategory {

        @Test
        @DisplayName("should update category fields when provided")
        void shouldUpdateCategoryFields() {
            given(skillCategoryRepository.findById(categoryId))
                    .willReturn(Optional.of(sampleCategory));
            given(skillCategoryRepository.save(any(SkillCategory.class)))
                    .willAnswer(invocation -> invocation.getArgument(0));
            given(softSkillRepository.findAllByOrderByDisplayOrderAsc())
                    .willReturn(List.of());

            SkillCategoryUpdateRequest request = new SkillCategoryUpdateRequest();
            request.setNameEn("Updated Name");

            SkillCategoryDto result = skillCategoryService.updateCategory(categoryId, request, "en");

            verify(skillCategoryRepository).save(categoryCaptor.capture());
            assertThat(categoryCaptor.getValue().getNameEn()).isEqualTo("Updated Name");
        }

        @Test
        @DisplayName("should not update fields that are null")
        void shouldNotUpdateNullFields() {
            String originalName = sampleCategory.getNameEn();
            given(skillCategoryRepository.findById(categoryId))
                    .willReturn(Optional.of(sampleCategory));
            given(skillCategoryRepository.save(any(SkillCategory.class)))
                    .willAnswer(invocation -> invocation.getArgument(0));
            given(softSkillRepository.findAllByOrderByDisplayOrderAsc())
                    .willReturn(List.of());

            SkillCategoryUpdateRequest request = new SkillCategoryUpdateRequest();
            request.setNamePl("Nowa Nazwa");
            // nameEn is null

            skillCategoryService.updateCategory(categoryId, request, "en");

            verify(skillCategoryRepository).save(categoryCaptor.capture());
            assertThat(categoryCaptor.getValue().getNameEn()).isEqualTo(originalName);
            assertThat(categoryCaptor.getValue().getNamePl()).isEqualTo("Nowa Nazwa");
        }

        @Test
        @DisplayName("should throw EntityNotFoundException when not found")
        void shouldThrowWhenNotFound() {
            UUID nonExistentId = UUID.randomUUID();
            given(skillCategoryRepository.findById(nonExistentId))
                    .willReturn(Optional.empty());

            assertThatThrownBy(() -> skillCategoryService.updateCategory(nonExistentId, new SkillCategoryUpdateRequest(), "en"))
                    .isInstanceOf(EntityNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("deleteCategory")
    class DeleteCategory {

        @Test
        @DisplayName("should delete category when exists")
        void shouldDeleteCategory() {
            given(skillCategoryRepository.existsById(categoryId)).willReturn(true);

            skillCategoryService.deleteCategory(categoryId);

            verify(skillCategoryRepository).deleteById(categoryId);
        }

        @Test
        @DisplayName("should throw EntityNotFoundException when not found")
        void shouldThrowWhenNotFound() {
            UUID nonExistentId = UUID.randomUUID();
            given(skillCategoryRepository.existsById(nonExistentId)).willReturn(false);

            assertThatThrownBy(() -> skillCategoryService.deleteCategory(nonExistentId))
                    .isInstanceOf(EntityNotFoundException.class);
        }
    }
}
