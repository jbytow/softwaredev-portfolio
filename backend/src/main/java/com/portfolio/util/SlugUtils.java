package com.portfolio.util;

import com.github.slugify.Slugify;

import java.util.Locale;
import java.util.UUID;

/**
 * Single slug generator for every URL-addressable entity, so posts and newsletters get the same URL shape.
 */
public final class SlugUtils {

    // The "pl" locale loads Slugify's bundled ł -> l mapping; without it Unicode normalization drops ł entirely.
    private static final Slugify SLUGIFY = Slugify.builder().locale(Locale.of("pl")).build();

    private static final int FALLBACK_LENGTH = 8;

    private SlugUtils() {
        // Utility class, prevent instantiation
    }

    /**
     * Returns a lowercase ASCII slug for the given text. Text without any letters or digits (symbols, emoji)
     * gets a short random slug instead of an empty one, because an empty slug makes the record unreachable.
     */
    public static String toSlug(String text) {
        String slug = text == null ? "" : SLUGIFY.slugify(text);
        if (slug.isEmpty()) {
            return UUID.randomUUID().toString().replace("-", "").substring(0, FALLBACK_LENGTH);
        }
        return slug;
    }
}
