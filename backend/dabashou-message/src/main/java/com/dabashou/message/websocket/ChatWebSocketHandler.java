package com.dabashou.message.websocket;

import com.dabashou.message.service.ChatService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 聊天WebSocket处理器
 */
@Component
public class ChatWebSocketHandler extends TextWebSocketHandler {

    private static final Logger log = LoggerFactory.getLogger(ChatWebSocketHandler.class);

    private static final ConcurrentHashMap<Long, WebSocketSession> sessions = new ConcurrentHashMap<>();
    private static final ObjectMapper objectMapper = new ObjectMapper();

    private final ChatService chatService;
    private final JdbcTemplate jdbcTemplate;

    public ChatWebSocketHandler(ChatService chatService, JdbcTemplate jdbcTemplate) {
        this.chatService = chatService;
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        Long userId = (Long) session.getAttributes().get("userId");
        if (userId != null) {
            sessions.put(userId, session);
            log.info("WebSocket连接建立: userId={}", userId);
        } else {
            log.warn("WebSocket连接缺少userId，无法建立映射");
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        Long userId = (Long) session.getAttributes().get("userId");
        if (userId != null) {
            sessions.remove(userId);
            log.info("WebSocket连接关闭: userId={}", userId);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        Long userId = (Long) session.getAttributes().get("userId");
        if (userId == null) {
            log.warn("未认证的WebSocket消息");
            return;
        }

        try {
            Map<String, Object> payload = objectMapper.readValue(message.getPayload(), Map.class);
            String type = (String) payload.get("type");

            switch (type) {
                case "chat" -> handleChat(userId, payload);
                case "read" -> handleRead(userId, payload);
                case "ping" -> handlePing(session);
                default -> log.warn("未知消息类型: {}", type);
            }
        } catch (Exception e) {
            log.error("WebSocket消息处理异常: ", e);
        }
    }

    private void handleChat(Long senderId, Map<String, Object> payload) throws IOException {
        Long sessionId = toLong(payload.get("sessionId"));
        String content = (String) payload.get("content");
        Integer msgType = payload.get("msgType") != null
                ? ((Number) payload.get("msgType")).intValue()
                : 1;

        if (sessionId == null || content == null) {
            log.warn("chat消息缺少sessionId或content");
            return;
        }

        // 保存消息（业务方法会更新session）
        chatService.sendMessage(senderId, sessionId, content, msgType);

        // 构造返回消息
        Map<String, Object> response = Map.of(
                "type", "chat",
                "sessionId", sessionId,
                "senderId", senderId,
                "content", content,
                "msgType", msgType,
                "timestamp", System.currentTimeMillis()
        );
        String json = objectMapper.writeValueAsString(response);

        // 发送给对方（如果对方在线）
        Long otherUserId = findOtherUserId(sessionId, senderId);
        if (otherUserId != null) {
            WebSocketSession otherSession = sessions.get(otherUserId);
            if (otherSession != null && otherSession.isOpen()) {
                try {
                    synchronized (otherSession) {
                        otherSession.sendMessage(new TextMessage(json));
                    }
                } catch (IOException e) {
                    log.error("推送消息失败: otherUserId={}", otherUserId, e);
                }
            }
        }

        // 也发送给自己（回显确认）
        sessionAck(senderId, json);
    }

    private void handleRead(Long userId, Map<String, Object> payload) throws IOException {
        Long sessionId = toLong(payload.get("sessionId"));
        if (sessionId == null) {
            log.warn("read消息缺少sessionId");
            return;
        }

        // 将对方发送的消息标记为已读
        jdbcTemplate.update(
                "UPDATE dbs_chat_message SET is_read = 1 WHERE session_id = ? AND sender_id != ? AND is_read = 0",
                sessionId, userId
        );

        // 通知对方已读
        Map<String, Object> response = Map.of(
                "type", "read",
                "sessionId", sessionId,
                "readerId", userId,
                "timestamp", System.currentTimeMillis()
        );
        String json = objectMapper.writeValueAsString(response);

        Long otherUserId = findOtherUserId(sessionId, userId);
        if (otherUserId != null) {
            WebSocketSession otherSession = sessions.get(otherUserId);
            if (otherSession != null && otherSession.isOpen()) {
                try {
                    synchronized (otherSession) {
                        otherSession.sendMessage(new TextMessage(json));
                    }
                } catch (IOException e) {
                    log.error("推送已读通知失败: otherUserId={}", otherUserId, e);
                }
            }
        }
    }

    private void handlePing(WebSocketSession session) throws IOException {
        Map<String, Object> response = Map.of(
                "type", "pong",
                "timestamp", System.currentTimeMillis()
        );
        String json = objectMapper.writeValueAsString(response);
        synchronized (session) {
            session.sendMessage(new TextMessage(json));
        }
    }

    private void sessionAck(Long userId, String json) {
        WebSocketSession session = sessions.get(userId);
        if (session != null && session.isOpen()) {
            try {
                synchronized (session) {
                    session.sendMessage(new TextMessage(json));
                }
            } catch (IOException e) {
                log.error("回显消息失败: userId={}", userId, e);
            }
        }
    }

    private Long findOtherUserId(Long sessionId, Long userId) {
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(
                "SELECT user1_id, user2_id FROM dbs_chat_session WHERE id = ?", sessionId
        );
        if (rows.isEmpty()) {
            return null;
        }
        Map<String, Object> row = rows.get(0);
        Long user1Id = toLong(row.get("user1_id"));
        Long user2Id = toLong(row.get("user2_id"));
        if (user1Id == null || user2Id == null) {
            return null;
        }
        return user1Id.equals(userId) ? user2Id : user1Id;
    }

    private Long toLong(Object value) {
        if (value == null) return null;
        if (value instanceof Long l) return l;
        if (value instanceof Number n) return n.longValue();
        return Long.valueOf(value.toString());
    }
}
