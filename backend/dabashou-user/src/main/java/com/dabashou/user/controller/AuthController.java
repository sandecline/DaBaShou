package com.dabashou.user.controller;

import com.dabashou.common.core.AjaxResult;
import com.dabashou.common.utils.SecurityUtil;
import com.dabashou.user.dto.*;
import com.dabashou.user.service.UserService;
import com.dabashou.user.vo.LoginVo;
import com.dabashou.user.vo.UserProfileVo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;

@Tag(name = "认证管理", description = "注册、登录、验证码等认证接口")
@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @Operation(summary = "用户注册")
    @PostMapping("/register")
    public AjaxResult<Void> register(@Valid @RequestBody RegisterDto dto) {
        userService.register(dto);
        return AjaxResult.ok();
    }

    @Operation(summary = "密码登录")
    @PostMapping("/login")
    public AjaxResult<LoginVo> login(@Valid @RequestBody LoginDto dto) {
        return AjaxResult.ok(userService.login(dto));
    }

    @Operation(summary = "短信登录")
    @PostMapping("/sms-login")
    public AjaxResult<LoginVo> smsLogin(@Valid @RequestBody SmsLoginDto dto) {
        return AjaxResult.ok(userService.smsLogin(dto));
    }

    @Operation(summary = "发送短信验证码")
    @PostMapping("/sms-code")
    public AjaxResult<Void> sendSmsCode(@Valid @RequestBody SmsCodeDto dto) {
        userService.sendSmsCode(dto.getPhone());
        return AjaxResult.ok();
    }

    @Operation(summary = "刷新Token")
    @PostMapping("/refresh")
    public AjaxResult<LoginVo> refreshToken(
            @Parameter(description = "refreshToken") @RequestParam String refreshToken) {
        return AjaxResult.ok(userService.refreshToken(refreshToken));
    }

    @Operation(summary = "登出")
    @PostMapping("/logout")
    public AjaxResult<Void> logout(HttpServletRequest request) {
        Long userId = SecurityUtil.requireCurrentUserId();
        String authHeader = request.getHeader("Authorization");
        String token = (authHeader != null && authHeader.startsWith("Bearer ")) ? authHeader.substring(7) : null;
        userService.logout(userId, token);
        return AjaxResult.ok();
    }
}
