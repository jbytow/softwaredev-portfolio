package com.portfolio.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.type.classreading.CachingMetadataReaderFactory;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.TreeSet;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Public API routes")
class PublicRoutesTest {

    /** Every route a controller declares, as "METHOD /path", collected by reflection. */
    private static TreeSet<String> allRoutes() throws Exception {
        var resolver = new PathMatchingResourcePatternResolver();
        var readers = new CachingMetadataReaderFactory(resolver);
        TreeSet<String> routes = new TreeSet<>();
        for (var resource : resolver.getResources("classpath*:com/portfolio/controller/*Controller.class")) {
            Class<?> type = Class.forName(readers.getMetadataReader(resource).getClassMetadata().getClassName());
            if (!type.isAnnotationPresent(RestController.class)) {
                continue;
            }
            RequestMapping base = AnnotatedElementUtils.findMergedAnnotation(type, RequestMapping.class);
            String prefix = base == null || base.path().length == 0 ? "" : base.path()[0];
            for (Method method : type.getDeclaredMethods()) {
                RequestMapping mapping = AnnotatedElementUtils.findMergedAnnotation(method, RequestMapping.class);
                if (mapping == null) {
                    continue;
                }
                String path = mapping.path().length == 0 ? "" : mapping.path()[0];
                routes.add(mapping.method()[0] + " " + prefix + path);
            }
        }
        return routes;
    }

    @Test
    @DisplayName("public routes are exactly those the frontend calls")
    void publicRoutesMatchTheFrontendInventory() throws Exception {
        List<String> publicRoutes = new ArrayList<>(allRoutes().stream()
                .filter(route -> !route.contains("/api/admin/"))
                .toList());

        assertThat(publicRoutes).containsExactlyInAnyOrderElementsOf(Arrays.asList(
                "GET /api/achievements",
                "GET /api/experiences",
                "GET /api/health",
                "GET /api/interests",
                "GET /api/media/{subDir}/{filename}",
                "GET /api/posts",
                "GET /api/posts/{slug}",
                "GET /api/rpg-stats",
                "GET /api/settings",
                "GET /api/skill-categories/with-skills",
                "POST /api/contact"));
    }
}
