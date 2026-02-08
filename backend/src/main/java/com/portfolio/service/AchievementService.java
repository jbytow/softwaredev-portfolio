package com.portfolio.service;

import com.portfolio.dto.AchievementCreateRequest;
import com.portfolio.dto.AchievementDto;
import com.portfolio.dto.AchievementUpdateRequest;
import com.portfolio.dto.ReorderRequest;
import com.portfolio.entity.Achievement;
import com.portfolio.repository.AchievementRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AchievementService {

    private final AchievementRepository achievementRepository;

    public List<AchievementDto> getAll(String locale) {
        return achievementRepository.findAllByOrderByDisplayOrderAsc()
                .stream()
                .map(achievement -> mapToDto(achievement, locale))
                .toList();
    }

    public AchievementDto getById(UUID id, String locale) {
        Achievement achievement = achievementRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Achievement not found"));
        return mapToDto(achievement, locale);
    }

    @Transactional
    public AchievementDto create(AchievementCreateRequest request, String locale) {
        Achievement achievement = Achievement.builder()
                .titleEn(request.getTitleEn())
                .titlePl(request.getTitlePl())
                .descriptionEn(request.getDescriptionEn())
                .descriptionPl(request.getDescriptionPl())
                .icon(request.getIcon())
                .year(request.getYear())
                .displayOrder(request.getDisplayOrder() != null ?
                        request.getDisplayOrder() :
                        achievementRepository.getMaxDisplayOrder() + 1)
                .build();

        achievement = achievementRepository.save(achievement);
        return mapToDto(achievement, locale);
    }

    @Transactional
    public AchievementDto update(UUID id, AchievementUpdateRequest request, String locale) {
        Achievement achievement = achievementRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Achievement not found"));

        if (request.getTitleEn() != null) {
            achievement.setTitleEn(request.getTitleEn());
        }
        if (request.getTitlePl() != null) {
            achievement.setTitlePl(request.getTitlePl());
        }
        if (request.getDescriptionEn() != null) {
            achievement.setDescriptionEn(request.getDescriptionEn());
        }
        if (request.getDescriptionPl() != null) {
            achievement.setDescriptionPl(request.getDescriptionPl());
        }
        if (request.getIcon() != null) {
            achievement.setIcon(request.getIcon());
        }
        if (request.getYear() != null) {
            achievement.setYear(request.getYear());
        }
        if (request.getDisplayOrder() != null) {
            achievement.setDisplayOrder(request.getDisplayOrder());
        }

        achievement = achievementRepository.save(achievement);
        return mapToDto(achievement, locale);
    }

    @Transactional
    public void delete(UUID id) {
        if (!achievementRepository.existsById(id)) {
            throw new EntityNotFoundException("Achievement not found");
        }
        achievementRepository.deleteById(id);
    }

    @Transactional
    public void reorder(ReorderRequest request) {
        request.getItems().forEach(item -> {
            Achievement achievement = achievementRepository.findById(item.getId())
                    .orElseThrow(() -> new EntityNotFoundException("Achievement not found"));
            achievement.setDisplayOrder(item.getDisplayOrder());
            achievementRepository.save(achievement);
        });
    }

    private AchievementDto mapToDto(Achievement achievement, String locale) {
        return AchievementDto.builder()
                .id(achievement.getId())
                .title(achievement.getTitle(locale))
                .titleEn(achievement.getTitleEn())
                .titlePl(achievement.getTitlePl())
                .description(achievement.getDescription(locale))
                .descriptionEn(achievement.getDescriptionEn())
                .descriptionPl(achievement.getDescriptionPl())
                .icon(achievement.getIcon())
                .year(achievement.getYear())
                .displayOrder(achievement.getDisplayOrder())
                .createdAt(achievement.getCreatedAt())
                .updatedAt(achievement.getUpdatedAt())
                .build();
    }
}
