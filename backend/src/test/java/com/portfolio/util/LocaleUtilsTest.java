package com.portfolio.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("LocaleUtils")
class LocaleUtilsTest {

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   ", "en", "en-US", "en-US,en;q=0.9", "de", "de-DE,pl;q=0.9", "*"})
    @DisplayName("falls back to en for blank, English and unsupported languages")
    void defaultsToEnglish(String header) {
        assertThat(LocaleUtils.extractLocale(header)).isEqualTo("en");
    }

    @ParameterizedTest
    @CsvSource(delimiter = '|', value = {"pl|pl", "pl-PL|pl", "pl-PL,pl;q=0.9,en;q=0.8|pl", "pl;q=0.9,en|pl", " pl-PL |pl"})
    @DisplayName("picks pl when Polish is the first listed language")
    void picksPolish(String header, String expected) {
        assertThat(LocaleUtils.extractLocale(header)).isEqualTo(expected);
    }
}
