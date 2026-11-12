package com.portfolio.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("SlugUtils")
class SlugUtilsTest {

    @ParameterizedTest
    @CsvSource(delimiter = '|', value = {
            "Campaign for Łódź – żółć|campaign-for-lodz-zolc",
            "Głos marki|glos-marki",
            "ZŁOTE RADY|zlote-rady",
            "' My Slug '|my-slug",
            "A&B|a-b",
            "Kampania Łódź/2024|kampania-lodz-2024",
            "already-a-slug|already-a-slug"
    })
    @DisplayName("should produce lowercase ASCII slugs, mapping Polish letters including ł")
    void shouldSlugify(String input, String expected) {
        assertThat(SlugUtils.toSlug(input)).isEqualTo(expected);
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   ", "!!! ???", "\uD83D\uDE80\uD83D\uDD25", "–"})
    @DisplayName("should fall back to a non-blank URL-safe slug when nothing is left to slugify")
    void shouldFallBackWhenResultIsBlank(String input) {
        assertThat(SlugUtils.toSlug(input)).matches("[a-z0-9]+(-[a-z0-9]+)*");
    }

    @Test
    @DisplayName("should not reuse the same fallback for two blank inputs")
    void shouldGenerateDistinctFallbacks() {
        assertThat(SlugUtils.toSlug("!!!")).isNotEqualTo(SlugUtils.toSlug("!!!"));
    }
}
