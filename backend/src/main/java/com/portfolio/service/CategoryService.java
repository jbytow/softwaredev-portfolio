package com.portfolio.service;

import com.portfolio.dto.CategoryDto;
import com.portfolio.entity.Category;
import com.portfolio.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CategoryService {

    private final PostRepository postRepository;

    public List<CategoryDto> getAllCategories(String locale) {
        return Arrays.stream(Category.values())
                .map(category -> CategoryDto.builder()
                        .value(category)
                        .label(category.getLabel(locale))
                        .labelEn(category.getLabelEn())
                        .labelPl(category.getLabelPl())
                        .postCount(postRepository.countByCategoryAndPublished(category))
                        .build())
                .toList();
    }

    public CategoryDto getCategory(Category category, String locale) {
        return CategoryDto.builder()
                .value(category)
                .label(category.getLabel(locale))
                .labelEn(category.getLabelEn())
                .labelPl(category.getLabelPl())
                .postCount(postRepository.countByCategoryAndPublished(category))
                .build();
    }
}
