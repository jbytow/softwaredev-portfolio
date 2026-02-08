package com.portfolio.service;

import com.portfolio.dto.InterestCreateRequest;
import com.portfolio.dto.InterestDto;
import com.portfolio.dto.InterestUpdateRequest;
import com.portfolio.entity.Interest;
import com.portfolio.repository.InterestRepository;
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
@DisplayName("InterestService")
class InterestServiceTest {

    @Mock
    private InterestRepository interestRepository;

    @InjectMocks
    private InterestService interestService;

    @Captor
    private ArgumentCaptor<Interest> interestCaptor;

    private Interest sampleInterest;
    private UUID interestId;

    @BeforeEach
    void setUp() {
        interestId = UUID.randomUUID();
        sampleInterest = Interest.builder()
                .id(interestId)
                .titleEn("Photography")
                .titlePl("Fotografia")
                .image1("/uploads/photo1.jpg")
                .image2("/uploads/photo2.jpg")
                .image3("/uploads/photo3.jpg")
                .displayOrder(1)
                .createdAt(OffsetDateTime.now())
                .updatedAt(OffsetDateTime.now())
                .build();
    }

    @Nested
    @DisplayName("getAll")
    class GetAll {

        @Test
        @DisplayName("should return all interests ordered by displayOrder")
        void shouldReturnAllInterests() {
            Interest interest2 = Interest.builder()
                    .id(UUID.randomUUID())
                    .titleEn("Travel")
                    .titlePl("Podróże")
                    .displayOrder(2)
                    .build();
            given(interestRepository.findAllByOrderByDisplayOrderAsc())
                    .willReturn(List.of(sampleInterest, interest2));

            List<InterestDto> result = interestService.getAll("en");

            assertThat(result).hasSize(2);
            assertThat(result.get(0).getTitle()).isEqualTo("Photography");
            assertThat(result.get(1).getTitle()).isEqualTo("Travel");
        }

        @Test
        @DisplayName("should use Polish title when locale is pl")
        void shouldUsePolishTitle() {
            given(interestRepository.findAllByOrderByDisplayOrderAsc())
                    .willReturn(List.of(sampleInterest));

            List<InterestDto> result = interestService.getAll("pl");

            assertThat(result.get(0).getTitle()).isEqualTo("Fotografia");
        }

        @Test
        @DisplayName("should return empty list when no interests")
        void shouldReturnEmptyList() {
            given(interestRepository.findAllByOrderByDisplayOrderAsc())
                    .willReturn(List.of());

            List<InterestDto> result = interestService.getAll("en");

            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("getById")
    class GetById {

        @Test
        @DisplayName("should return interest when found")
        void shouldReturnInterestWhenFound() {
            given(interestRepository.findById(interestId))
                    .willReturn(Optional.of(sampleInterest));

            InterestDto result = interestService.getById(interestId, "en");

            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(interestId);
            assertThat(result.getTitle()).isEqualTo("Photography");
        }

        @Test
        @DisplayName("should throw EntityNotFoundException when not found")
        void shouldThrowWhenNotFound() {
            UUID nonExistentId = UUID.randomUUID();
            given(interestRepository.findById(nonExistentId))
                    .willReturn(Optional.empty());

            assertThatThrownBy(() -> interestService.getById(nonExistentId, "en"))
                    .isInstanceOf(EntityNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("create")
    class Create {

        @Test
        @DisplayName("should create interest with provided data")
        void shouldCreateInterest() {
            InterestCreateRequest request = new InterestCreateRequest();
            request.setTitleEn("Music");
            request.setTitlePl("Muzyka");
            request.setImage1("/uploads/music1.jpg");

            given(interestRepository.getMaxDisplayOrder()).willReturn(0);
            given(interestRepository.save(any(Interest.class))).willAnswer(invocation -> {
                Interest interest = invocation.getArgument(0);
                interest.setId(UUID.randomUUID());
                return interest;
            });

            InterestDto result = interestService.create(request, "en");

            verify(interestRepository).save(interestCaptor.capture());
            Interest saved = interestCaptor.getValue();

            assertThat(saved.getTitleEn()).isEqualTo("Music");
            assertThat(saved.getTitlePl()).isEqualTo("Muzyka");
            assertThat(saved.getImage1()).isEqualTo("/uploads/music1.jpg");
        }

        @Test
        @DisplayName("should auto-increment displayOrder when not provided")
        void shouldAutoIncrementDisplayOrder() {
            InterestCreateRequest request = new InterestCreateRequest();
            request.setTitleEn("Gaming");
            request.setTitlePl("Gry");

            given(interestRepository.getMaxDisplayOrder()).willReturn(5);
            given(interestRepository.save(any(Interest.class))).willAnswer(invocation -> {
                Interest interest = invocation.getArgument(0);
                interest.setId(UUID.randomUUID());
                return interest;
            });

            interestService.create(request, "en");

            verify(interestRepository).save(interestCaptor.capture());
            assertThat(interestCaptor.getValue().getDisplayOrder()).isEqualTo(6);
        }
    }

    @Nested
    @DisplayName("update")
    class Update {

        @Test
        @DisplayName("should update interest fields when provided")
        void shouldUpdateInterestFields() {
            given(interestRepository.findById(interestId))
                    .willReturn(Optional.of(sampleInterest));
            given(interestRepository.save(any(Interest.class)))
                    .willAnswer(invocation -> invocation.getArgument(0));

            InterestUpdateRequest request = new InterestUpdateRequest();
            request.setTitleEn("Updated Photography");

            InterestDto result = interestService.update(interestId, request, "en");

            verify(interestRepository).save(interestCaptor.capture());
            assertThat(interestCaptor.getValue().getTitleEn()).isEqualTo("Updated Photography");
        }

        @Test
        @DisplayName("should not update fields that are null")
        void shouldNotUpdateNullFields() {
            String originalTitle = sampleInterest.getTitleEn();
            given(interestRepository.findById(interestId))
                    .willReturn(Optional.of(sampleInterest));
            given(interestRepository.save(any(Interest.class)))
                    .willAnswer(invocation -> invocation.getArgument(0));

            InterestUpdateRequest request = new InterestUpdateRequest();
            request.setImage1("/uploads/new-image.jpg");
            // titleEn is null

            interestService.update(interestId, request, "en");

            verify(interestRepository).save(interestCaptor.capture());
            assertThat(interestCaptor.getValue().getTitleEn()).isEqualTo(originalTitle);
            assertThat(interestCaptor.getValue().getImage1()).isEqualTo("/uploads/new-image.jpg");
        }

        @Test
        @DisplayName("should throw EntityNotFoundException when not found")
        void shouldThrowWhenNotFound() {
            UUID nonExistentId = UUID.randomUUID();
            given(interestRepository.findById(nonExistentId))
                    .willReturn(Optional.empty());

            assertThatThrownBy(() -> interestService.update(nonExistentId, new InterestUpdateRequest(), "en"))
                    .isInstanceOf(EntityNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("delete")
    class Delete {

        @Test
        @DisplayName("should delete interest when exists")
        void shouldDeleteInterest() {
            given(interestRepository.existsById(interestId)).willReturn(true);

            interestService.delete(interestId);

            verify(interestRepository).deleteById(interestId);
        }

        @Test
        @DisplayName("should throw EntityNotFoundException when not found")
        void shouldThrowWhenNotFound() {
            UUID nonExistentId = UUID.randomUUID();
            given(interestRepository.existsById(nonExistentId)).willReturn(false);

            assertThatThrownBy(() -> interestService.delete(nonExistentId))
                    .isInstanceOf(EntityNotFoundException.class);
        }
    }
}
