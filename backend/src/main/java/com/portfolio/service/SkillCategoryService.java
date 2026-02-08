package com.portfolio.service;

import com.portfolio.dto.*;
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
public class SkillCategoryService {

    private final SkillCategoryRepository skillCategoryRepository;
    private final SoftSkillRepository softSkillRepository;

    public List<SkillCategoryDto> getAllCategories(String locale) {
        return skillCategoryRepository.findAllByOrderByDisplayOrderAsc().stream()
                .map(category -> mapToDto(category, locale))
                .toList();
    }

    public List<SkillCategoryWithSkillsDto> getAllCategoriesWithSkills(String locale) {
        List<SkillCategory> categories = skillCategoryRepository.findAllByOrderByDisplayOrderAsc();
        List<SoftSkill> allSkills = softSkillRepository.findAllByOrderByDisplayOrderAsc();

        return categories.stream()
                .map(category -> {
                    List<SoftSkillDto> categorySkills = allSkills.stream()
                            .filter(skill -> skill.getCategory() != null &&
                                    skill.getCategory().getId().equals(category.getId()))
                            .map(skill -> mapSkillToDto(skill, locale))
                            .toList();

                    return SkillCategoryWithSkillsDto.builder()
                            .id(category.getId())
                            .name(category.getName(locale))
                            .nameEn(category.getNameEn())
                            .namePl(category.getNamePl())
                            .displayOrder(category.getDisplayOrder())
                            .skills(categorySkills)
                            .createdAt(category.getCreatedAt())
                            .updatedAt(category.getUpdatedAt())
                            .build();
                })
                .toList();
    }

    public SkillCategoryDto getCategoryById(UUID id, String locale) {
        SkillCategory category = skillCategoryRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Skill category not found: " + id));
        return mapToDto(category, locale);
    }

    @Transactional
    public SkillCategoryDto createCategory(SkillCategoryCreateRequest request, String locale) {
        SkillCategory category = SkillCategory.builder()
                .nameEn(request.getNameEn())
                .namePl(request.getNamePl())
                .displayOrder(request.getDisplayOrder() != null ? request.getDisplayOrder() :
                        skillCategoryRepository.getMaxDisplayOrder() + 1)
                .build();

        category = skillCategoryRepository.save(category);
        return mapToDto(category, locale);
    }

    @Transactional
    public SkillCategoryDto updateCategory(UUID id, SkillCategoryUpdateRequest request, String locale) {
        SkillCategory category = skillCategoryRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Skill category not found: " + id));

        if (request.getNameEn() != null) {
            category.setNameEn(request.getNameEn());
        }
        if (request.getNamePl() != null) {
            category.setNamePl(request.getNamePl());
        }
        if (request.getDisplayOrder() != null) {
            category.setDisplayOrder(request.getDisplayOrder());
        }

        category = skillCategoryRepository.save(category);
        return mapToDto(category, locale);
    }

    @Transactional
    public void deleteCategory(UUID id) {
        if (!skillCategoryRepository.existsById(id)) {
            throw new EntityNotFoundException("Skill category not found: " + id);
        }
        // Skills will be set to uncategorized (null) due to ON DELETE SET NULL
        skillCategoryRepository.deleteById(id);
    }

    @Transactional
    public void reorderCategories(ReorderRequest request) {
        for (ReorderRequest.OrderItem item : request.getItems()) {
            skillCategoryRepository.updateDisplayOrder(item.getId(), item.getDisplayOrder());
        }
    }

    private SkillCategoryDto mapToDto(SkillCategory category, String locale) {
        int skillCount = (int) softSkillRepository.findAllByOrderByDisplayOrderAsc().stream()
                .filter(skill -> skill.getCategory() != null &&
                        skill.getCategory().getId().equals(category.getId()))
                .count();

        return SkillCategoryDto.builder()
                .id(category.getId())
                .name(category.getName(locale))
                .nameEn(category.getNameEn())
                .namePl(category.getNamePl())
                .displayOrder(category.getDisplayOrder())
                .skillCount(skillCount)
                .createdAt(category.getCreatedAt())
                .updatedAt(category.getUpdatedAt())
                .build();
    }

    private SoftSkillDto mapSkillToDto(SoftSkill skill, String locale) {
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
