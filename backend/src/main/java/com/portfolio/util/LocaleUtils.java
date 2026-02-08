package com.portfolio.util;

/**
 * Utility class for locale/language handling.
 */
public final class LocaleUtils {

    private LocaleUtils() {
        // Utility class, prevent instantiation
    }

    /**
     * Extracts the locale from Accept-Language header.
     * Returns "pl" for Polish, "en" for everything else.
     *
     * @param acceptLanguage the Accept-Language header value
     * @return "pl" or "en"
     */
    public static String extractLocale(String acceptLanguage) {
        if (acceptLanguage == null || acceptLanguage.isBlank()) {
            return "en";
        }
        String primary = acceptLanguage.split(",")[0].split(";")[0].trim();
        return primary.startsWith("pl") ? "pl" : "en";
    }
}
