package com.portfolio.service;

import com.portfolio.dto.SoftSkillDto;
import com.portfolio.dto.SoftSkillUpdateRequest;
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
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("SoftSkillService")
class SoftSkillServiceTest {

    @Mock
    private SoftSkillRepository softSkillRepository;

    @Mock
    private SkillCategoryRepository skillCategoryRepository;

    @InjectMocks
    private SoftSkillService softSkillService;

    @Captor
    private ArgumentCaptor<SoftSkill> skillCaptor;

    private SkillCategory sampleCategory;
    private SoftSkill sampleSkill;
    private UUID skillId;

    @BeforeEach
    void setUp() {
        sampleCategory = SkillCategory.builder()
                .id(UUID.randomUUID())
                .nameEn("SEO, Email")
                .namePl("SEO, E-mail")
                .displayOrder(1)
                .build();
        skillId = UUID.randomUUID();
        sampleSkill = SoftSkill.builder()
                .id(skillId)
                .nameEn("Copywriting")
                .namePl("Copywriting PL")
                .descriptionEn("Writing copy")
                .descriptionPl("Pisanie tekstów")
                .professionalUsageEn("Landing pages")
                .professionalUsagePl("Strony docelowe")
                .icon("pen")
                .displayOrder(1)
                .category(sampleCategory)
                .createdAt(OffsetDateTime.now())
                .updatedAt(OffsetDateTime.now())
                .build();
    }

    @Nested
    @DisplayName("updateSoftSkill")
    class UpdateSoftSkill {

        @Test
        @DisplayName("should clear the category when categoryId is null")
        void shouldClearCategoryWhenCategoryIdIsNull() {
            given(softSkillRepository.findById(skillId)).willReturn(Optional.of(sampleSkill));
            given(softSkillRepository.save(any(SoftSkill.class))).willAnswer(invocation -> invocation.getArgument(0));

            SoftSkillUpdateRequest request = new SoftSkillUpdateRequest();
            request.setNameEn("Copywriting");

            SoftSkillDto result = softSkillService.updateSoftSkill(skillId, request, "en");

            verify(softSkillRepository).save(skillCaptor.capture());
            assertThat(skillCaptor.getValue().getCategory()).isNull();
            assertThat(result.getCategoryId()).isNull();
            assertThat(result.getCategoryName()).isNull();
            verify(skillCategoryRepository, never()).findById(any());
        }

        @Test
        @DisplayName("should set the category when categoryId is known")
        void shouldSetCategoryWhenKnown() {
            SkillCategory other = SkillCategory.builder()
                    .id(UUID.randomUUID())
                    .nameEn("Social Media")
                    .namePl("Media społecznościowe")
                    .build();
            given(softSkillRepository.findById(skillId)).willReturn(Optional.of(sampleSkill));
            given(skillCategoryRepository.findById(other.getId())).willReturn(Optional.of(other));
            given(softSkillRepository.save(any(SoftSkill.class))).willAnswer(invocation -> invocation.getArgument(0));

            SoftSkillUpdateRequest request = new SoftSkillUpdateRequest();
            request.setCategoryId(other.getId());

            SoftSkillDto result = softSkillService.updateSoftSkill(skillId, request, "pl");

            verify(softSkillRepository).save(skillCaptor.capture());
            assertThat(skillCaptor.getValue().getCategory()).isSameAs(other);
            assertThat(result.getCategoryId()).isEqualTo(other.getId());
            assertThat(result.getCategoryName()).isEqualTo("Media społecznościowe");
        }

        @Test
        @DisplayName("should throw EntityNotFoundException when categoryId is unknown")
        void shouldThrowWhenCategoryUnknown() {
            UUID unknownCategoryId = UUID.randomUUID();
            given(softSkillRepository.findById(skillId)).willReturn(Optional.of(sampleSkill));
            given(skillCategoryRepository.findById(unknownCategoryId)).willReturn(Optional.empty());

            SoftSkillUpdateRequest request = new SoftSkillUpdateRequest();
            request.setCategoryId(unknownCategoryId);

            assertThatThrownBy(() -> softSkillService.updateSoftSkill(skillId, request, "en"))
                    .isInstanceOf(EntityNotFoundException.class)
                    .hasMessageContaining(unknownCategoryId.toString());
            verify(softSkillRepository, never()).save(any());
        }
    }
}
