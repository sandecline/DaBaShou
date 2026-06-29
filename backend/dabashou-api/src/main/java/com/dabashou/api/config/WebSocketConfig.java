package com.dabashou.api.config;

import com.dabashou.api.interceptor.WsAuthInterceptor;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

/**
 * WebSocket配置 — 注册/chat端点
 * ChatWebSocketHandler在Phase G(message模块)实现后自动注入
 */
@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    private final WsAuthInterceptor wsAuthInterceptor;
    private final ApplicationContext context;

    public WebSocketConfig(WsAuthInterceptor wsAuthInterceptor, ApplicationContext context) {
        this.wsAuthInterceptor = wsAuthInterceptor;
        this.context = context;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        try {
            WebSocketHandler handler = (WebSocketHandler) context.getBean("chatWebSocketHandler");
            registry.addHandler(handler, "/ws/chat")
                    .addInterceptors(wsAuthInterceptor)
                    .setAllowedOrigins("*");
        } catch (Exception e) {
            // ChatWebSocketHandler尚未注册，跳过(Phase G实现后自动生效)
        }
    }
}
