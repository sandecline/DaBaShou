package com.dabashou.message.websocket;

import com.dabashou.common.utils.JwtUtil;
import com.dabashou.message.service.ChatService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.*;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class ChatWebSocketHandler extends TextWebSocketHandler {

    private final ChatService chatService;
    private final ObjectMapper objectMapper;

    @Value("${dabashou.jwt.secret}")
    private String jwtSecret;

    private static final Map<Long, WebSocketSession> SESSIONS = new ConcurrentHashMap<>();

    public ChatWebSocketHandler(ChatService chatService, ObjectMapper objectMapper) {
        this.chatService = chatService;
        this.objectMapper = objectMapper;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        String query = session.getUri() != null ? session.getUri().getQuery() : null;
        String token = null;
        if (query != null) {
            for (String param : query.split("&")) {
                if (param.startsWith("token=")) {
                    token = param.substring(6);
                    break;
                }
            }
        }
        if (token == null || token.isEmpty()) {
            session.close(CloseStatus.NOT_ACCEPTABLE);
            return;
        }
        try {
            Claims claims = JwtUtil.parseToken(token, jwtSecret);
            Long userId = JwtUtil.getUserId(claims);
            session.getAttributes().put("userId", userId);
            SESSIONS.put(userId, session);
        } catch (Exception e) {
            session.close(CloseStatus.NOT_ACCEPTABLE);
        }
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        Long userId = (Long) session.getAttributes().get("userId");
        if (userId == null) return;

        JsonNode node = objectMapper.readTree(message.getPayload());
        String type = node.has("type") ? node.get("type").asText() : "";

        switch (type) {
            case "chat" -> {
                Long sessionId = node.get("sessionId").asLong();
                String content = node.get("content").asText();
                int msgType = node.has("msgType") ? node.get("msgType").asInt() : 1;
                chatService.sendMessage(userId, sessionId, content, msgType);
                sendToUser(userId, objectMapper.writeValueAsString(
                    Map.of("type", "sent", "sessionId", sessionId, "content", content)));
            }
            case "read" -> {
                Long sessionId = node.get("sessionId").asLong();
                chatService.markRead(userId, sessionId);
            }
            case "ping" -> {
                session.sendMessage(new TextMessage("{\"type\":\"pong\"}"));
            }
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        Long userId = (Long) session.getAttributes().get("userId");
        if (userId != null) {
            SESSIONS.remove(userId);
        }
    }

    public void sendToUser(Long userId, String message) {
        WebSocketSession session = SESSIONS.get(userId);
        if (session != null && session.isOpen()) {
            try {
                session.sendMessage(new TextMessage(message));
            } catch (IOException e) {
                SESSIONS.remove(userId);
            }
        }
    }
}
