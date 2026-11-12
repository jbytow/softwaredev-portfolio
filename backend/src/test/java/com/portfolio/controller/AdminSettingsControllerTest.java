package com.portfolio.controller;

import com.portfolio.config.TestSecurityConfig;
import com.portfolio.dto.SiteSettingsDto;
import com.portfolio.dto.SiteSettingsUpdateRequest;
import com.portfolio.security.JwtService;
import com.portfolio.service.SiteSettingsService;
import com.portfolio.service.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
        controllers = AdminSettingsController.class,
        excludeAutoConfiguration = {
                org.springframework.boot.security.oauth2.client.autoconfigure.OAuth2ClientAutoConfiguration.class
        }
)
@Import(TestSecurityConfig.class)
@ActiveProfiles("test")
@DisplayName("AdminSettingsController")
class AdminSettingsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private SiteSettingsService siteSettingsService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private UserService userService;

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("should update settings for a valid body")
    void shouldUpdateSettings() throws Exception {
        given(siteSettingsService.updateSettings(any(SiteSettingsUpdateRequest.class), eq("en")))
                .willReturn(SiteSettingsDto.builder().phone("+48 123 456 789").build());

        mockMvc.perform(put("/api/admin/settings")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"phone\":\"+48 123 456 789\",\"email\":\"owner@example.com\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").doesNotExist())
                .andExpect(jsonPath("$.data.phone").value("+48 123 456 789"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("should return 400 with a field error for a 51-character phone")
    void shouldRejectOverLongPhone() throws Exception {
        mockMvc.perform(put("/api/admin/settings")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"phone\":\"" + "1".repeat(51) + "\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.phone").exists());

        verify(siteSettingsService, never()).updateSettings(any(), any());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("should return 400 with a field error for a malformed e-mail")
    void shouldRejectMalformedEmail() throws Exception {
        mockMvc.perform(put("/api/admin/settings")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"not-an-email\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.email").exists());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("should return 400 with a field error for an over-long RPG class title")
    void shouldRejectOverLongRpgClassTitle() throws Exception {
        mockMvc.perform(put("/api/admin/settings")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"rpgClassTitlePl\":\"" + "x".repeat(256) + "\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.rpgClassTitlePl").exists());
    }

    @Test
    @DisplayName("should return 403 for unauthenticated user")
    void shouldReturn403ForUnauthenticated() throws Exception {
        mockMvc.perform(put("/api/admin/settings")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isForbidden());
    }
}
