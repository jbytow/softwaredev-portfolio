package com.portfolio.config;

import com.portfolio.entity.MediaType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("MediaTypeConverter")
class MediaTypeConverterTest {

    private MediaTypeConverter converter;

    @BeforeEach
    void setUp() {
        converter = new MediaTypeConverter();
    }

    @Nested
    @DisplayName("convertToDatabaseColumn")
    class ConvertToDatabaseColumn {

        @ParameterizedTest
        @EnumSource(MediaType.class)
        @DisplayName("should convert all enum values to their string names")
        void shouldConvertEnumToString(MediaType mediaType) {
            String result = converter.convertToDatabaseColumn(mediaType);

            assertThat(result).isEqualTo(mediaType.name());
        }

        @Test
        @DisplayName("should return null when mediaType is null")
        void shouldReturnNullForNullMediaType() {
            String result = converter.convertToDatabaseColumn(null);

            assertThat(result).isNull();
        }
    }

    @Nested
    @DisplayName("convertToEntityAttribute")
    class ConvertToEntityAttribute {

        @ParameterizedTest
        @EnumSource(MediaType.class)
        @DisplayName("should convert all valid string values to enum")
        void shouldConvertStringToEnum(MediaType mediaType) {
            MediaType result = converter.convertToEntityAttribute(mediaType.name());

            assertThat(result).isEqualTo(mediaType);
        }

        @Test
        @DisplayName("should return null when database value is null")
        void shouldReturnNullForNullValue() {
            MediaType result = converter.convertToEntityAttribute(null);

            assertThat(result).isNull();
        }

        @ParameterizedTest
        @ValueSource(strings = {"INVALID", "unknown", "AUDIO", ""})
        @DisplayName("should throw exception for invalid media type values")
        void shouldThrowExceptionForInvalidValue(String invalidValue) {
            assertThatThrownBy(() -> converter.convertToEntityAttribute(invalidValue))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("round-trip conversion")
    class RoundTripConversion {

        @ParameterizedTest
        @EnumSource(MediaType.class)
        @DisplayName("should maintain value integrity through round-trip conversion")
        void shouldMaintainValueThroughRoundTrip(MediaType original) {
            String dbValue = converter.convertToDatabaseColumn(original);
            MediaType restored = converter.convertToEntityAttribute(dbValue);

            assertThat(restored).isEqualTo(original);
        }
    }
}
