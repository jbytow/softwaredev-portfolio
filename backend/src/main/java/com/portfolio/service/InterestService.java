package com.portfolio.service;

import com.portfolio.dto.InterestCreateRequest;
import com.portfolio.dto.InterestDto;
import com.portfolio.dto.InterestUpdateRequest;
import com.portfolio.dto.ReorderRequest;
import com.portfolio.entity.Interest;
import com.portfolio.repository.InterestRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class InterestService {

    private final InterestRepository interestRepository;

    public List<InterestDto> getAll(String locale) {
        return interestRepository.findAllByOrderByDisplayOrderAsc()
                .stream()
                .map(interest -> mapToDto(interest, locale))
                .toList();
    }

    public InterestDto getById(UUID id, String locale) {
        Interest interest = interestRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Interest not found"));
        return mapToDto(interest, locale);
    }

    @Transactional
    public InterestDto create(InterestCreateRequest request, String locale) {
        Interest interest = Interest.builder()
                .titleEn(request.getTitleEn())
                .titlePl(request.getTitlePl())
                .image1(request.getImage1())
                .image2(request.getImage2())
                .image3(request.getImage3())
                .displayOrder(request.getDisplayOrder() != null ?
                        request.getDisplayOrder() :
                        interestRepository.getMaxDisplayOrder() + 1)
                .build();

        interest = interestRepository.save(interest);
        return mapToDto(interest, locale);
    }

    @Transactional
    public InterestDto update(UUID id, InterestUpdateRequest request, String locale) {
        Interest interest = interestRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Interest not found"));

        if (request.getTitleEn() != null) {
            interest.setTitleEn(request.getTitleEn());
        }
        if (request.getTitlePl() != null) {
            interest.setTitlePl(request.getTitlePl());
        }
        if (request.getImage1() != null) {
            interest.setImage1(request.getImage1());
        }
        if (request.getImage2() != null) {
            interest.setImage2(request.getImage2());
        }
        if (request.getImage3() != null) {
            interest.setImage3(request.getImage3());
        }
        if (request.getDisplayOrder() != null) {
            interest.setDisplayOrder(request.getDisplayOrder());
        }

        interest = interestRepository.save(interest);
        return mapToDto(interest, locale);
    }

    @Transactional
    public void delete(UUID id) {
        if (!interestRepository.existsById(id)) {
            throw new EntityNotFoundException("Interest not found");
        }
        interestRepository.deleteById(id);
    }

    @Transactional
    public void reorder(ReorderRequest request) {
        request.getItems().forEach(item -> {
            Interest interest = interestRepository.findById(item.getId())
                    .orElseThrow(() -> new EntityNotFoundException("Interest not found"));
            interest.setDisplayOrder(item.getDisplayOrder());
            interestRepository.save(interest);
        });
    }

    private InterestDto mapToDto(Interest interest, String locale) {
        return InterestDto.builder()
                .id(interest.getId())
                .title(interest.getTitle(locale))
                .titleEn(interest.getTitleEn())
                .titlePl(interest.getTitlePl())
                .image1(interest.getImage1())
                .image2(interest.getImage2())
                .image3(interest.getImage3())
                .displayOrder(interest.getDisplayOrder())
                .createdAt(interest.getCreatedAt())
                .updatedAt(interest.getUpdatedAt())
                .build();
    }
}
