package com.portfolio.controller;

import com.portfolio.config.TestSecurityConfig;
import com.portfolio.dto.SkillCategoryCreateRequest;
import com.portfolio.dto.SkillCategoryDto;
import com.portfolio.dto.SkillCategoryUpdateRequest;
import com.portfolio.security.JwtService;
import com.portfolio.service.SkillCategoryService;
import com.portfolio.service.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
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
        controllers = AdminSkillCategoryController.class,
        excludeAutoConfiguration = {
                org.springframework.boot.autoconfigure.security.oauth2.client.servlet.OAuth2ClientAutoConfiguration.class
        }
)
@Import(TestSecurityConfig.class)
@ActiveProfiles("test")
@DisplayName("AdminSkillCategoryController")
class AdminSkillCategoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private SkillCategoryService service;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private UserService userService;

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("should return 201 without a success message when created")
    void shouldReturn201OnCreate() throws Exception {
        given(service.createCategory(any(SkillCategoryCreateRequest.class), eq("en")))
                .willReturn(SkillCategoryDto.builder().id(UUID.randomUUID()).build());

        mockMvc.perform(post("/api/admin/skill-categories")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nameEn\":\"A\",\"namePl\":\"B\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").doesNotExist());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("should return 400 for a blank required field on update")
    void shouldRejectBlankOnUpdate() throws Exception {
        mockMvc.perform(put("/api/admin/skill-categories/" + UUID.randomUUID())
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nameEn\":\"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.nameEn").exists());

        verify(service, never()).updateCategory(any(), any(SkillCategoryUpdateRequest.class), any());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("should return 400 for an over-long field on update")
    void shouldRejectOverLongOnUpdate() throws Exception {
        mockMvc.perform(put("/api/admin/skill-categories/" + UUID.randomUUID())
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"namePl\":\"" + "x".repeat(256) + "\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.namePl").exists());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("should return 400 when a reorder item has no displayOrder")
    void shouldRejectInvalidReorder() throws Exception {
        mockMvc.perform(patch("/api/admin/skill-categories/reorder")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"items\":[{\"id\":\"" + UUID.randomUUID() + "\"}]}"))
                .andExpect(status().isBadRequest());

        verify(service, never()).reorderCategories(any());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("should return 400 for an empty reorder list")
    void shouldRejectEmptyReorder() throws Exception {
        mockMvc.perform(patch("/api/admin/skill-categories/reorder")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"items\":[]}"))
                .andExpect(status().isBadRequest());

        verify(service, never()).reorderCategories(any());
    }
}
