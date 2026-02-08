package com.portfolio.config;

import com.portfolio.entity.Category;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("CategoryConverter")
class CategoryConverterTest {

    private CategoryConverter converter;

    @BeforeEach
    void setUp() {
        converter = new CategoryConverter();
    }

    @Nested
    @DisplayName("convertToDatabaseColumn")
    class ConvertToDatabaseColumn {

        @ParameterizedTest
        @EnumSource(Category.class)
        @DisplayName("should convert all enum values to their string names")
        void shouldConvertEnumToString(Category category) {
            String result = converter.convertToDatabaseColumn(category);

            assertThat(result).isEqualTo(category.name());
        }

        @Test
        @DisplayName("should return null when category is null")
        void shouldReturnNullForNullCategory() {
            String result = converter.convertToDatabaseColumn(null);

            assertThat(result).isNull();
        }
    }

    @Nested
    @DisplayName("convertToEntityAttribute")
    class ConvertToEntityAttribute {

        @ParameterizedTest
        @EnumSource(Category.class)
        @DisplayName("should convert all valid string values to enum")
        void shouldConvertStringToEnum(Category category) {
            Category result = converter.convertToEntityAttribute(category.name());

            assertThat(result).isEqualTo(category);
        }

        @Test
        @DisplayName("should return null when database value is null")
        void shouldReturnNullForNullValue() {
            Category result = converter.convertToEntityAttribute(null);

            assertThat(result).isNull();
        }

        @ParameterizedTest
        @ValueSource(strings = {"INVALID", "unknown", "ABOUT_ME", ""})
        @DisplayName("should throw exception for invalid category values")
        void shouldThrowExceptionForInvalidValue(String invalidValue) {
            assertThatThrownBy(() -> converter.convertToEntityAttribute(invalidValue))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("round-trip conversion")
    class RoundTripConversion {

        @ParameterizedTest
        @EnumSource(Category.class)
        @DisplayName("should maintain value integrity through round-trip conversion")
        void shouldMaintainValueThroughRoundTrip(Category original) {
            String dbValue = converter.convertToDatabaseColumn(original);
            Category restored = converter.convertToEntityAttribute(dbValue);

            assertThat(restored).isEqualTo(original);
        }
    }
}
