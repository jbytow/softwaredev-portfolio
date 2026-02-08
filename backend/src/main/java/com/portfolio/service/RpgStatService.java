package com.portfolio.service;

import com.portfolio.dto.ReorderRequest;
import com.portfolio.dto.RpgStatCreateRequest;
import com.portfolio.dto.RpgStatDto;
import com.portfolio.dto.RpgStatUpdateRequest;
import com.portfolio.entity.RpgStat;
import com.portfolio.repository.RpgStatRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RpgStatService {

    private final RpgStatRepository rpgStatRepository;

    public List<RpgStatDto> getAll(String locale) {
        return rpgStatRepository.findAllByOrderByDisplayOrderAsc()
                .stream()
                .map(stat -> mapToDto(stat, locale))
                .toList();
    }

    public RpgStatDto getById(UUID id, String locale) {
        RpgStat stat = rpgStatRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("RPG stat not found"));
        return mapToDto(stat, locale);
    }

    @Transactional
    public RpgStatDto create(RpgStatCreateRequest request, String locale) {
        RpgStat stat = RpgStat.builder()
                .attr(request.getAttr())
                .labelEn(request.getLabelEn())
                .labelPl(request.getLabelPl())
                .level(request.getLevel() != null ? request.getLevel() : 5)
                .maxLevel(request.getMaxLevel() != null ? request.getMaxLevel() : 10)
                .skills(joinSkills(request.getSkills()))
                .displayOrder(request.getDisplayOrder() != null ?
                        request.getDisplayOrder() :
                        rpgStatRepository.getMaxDisplayOrder() + 1)
                .build();

        stat = rpgStatRepository.save(stat);
        return mapToDto(stat, locale);
    }

    @Transactional
    public RpgStatDto update(UUID id, RpgStatUpdateRequest request, String locale) {
        RpgStat stat = rpgStatRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("RPG stat not found"));

        if (request.getAttr() != null) {
            stat.setAttr(request.getAttr());
        }
        if (request.getLabelEn() != null) {
            stat.setLabelEn(request.getLabelEn());
        }
        if (request.getLabelPl() != null) {
            stat.setLabelPl(request.getLabelPl());
        }
        if (request.getLevel() != null) {
            stat.setLevel(request.getLevel());
        }
        if (request.getMaxLevel() != null) {
            stat.setMaxLevel(request.getMaxLevel());
        }
        if (request.getSkills() != null) {
            stat.setSkills(joinSkills(request.getSkills()));
        }
        if (request.getDisplayOrder() != null) {
            stat.setDisplayOrder(request.getDisplayOrder());
        }

        stat = rpgStatRepository.save(stat);
        return mapToDto(stat, locale);
    }

    @Transactional
    public void delete(UUID id) {
        if (!rpgStatRepository.existsById(id)) {
            throw new EntityNotFoundException("RPG stat not found");
        }
        rpgStatRepository.deleteById(id);
    }

    @Transactional
    public void reorder(ReorderRequest request) {
        request.getItems().forEach(item -> {
            RpgStat stat = rpgStatRepository.findById(item.getId())
                    .orElseThrow(() -> new EntityNotFoundException("RPG stat not found"));
            stat.setDisplayOrder(item.getDisplayOrder());
            rpgStatRepository.save(stat);
        });
    }

    private List<String> splitSkills(String skills) {
        if (skills == null || skills.isBlank()) {
            return Collections.emptyList();
        }
        return Arrays.stream(skills.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toList();
    }

    private String joinSkills(List<String> skills) {
        if (skills == null || skills.isEmpty()) {
            return "";
        }
        return String.join(", ", skills);
    }

    private RpgStatDto mapToDto(RpgStat stat, String locale) {
        return RpgStatDto.builder()
                .id(stat.getId())
                .attr(stat.getAttr())
                .label(stat.getLabel(locale))
                .labelEn(stat.getLabelEn())
                .labelPl(stat.getLabelPl())
                .level(stat.getLevel())
                .maxLevel(stat.getMaxLevel())
                .skills(splitSkills(stat.getSkills()))
                .displayOrder(stat.getDisplayOrder())
                .createdAt(stat.getCreatedAt())
                .updatedAt(stat.getUpdatedAt())
                .build();
    }
}
