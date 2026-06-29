package com.dabashou.message.config;

import com.dabashou.message.websocket.ChatWebSocketHandler;
import com.dabashou.common.utils.JwtUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.Map;

/**
 * WebSocket配置 - 注册 /ws/chat 路径，JWT 鉴权
 */
@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    private static final Logger log = LoggerFactory.getLogger(WebSocketConfig.class);

    private final ChatWebSocketHandler chatWebSocketHandler;

    @Value("${dabashou.jwt.secret}")
    private String jwtSecret;

    public WebSocketConfig(ChatWebSocketHandler chatWebSocketHandler) {
        this.chatWebSocketHandler = chatWebSocketHandler;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(chatWebSocketHandler, "/ws/chat")
                .addInterceptors(new JwtAuthHandshakeInterceptor(jwtSecret))
                .setAllowedOrigins("*");
    }

    /**
     * JWT 鉴权拦截器 - 从 query 参数 token 中解析 JWT 获取 userId
     */
    static class JwtAuthHandshakeInterceptor implements HandshakeInterceptor {

        private static final Logger log = LoggerFactory.getLogger(JwtAuthHandshakeInterceptor.class);
        private final String jwtSecret;

        JwtAuthHandshakeInterceptor(String jwtSecret) {
            this.jwtSecret = jwtSecret;
        }

        @Override
        public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response,
                                       WebSocketHandler wsHandler, Map<String, Object> attributes) {
            String query = request.getURI().getQuery();
            if (query == null) {
                log.warn("WebSocket 握手失败: 缺少 token 参数");
                return false;
            }
            String token = null;
            for (String param : query.split("&")) {
                if (param.startsWith("token=")) {
                    token = param.substring(6);
                    break;
                }
            }
            if (token == null || token.isBlank()) {
                log.warn("WebSocket 握手失败: token 为空");
                return false;
            }
            try {
                Long userId = JwtUtil.parseUserId(token, jwtSecret);
                if (userId == null) {
                    log.warn("WebSocket 握手失败: token 无效或已过期");
                    return false;
                }
                attributes.put("userId", userId);
                log.debug("WebSocket 鉴权成功: userId={}", userId);
                return true;
            } catch (Exception e) {
                log.warn("WebSocket 握手失败: token 解析异常: {}", e.getMessage());
                return false;
            }
        }

        @Override
        public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response,
                                   WebSocketHandler wsHandler, Exception exception) {
            // no-op
        }
    }
}
