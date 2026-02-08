package com.portfolio.service;

import com.portfolio.dto.ExperienceCreateRequest;
import com.portfolio.dto.ExperienceDto;
import com.portfolio.dto.ExperienceUpdateRequest;
import com.portfolio.dto.ReorderRequest;
import com.portfolio.entity.Experience;
import com.portfolio.repository.ExperienceRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ExperienceService {

    private final ExperienceRepository experienceRepository;

    public List<ExperienceDto> getAllExperiences(String locale) {
        return experienceRepository.findAllByOrderByDisplayOrderAsc().stream()
                .map(exp -> mapToDto(exp, locale))
                .toList();
    }

    public ExperienceDto getExperienceById(UUID id, String locale) {
        Experience exp = experienceRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Experience not found: " + id));
        return mapToDto(exp, locale);
    }

    @Transactional
    public ExperienceDto createExperience(ExperienceCreateRequest request, String locale) {
        Experience exp = Experience.builder()
                .titleEn(request.getTitleEn())
                .titlePl(request.getTitlePl())
                .company(request.getCompany())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .descriptionEn(request.getDescriptionEn())
                .descriptionPl(request.getDescriptionPl())
                .achievementsEn(request.getAchievementsEn())
                .achievementsPl(request.getAchievementsPl())
                .displayOrder(request.getDisplayOrder() != null ? request.getDisplayOrder() :
                        experienceRepository.getMaxDisplayOrder() + 1)
                .build();

        exp = experienceRepository.save(exp);
        return mapToDto(exp, locale);
    }

    @Transactional
    public ExperienceDto updateExperience(UUID id, ExperienceUpdateRequest request, String locale) {
        Experience exp = experienceRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Experience not found: " + id));

        if (request.getTitleEn() != null) {
            exp.setTitleEn(request.getTitleEn());
        }
        if (request.getTitlePl() != null) {
            exp.setTitlePl(request.getTitlePl());
        }
        if (request.getCompany() != null) {
            exp.setCompany(request.getCompany());
        }
        if (request.getStartDate() != null) {
            exp.setStartDate(request.getStartDate());
        }
        // Allow setting endDate to null
        exp.setEndDate(request.getEndDate());
        if (request.getDescriptionEn() != null) {
            exp.setDescriptionEn(request.getDescriptionEn());
        }
        if (request.getDescriptionPl() != null) {
            exp.setDescriptionPl(request.getDescriptionPl());
        }
        if (request.getAchievementsEn() != null) {
            exp.setAchievementsEn(request.getAchievementsEn());
        }
        if (request.getAchievementsPl() != null) {
            exp.setAchievementsPl(request.getAchievementsPl());
        }
        if (request.getDisplayOrder() != null) {
            exp.setDisplayOrder(request.getDisplayOrder());
        }

        exp = experienceRepository.save(exp);
        return mapToDto(exp, locale);
    }

    @Transactional
    public void deleteExperience(UUID id) {
        if (!experienceRepository.existsById(id)) {
            throw new EntityNotFoundException("Experience not found: " + id);
        }
        experienceRepository.deleteById(id);
    }

    @Transactional
    public void reorderExperiences(ReorderRequest request) {
        for (ReorderRequest.OrderItem item : request.getItems()) {
            experienceRepository.updateDisplayOrder(item.getId(), item.getDisplayOrder());
        }
    }

    private ExperienceDto mapToDto(Experience exp, String locale) {
        return ExperienceDto.builder()
                .id(exp.getId())
                .title(exp.getTitle(locale))
                .titleEn(exp.getTitleEn())
                .titlePl(exp.getTitlePl())
                .company(exp.getCompany())
                .startDate(exp.getStartDate())
                .endDate(exp.getEndDate())
                .description(exp.getDescription(locale))
                .descriptionEn(exp.getDescriptionEn())
                .descriptionPl(exp.getDescriptionPl())
                .achievements(exp.getAchievements(locale))
                .achievementsEn(exp.getAchievementsEn())
                .achievementsPl(exp.getAchievementsPl())
                .displayOrder(exp.getDisplayOrder())
                .createdAt(exp.getCreatedAt())
                .updatedAt(exp.getUpdatedAt())
                .build();
    }
}
