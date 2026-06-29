package com.dabashou.message.controller;

import com.dabashou.common.core.AjaxResult;
import com.dabashou.common.core.PageResult;
import com.dabashou.common.exception.BusinessException;
import com.dabashou.common.utils.SecurityUtil;
import com.dabashou.message.dto.ChatSessionDto;
import com.dabashou.message.service.ChatService;
import com.dabashou.message.vo.ChatMessageVo;
import com.dabashou.message.vo.ChatSessionVo;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

/**
 * 聊天控制器
 */
@RestController
@RequestMapping("/api/v1/chat")
public class ChatController {

    private final ChatService chatService;

    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    /**
     * 当前用户的会话列表
     */
    @GetMapping("/sessions")
    public AjaxResult<PageResult<ChatSessionVo>> listSessions(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "20") int pageSize) {
        Long userId = SecurityUtil.requireCurrentUserId();
        try {
            PageResult<ChatSessionVo> result = chatService.listSessions(userId, pageNum, pageSize);
            return AjaxResult.ok(result);
        } catch (BusinessException e) {
            return AjaxResult.fail(e.getCode(), e.getMessage());
        }
    }

    /**
     * 创建或返回已有会话
     */
    @PostMapping("/sessions")
    public AjaxResult<Long> createSession(@Valid @RequestBody ChatSessionDto dto) {
        Long userId = SecurityUtil.requireCurrentUserId();
        try {
            Long sessionId = chatService.createSession(userId, dto);
            return AjaxResult.ok(sessionId);
        } catch (BusinessException e) {
            return AjaxResult.fail(e.getCode(), e.getMessage());
        }
    }

    /**
     * 分页查消息
     */
    @GetMapping("/sessions/{sessionId}/messages")
    public AjaxResult<PageResult<ChatMessageVo>> getMessages(
            @PathVariable Long sessionId,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "20") int pageSize) {
        Long userId = SecurityUtil.requireCurrentUserId();
        try {
            PageResult<ChatMessageVo> result = chatService.getMessages(userId, sessionId, pageNum, pageSize);
            return AjaxResult.ok(result);
        } catch (BusinessException e) {
            return AjaxResult.fail(e.getCode(), e.getMessage());
        }
    }
}
