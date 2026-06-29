package com.dabashou.api.interceptor;

import com.dabashou.common.utils.JwtUtil;
import io.jsonwebtoken.Claims;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.List;
import java.util.Map;

/**
 * WebSocket握手鉴权拦截器
 * 从query参数token解析用户信息，放入WebSocket attributes
 */
@Component
public class WsAuthInterceptor implements HandshakeInterceptor {

    private static final Logger log = LoggerFactory.getLogger(WsAuthInterceptor.class);

    @Value("${dabashou.jwt.secret}")
    private String jwtSecret;

    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response,
                                   WebSocketHandler wsHandler, Map<String, Object> attributes) {
        String query = request.getURI().getQuery();
        if (query == null || !query.contains("token=")) {
            log.warn("WebSocket握手缺少token参数");
            return false;
        }
        String token = null;
        for (String param : query.split("&")) {
            if (param.startsWith("token=")) {
                token = param.substring(6);
                break;
            }
        }
        if (token == null || token.isEmpty()) {
            log.warn("WebSocket握手token为空");
            return false;
        }
        try {
            Claims claims = JwtUtil.parseToken(token, jwtSecret);
            Long userId = JwtUtil.getUserId(claims);
            List<String> roles = JwtUtil.getRoles(claims);
            attributes.put("userId", userId);
            attributes.put("roles", roles);
            log.debug("WebSocket握手成功: userId={}", userId);
            return true;
        } catch (Exception e) {
            log.warn("WebSocket握手鉴权失败: {}", e.getMessage());
            return false;
        }
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response,
                               WebSocketHandler wsHandler, Exception exception) {
        // no-op
    }
}
