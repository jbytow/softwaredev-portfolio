package com.portfolio.service;

import com.portfolio.dto.ReorderRequest;
import com.portfolio.dto.SoftSkillCreateRequest;
import com.portfolio.dto.SoftSkillDto;
import com.portfolio.dto.SoftSkillUpdateRequest;
import com.portfolio.entity.SkillCategory;
import com.portfolio.entity.SoftSkill;
import com.portfolio.repository.SkillCategoryRepository;
import com.portfolio.repository.SoftSkillRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SoftSkillService {

    private final SoftSkillRepository softSkillRepository;
    private final SkillCategoryRepository skillCategoryRepository;

    public List<SoftSkillDto> getAllSoftSkills(String locale) {
        return softSkillRepository.findAllByOrderByDisplayOrderAsc().stream()
                .map(skill -> mapToDto(skill, locale))
                .toList();
    }

    public SoftSkillDto getSoftSkillById(UUID id, String locale) {
        SoftSkill skill = softSkillRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Soft skill not found: " + id));
        return mapToDto(skill, locale);
    }

    @Transactional
    public SoftSkillDto createSoftSkill(SoftSkillCreateRequest request, String locale) {
        SkillCategory category = null;
        if (request.getCategoryId() != null) {
            category = skillCategoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new EntityNotFoundException("Skill category not found: " + request.getCategoryId()));
        }

        SoftSkill skill = SoftSkill.builder()
                .nameEn(request.getNameEn())
                .namePl(request.getNamePl())
                .descriptionEn(request.getDescriptionEn())
                .descriptionPl(request.getDescriptionPl())
                .professionalUsageEn(request.getProfessionalUsageEn())
                .professionalUsagePl(request.getProfessionalUsagePl())
                .icon(request.getIcon())
                .displayOrder(request.getDisplayOrder() != null ? request.getDisplayOrder() :
                        softSkillRepository.getMaxDisplayOrder() + 1)
                .category(category)
                .build();

        skill = softSkillRepository.save(skill);
        return mapToDto(skill, locale);
    }

    @Transactional
    public SoftSkillDto updateSoftSkill(UUID id, SoftSkillUpdateRequest request, String locale) {
        SoftSkill skill = softSkillRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Soft skill not found: " + id));

        if (request.getNameEn() != null) {
            skill.setNameEn(request.getNameEn());
        }
        if (request.getNamePl() != null) {
            skill.setNamePl(request.getNamePl());
        }
        if (request.getDescriptionEn() != null) {
            skill.setDescriptionEn(request.getDescriptionEn());
        }
        if (request.getDescriptionPl() != null) {
            skill.setDescriptionPl(request.getDescriptionPl());
        }
        if (request.getProfessionalUsageEn() != null) {
            skill.setProfessionalUsageEn(request.getProfessionalUsageEn());
        }
        if (request.getProfessionalUsagePl() != null) {
            skill.setProfessionalUsagePl(request.getProfessionalUsagePl());
        }
        if (request.getIcon() != null) {
            skill.setIcon(request.getIcon());
        }
        if (request.getDisplayOrder() != null) {
            skill.setDisplayOrder(request.getDisplayOrder());
        }
        if (request.getCategoryId() != null) {
            SkillCategory category = skillCategoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new EntityNotFoundException("Skill category not found: " + request.getCategoryId()));
            skill.setCategory(category);
        }

        skill = softSkillRepository.save(skill);
        return mapToDto(skill, locale);
    }

    @Transactional
    public void deleteSoftSkill(UUID id) {
        if (!softSkillRepository.existsById(id)) {
            throw new EntityNotFoundException("Soft skill not found: " + id);
        }
        softSkillRepository.deleteById(id);
    }

    @Transactional
    public void reorderSoftSkills(ReorderRequest request) {
        for (ReorderRequest.OrderItem item : request.getItems()) {
            softSkillRepository.updateDisplayOrder(item.getId(), item.getDisplayOrder());
        }
    }

    private SoftSkillDto mapToDto(SoftSkill skill, String locale) {
        return SoftSkillDto.builder()
                .id(skill.getId())
                .name(skill.getName(locale))
                .nameEn(skill.getNameEn())
                .namePl(skill.getNamePl())
                .description(skill.getDescription(locale))
                .descriptionEn(skill.getDescriptionEn())
                .descriptionPl(skill.getDescriptionPl())
                .professionalUsage(skill.getProfessionalUsage(locale))
                .professionalUsageEn(skill.getProfessionalUsageEn())
                .professionalUsagePl(skill.getProfessionalUsagePl())
                .icon(skill.getIcon())
                .displayOrder(skill.getDisplayOrder())
                .categoryId(skill.getCategory() != null ? skill.getCategory().getId() : null)
                .categoryName(skill.getCategory() != null ? skill.getCategory().getName(locale) : null)
                .createdAt(skill.getCreatedAt())
                .updatedAt(skill.getUpdatedAt())
                .build();
    }
}
