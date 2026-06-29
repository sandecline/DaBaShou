package com.dabashou.api.integration;

import com.dabashou.api.DaBaShouApplication;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 安全与鉴权集成测试
 */
@SpringBootTest(classes = DaBaShouApplication.class)
@AutoConfigureMockMvc
@ActiveProfiles("dev")
class SecurityIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("未携带Token访问受保护接口 → 401")
    void shouldRejectWithoutToken() throws Exception {
        mockMvc.perform(get("/api/v1/user/profile"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(401));
    }

    @Test
    @DisplayName("携带无效Token → 401")
    void shouldRejectWithInvalidToken() throws Exception {
        mockMvc.perform(get("/api/v1/user/profile")
                        .header("Authorization", "Bearer invalid-token-xxx"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(401));
    }

    @Test
    @DisplayName("登录接口无需认证")
    void loginShouldBePublic() throws Exception {
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType("application/json")
                        .content("{\"username\":\"zhangsan\",\"password\":\"123456\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @DisplayName("注册接口无需认证")
    void registerShouldBePublic() throws Exception {
        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType("application/json")
                        .content("{\"username\":\"testuser\"}"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("公开查询接口无需认证")
    void publicEndpointsShouldWork() throws Exception {
        mockMvc.perform(get("/api/v1/skills/categories/tree"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @DisplayName("管理员接口需 ADMIN 角色")
    void adminEndpointsShouldRequireRole() throws Exception {
        // 无Token访问 admin 接口
        mockMvc.perform(get("/api/admin/v1/stats/overview"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(401));
    }
}
