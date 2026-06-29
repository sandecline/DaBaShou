package com.dabashou.message.controller;

import com.dabashou.common.core.AjaxResult;
import com.dabashou.common.core.PageResult;
import com.dabashou.common.utils.SecurityUtil;
import com.dabashou.message.dto.ChatMessageDto;
import com.dabashou.message.service.ChatService;
import com.dabashou.message.vo.ChatMessageVo;
import com.dabashou.message.vo.ChatSessionVo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@Tag(name = "聊天", description = "即时通讯")
@RestController
@RequestMapping("/api/v1/chat")
public class ChatController {

    private final ChatService chatService;

    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    @Operation(summary = "发起会话")
    @PostMapping("/sessions")
    public AjaxResult<Long> createSession(@Parameter(description = "对方用户ID") @RequestParam Long userId) {
        return AjaxResult.ok(chatService.createSession(SecurityUtil.requireCurrentUserId(), userId));
    }

    @Operation(summary = "会话列表")
    @GetMapping("/sessions")
    public AjaxResult<PageResult<ChatSessionVo>> getSessions(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") int pageNum,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "10") int pageSize) {
        return AjaxResult.ok(chatService.getSessions(SecurityUtil.requireCurrentUserId(), pageNum, pageSize));
    }

    @Operation(summary = "聊天记录")
    @GetMapping("/sessions/{sessionId}/messages")
    public AjaxResult<PageResult<ChatMessageVo>> getChatHistory(
            @PathVariable Long sessionId,
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") int pageNum,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "20") int pageSize) {
        return AjaxResult.ok(chatService.getChatHistory(SecurityUtil.requireCurrentUserId(), sessionId, pageNum, pageSize));
    }

    @Operation(summary = "发送消息")
    @PostMapping("/messages")
    public AjaxResult<Void> sendMessage(@Valid @RequestBody ChatMessageDto dto) {
        chatService.sendMessage(SecurityUtil.requireCurrentUserId(), dto.getSessionId(), dto.getContent(), dto.getMsgType());
        return AjaxResult.ok();
    }

    @Operation(summary = "标记已读")
    @PutMapping("/sessions/{sessionId}/read")
    public AjaxResult<Void> markRead(@PathVariable Long sessionId) {
        chatService.markRead(SecurityUtil.requireCurrentUserId(), sessionId);
        return AjaxResult.ok();
    }
}
