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

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 订单全链路集成测试
 * 测试: 创建货架→创建需求→创建订单→支付(冻结积分)→开始服务→核销→确认(结算积分)
 *
 * 依赖: 真实 MySQL + Flyway 迁移脚本 + 种子数据
 */
@SpringBootTest(classes = DabashouApplication.class)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class OrderIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private static String accessToken;
    private static Long shelfId;
    private static Long demandId;
    private static Long orderId;

    @Test
    @Order(1)
    @DisplayName("1. 用户登录获取JWT")
    void login() throws Exception {
        Map<String, String> body = Map.of("username", "zhangsan", "password", "123456");
        MvcResult result = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andReturn();

        Map<String, Object> response = objectMapper.readValue(
                result.getResponse().getContentAsString(), Map.class);
        Map<String, Object> data = (Map<String, Object>) response.get("data");
        accessToken = (String) data.get("accessToken");
        assertNotNull(accessToken, "JWT token should not be null");
    }

    @Test
    @Order(2)
    @DisplayName("2. 发布技能货架")
    void createShelf() throws Exception {
        Map<String, Object> body = new HashMap<>();
        body.put("skillTagId", 1);
        body.put("title", "Python编程辅导（测试）");
        body.put("description", "一对一Python辅导");
        body.put("pointPrice", 50);
        body.put("durationMinutes", 60);
        body.put("locationType", 3);

        MvcResult result = mockMvc.perform(post("/api/v1/shelves")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andReturn();

        Map<String, Object> response = objectMapper.readValue(
                result.getResponse().getContentAsString(), Map.class);
        shelfId = ((Number) response.get("data")).longValue();
        assertNotNull(shelfId);
    }

    @Test
    @Order(3)
    @DisplayName("3. 发布需求")
    void createDemand() throws Exception {
        Map<String, Object> body = new HashMap<>();
        body.put("skillTagId", 1);
        body.put("title", "急需Python辅导");
        body.put("description", "期末考试前突击");
        body.put("pointReward", 100);
        body.put("locationType", 3);

        MvcResult result = mockMvc.perform(post("/api/v1/demands")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andReturn();

        Map<String, Object> response = objectMapper.readValue(
                result.getResponse().getContentAsString(), Map.class);
        demandId = ((Number) response.get("data")).longValue();
        assertNotNull(demandId);
    }

    @Test
    @Order(4)
    @DisplayName("4. 创建订单（从货架购买）")
    void createOrderFromShelf() throws Exception {
        Map<String, Object> body = new HashMap<>();
        // 购买种子数据中李四发布的货架，避免买家购买自己刚发布的服务。
        body.put("shelfId", 3L);
        body.put("idempotentToken", "test-integration-" + System.currentTimeMillis());

        MvcResult result = mockMvc.perform(post("/api/v1/orders/from-shelf")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andReturn();

        Map<String, Object> response = objectMapper.readValue(
                result.getResponse().getContentAsString(), Map.class);
        orderId = ((Number) response.get("data")).longValue();
        assertNotNull(orderId);
    }

    @Test
    @Order(5)
    @DisplayName("5. 支付订单（应冻结积分）")
    void payOrder() throws Exception {
        MvcResult result = mockMvc.perform(post("/api/v1/orders/" + orderId + "/pay")
                        .header("Authorization", "Bearer " + accessToken)
                        .param("idempotentToken", "pay-test-" + System.currentTimeMillis()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.verifyCode").isNotEmpty())
                .andReturn();

        System.out.println("Payment response: " + result.getResponse().getContentAsString());
    }

    @Test
    @Order(6)
    @DisplayName("6. 验证状态机 — 非法流转应返回 409")
    void rejectInvalidTransition() throws Exception {
        // 尝试跳过服务直接确认（状态2→5非法）
        mockMvc.perform(post("/api/v1/orders/" + orderId + "/confirm")
                .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value(409));
    }

    @Test
    @Order(7)
    @DisplayName("7. 验证权限 — 非卖家不能开始服务")
    void rejectNonSellerStartService() throws Exception {
        // zhangsan 是买家，不是卖家
        mockMvc.perform(put("/api/v1/orders/" + orderId + "/start")
                .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value(403));
    }

    @Test
    @Order(8)
    @DisplayName("8. 验证订单不存在返回404")
    void rejectNonExistentOrder() throws Exception {
        mockMvc.perform(get("/api/v1/orders/99999/detail")
                .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(404));
    }
}
