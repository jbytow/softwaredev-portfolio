package com.portfolio.controller;

import com.portfolio.config.TestSecurityConfig;
import com.portfolio.dto.AchievementCreateRequest;
import com.portfolio.dto.AchievementDto;
import com.portfolio.dto.AchievementUpdateRequest;
import com.portfolio.security.JwtService;
import com.portfolio.service.AchievementService;
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

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
        controllers = AdminAchievementController.class,
        excludeAutoConfiguration = {
                org.springframework.boot.security.oauth2.client.autoconfigure.OAuth2ClientAutoConfiguration.class
        }
)
@Import(TestSecurityConfig.class)
@ActiveProfiles("test")
@DisplayName("AdminAchievementController")
class AdminAchievementControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AchievementService service;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private UserService userService;

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("should return 201 without a success message when created")
    void shouldReturn201OnCreate() throws Exception {
        given(service.create(any(AchievementCreateRequest.class), eq("en")))
                .willReturn(AchievementDto.builder().id(UUID.randomUUID()).build());

        mockMvc.perform(post("/api/admin/achievements")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"titleEn\":\"A\",\"titlePl\":\"B\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").doesNotExist());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("should return 400 for a blank required field on update")
    void shouldRejectBlankOnUpdate() throws Exception {
        mockMvc.perform(put("/api/admin/achievements/" + UUID.randomUUID())
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"titleEn\":\"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.titleEn").exists());

        verify(service, never()).update(any(), any(AchievementUpdateRequest.class), any());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("should return 400 for an over-long field on update")
    void shouldRejectOverLongOnUpdate() throws Exception {
        mockMvc.perform(put("/api/admin/achievements/" + UUID.randomUUID())
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"icon\":\"" + "x".repeat(51) + "\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.icon").exists());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("should return 400 when a reorder item has no displayOrder")
    void shouldRejectInvalidReorder() throws Exception {
        mockMvc.perform(patch("/api/admin/achievements/reorder")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"items\":[{\"id\":\"" + UUID.randomUUID() + "\"}]}"))
                .andExpect(status().isBadRequest());

        verify(service, never()).reorder(any());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("should return 400 for an empty reorder list")
    void shouldRejectEmptyReorder() throws Exception {
        mockMvc.perform(patch("/api/admin/achievements/reorder")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"items\":[]}"))
                .andExpect(status().isBadRequest());

        verify(service, never()).reorder(any());
    }
}
