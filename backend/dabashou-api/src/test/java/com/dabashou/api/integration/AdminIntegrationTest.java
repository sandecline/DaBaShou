package com.dabashou.api.integration;

import com.dabashou.api.DabashouApplication;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(classes = DabashouApplication.class)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class AdminIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private static String adminToken;
    private static String userToken;
    private static String zhangsanToken;
    private static String resetPassword;

    @Test
    @Order(1)
    @DisplayName("未登录访问后台接口返回401")
    void adminEndpointShouldRejectAnonymous() throws Exception {
        mockMvc.perform(get("/api/admin/v1/users"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(401));
    }

    @Test
    @Order(2)
    @DisplayName("管理员和普通用户登录")
    void loginUsers() throws Exception {
        adminToken = login("admin", "admin123");
        userToken = login("lisi", "123456");
        zhangsanToken = login("zhangsan", "123456");
        assertNotNull(adminToken);
        assertNotNull(userToken);
        assertNotNull(zhangsanToken);
    }

    @Test
    @Order(3)
    @DisplayName("普通用户访问后台接口返回403")
    void adminEndpointShouldRejectNormalUser() throws Exception {
        mockMvc.perform(get("/api/admin/v1/users").header("Authorization", "Bearer " + userToken))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value(403));
    }

    @Test
    @Order(4)
    @DisplayName("管理员访问用户列表成功")
    void adminCanListUsers() throws Exception {
        mockMvc.perform(get("/api/admin/v1/users")
                        .header("Authorization", "Bearer " + adminToken)
                        .param("pageNum", "1")
                        .param("pageSize", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.list").isArray());
    }

    @Test
    @Order(5)
    @DisplayName("管理员禁用用户后登录失败")
    void disabledUserCannotLogin() throws Exception {
        mockMvc.perform(put("/api/admin/v1/users/2/status")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":0}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"zhangsan\",\"password\":\"123456\"}"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value(403));

        mockMvc.perform(get("/api/v1/user/profile")
                        .header("Authorization", "Bearer " + zhangsanToken))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(401));

        mockMvc.perform(put("/api/admin/v1/users/2/status")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":1}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @Order(6)
    @DisplayName("管理员重置密码后新密码可登录")
    void resetPasswordCanLogin() throws Exception {
        MvcResult result = mockMvc.perform(post("/api/admin/v1/users/4/reset-password")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.newPassword").isNotEmpty())
                .andReturn();
        Map<String, Object> response = objectMapper.readValue(result.getResponse().getContentAsString(), Map.class);
        resetPassword = (String) ((Map<String, Object>) response.get("data")).get("newPassword");

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("username", "wangwu", "password", resetPassword))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @Order(7)
    @DisplayName("系统配置非法key更新失败")
    void rejectIllegalConfigKey() throws Exception {
        mockMvc.perform(put("/api/admin/v1/config")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"jwt.secret\":\"leak\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400));
    }

    private String login(String username, String password) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("username", username, "password", password))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andReturn();
        Map<String, Object> response = objectMapper.readValue(result.getResponse().getContentAsString(), Map.class);
        return (String) ((Map<String, Object>) response.get("data")).get("accessToken");
    }
}
