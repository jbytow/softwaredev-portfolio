package com.portfolio.repository;

import com.portfolio.entity.SiteSettings;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SiteSettingsRepository extends JpaRepository<SiteSettings, Integer> {

    default SiteSettings getSettings() {
        return findById(1).orElseGet(() -> {
            SiteSettings settings = SiteSettings.builder()
                    .id(1)
                    .heroTitleEn("Marketing Professional")
                    .heroTitlePl("Specjalista ds. Marketingu")
                    .build();
            return save(settings);
        });
    }
}
