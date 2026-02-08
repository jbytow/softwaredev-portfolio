package com.portfolio.service;

import com.portfolio.dto.ExperienceCreateRequest;
import com.portfolio.dto.ExperienceDto;
import com.portfolio.dto.ExperienceUpdateRequest;
import com.portfolio.entity.Experience;
import com.portfolio.repository.ExperienceRepository;
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

import java.time.LocalDate;
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
@DisplayName("ExperienceService")
class ExperienceServiceTest {

    @Mock
    private ExperienceRepository experienceRepository;

    @InjectMocks
    private ExperienceService experienceService;

    @Captor
    private ArgumentCaptor<Experience> experienceCaptor;

    private Experience sampleExperience;
    private UUID experienceId;

    @BeforeEach
    void setUp() {
        experienceId = UUID.randomUUID();
        sampleExperience = Experience.builder()
                .id(experienceId)
                .titleEn("Marketing Manager")
                .titlePl("Kierownik Marketingu")
                .company("Tech Corp")
                .startDate(LocalDate.of(2020, 1, 1))
                .endDate(LocalDate.of(2023, 6, 30))
                .descriptionEn("Led marketing team")
                .descriptionPl("Kierowanie zespołem marketingu")
                .achievementsEn(List.of("Increased sales by 50%", "Launched new campaigns"))
                .achievementsPl(List.of("Zwiększenie sprzedaży o 50%", "Uruchomienie nowych kampanii"))
                .displayOrder(1)
                .createdAt(OffsetDateTime.now())
                .updatedAt(OffsetDateTime.now())
                .build();
    }

    @Nested
    @DisplayName("getAllExperiences")
    class GetAllExperiences {

        @Test
        @DisplayName("should return all experiences ordered by displayOrder")
        void shouldReturnAllExperiences() {
            Experience exp2 = Experience.builder()
                    .id(UUID.randomUUID())
                    .titleEn("Senior Developer")
                    .titlePl("Starszy Programista")
                    .company("Another Corp")
                    .startDate(LocalDate.of(2018, 3, 1))
                    .displayOrder(2)
                    .build();
            given(experienceRepository.findAllByOrderByDisplayOrderAsc())
                    .willReturn(List.of(sampleExperience, exp2));

            List<ExperienceDto> result = experienceService.getAllExperiences("en");

            assertThat(result).hasSize(2);
            assertThat(result.get(0).getTitle()).isEqualTo("Marketing Manager");
            assertThat(result.get(1).getTitle()).isEqualTo("Senior Developer");
        }

        @Test
        @DisplayName("should use Polish title when locale is pl")
        void shouldUsePolishTitle() {
            given(experienceRepository.findAllByOrderByDisplayOrderAsc())
                    .willReturn(List.of(sampleExperience));

            List<ExperienceDto> result = experienceService.getAllExperiences("pl");

            assertThat(result.get(0).getTitle()).isEqualTo("Kierownik Marketingu");
        }

        @Test
        @DisplayName("should return Polish achievements when locale is pl")
        void shouldReturnPolishAchievements() {
            given(experienceRepository.findAllByOrderByDisplayOrderAsc())
                    .willReturn(List.of(sampleExperience));

            List<ExperienceDto> result = experienceService.getAllExperiences("pl");

            assertThat(result.get(0).getAchievements()).contains("Zwiększenie sprzedaży o 50%");
        }

        @Test
        @DisplayName("should return empty list when no experiences")
        void shouldReturnEmptyList() {
            given(experienceRepository.findAllByOrderByDisplayOrderAsc())
                    .willReturn(List.of());

            List<ExperienceDto> result = experienceService.getAllExperiences("en");

            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("getExperienceById")
    class GetExperienceById {

        @Test
        @DisplayName("should return experience when found")
        void shouldReturnExperienceWhenFound() {
            given(experienceRepository.findById(experienceId))
                    .willReturn(Optional.of(sampleExperience));

            ExperienceDto result = experienceService.getExperienceById(experienceId, "en");

            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(experienceId);
            assertThat(result.getTitle()).isEqualTo("Marketing Manager");
            assertThat(result.getCompany()).isEqualTo("Tech Corp");
        }

        @Test
        @DisplayName("should throw EntityNotFoundException when not found")
        void shouldThrowWhenNotFound() {
            UUID nonExistentId = UUID.randomUUID();
            given(experienceRepository.findById(nonExistentId))
                    .willReturn(Optional.empty());

            assertThatThrownBy(() -> experienceService.getExperienceById(nonExistentId, "en"))
                    .isInstanceOf(EntityNotFoundException.class)
                    .hasMessageContaining(nonExistentId.toString());
        }
    }

    @Nested
    @DisplayName("createExperience")
    class CreateExperience {

        @Test
        @DisplayName("should create experience with provided data")
        void shouldCreateExperience() {
            ExperienceCreateRequest request = new ExperienceCreateRequest();
            request.setTitleEn("Product Manager");
            request.setTitlePl("Menedżer Produktu");
            request.setCompany("New Corp");
            request.setStartDate(LocalDate.of(2024, 1, 1));
            request.setDescriptionEn("Leading product development");
            request.setAchievementsEn(List.of("Launched MVP"));

            given(experienceRepository.getMaxDisplayOrder()).willReturn(0);
            given(experienceRepository.save(any(Experience.class))).willAnswer(invocation -> {
                Experience exp = invocation.getArgument(0);
                exp.setId(UUID.randomUUID());
                return exp;
            });

            ExperienceDto result = experienceService.createExperience(request, "en");

            verify(experienceRepository).save(experienceCaptor.capture());
            Experience saved = experienceCaptor.getValue();

            assertThat(saved.getTitleEn()).isEqualTo("Product Manager");
            assertThat(saved.getCompany()).isEqualTo("New Corp");
            assertThat(saved.getStartDate()).isEqualTo(LocalDate.of(2024, 1, 1));
        }

        @Test
        @DisplayName("should allow null endDate for current positions")
        void shouldAllowNullEndDate() {
            ExperienceCreateRequest request = new ExperienceCreateRequest();
            request.setTitleEn("Current Role");
            request.setTitlePl("Obecna Rola");
            request.setCompany("Current Corp");
            request.setStartDate(LocalDate.of(2024, 1, 1));
            // endDate is null - current position

            given(experienceRepository.getMaxDisplayOrder()).willReturn(0);
            given(experienceRepository.save(any(Experience.class))).willAnswer(invocation -> {
                Experience exp = invocation.getArgument(0);
                exp.setId(UUID.randomUUID());
                return exp;
            });

            experienceService.createExperience(request, "en");

            verify(experienceRepository).save(experienceCaptor.capture());
            assertThat(experienceCaptor.getValue().getEndDate()).isNull();
        }

        @Test
        @DisplayName("should auto-increment displayOrder when not provided")
        void shouldAutoIncrementDisplayOrder() {
            ExperienceCreateRequest request = new ExperienceCreateRequest();
            request.setTitleEn("New Position");
            request.setTitlePl("Nowe Stanowisko");
            request.setCompany("Corp");
            request.setStartDate(LocalDate.now());

            given(experienceRepository.getMaxDisplayOrder()).willReturn(3);
            given(experienceRepository.save(any(Experience.class))).willAnswer(invocation -> {
                Experience exp = invocation.getArgument(0);
                exp.setId(UUID.randomUUID());
                return exp;
            });

            experienceService.createExperience(request, "en");

            verify(experienceRepository).save(experienceCaptor.capture());
            assertThat(experienceCaptor.getValue().getDisplayOrder()).isEqualTo(4);
        }
    }

    @Nested
    @DisplayName("updateExperience")
    class UpdateExperience {

        @Test
        @DisplayName("should update experience fields when provided")
        void shouldUpdateExperienceFields() {
            given(experienceRepository.findById(experienceId))
                    .willReturn(Optional.of(sampleExperience));
            given(experienceRepository.save(any(Experience.class)))
                    .willAnswer(invocation -> invocation.getArgument(0));

            ExperienceUpdateRequest request = new ExperienceUpdateRequest();
            request.setTitleEn("Updated Title");
            request.setCompany("New Company");

            ExperienceDto result = experienceService.updateExperience(experienceId, request, "en");

            verify(experienceRepository).save(experienceCaptor.capture());
            assertThat(experienceCaptor.getValue().getTitleEn()).isEqualTo("Updated Title");
            assertThat(experienceCaptor.getValue().getCompany()).isEqualTo("New Company");
        }

        @Test
        @DisplayName("should not update fields that are null except endDate")
        void shouldNotUpdateNullFieldsExceptEndDate() {
            String originalTitle = sampleExperience.getTitleEn();
            String originalCompany = sampleExperience.getCompany();
            given(experienceRepository.findById(experienceId))
                    .willReturn(Optional.of(sampleExperience));
            given(experienceRepository.save(any(Experience.class)))
                    .willAnswer(invocation -> invocation.getArgument(0));

            ExperienceUpdateRequest request = new ExperienceUpdateRequest();
            request.setDescriptionEn("New description");
            // other fields are null

            experienceService.updateExperience(experienceId, request, "en");

            verify(experienceRepository).save(experienceCaptor.capture());
            assertThat(experienceCaptor.getValue().getTitleEn()).isEqualTo(originalTitle);
            assertThat(experienceCaptor.getValue().getCompany()).isEqualTo(originalCompany);
            assertThat(experienceCaptor.getValue().getDescriptionEn()).isEqualTo("New description");
        }

        @Test
        @DisplayName("should throw EntityNotFoundException when not found")
        void shouldThrowWhenNotFound() {
            UUID nonExistentId = UUID.randomUUID();
            given(experienceRepository.findById(nonExistentId))
                    .willReturn(Optional.empty());

            assertThatThrownBy(() -> experienceService.updateExperience(nonExistentId, new ExperienceUpdateRequest(), "en"))
                    .isInstanceOf(EntityNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("deleteExperience")
    class DeleteExperience {

        @Test
        @DisplayName("should delete experience when exists")
        void shouldDeleteExperience() {
            given(experienceRepository.existsById(experienceId)).willReturn(true);

            experienceService.deleteExperience(experienceId);

            verify(experienceRepository).deleteById(experienceId);
        }

        @Test
        @DisplayName("should throw EntityNotFoundException when not found")
        void shouldThrowWhenNotFound() {
            UUID nonExistentId = UUID.randomUUID();
            given(experienceRepository.existsById(nonExistentId)).willReturn(false);

            assertThatThrownBy(() -> experienceService.deleteExperience(nonExistentId))
                    .isInstanceOf(EntityNotFoundException.class);
        }
    }
}
