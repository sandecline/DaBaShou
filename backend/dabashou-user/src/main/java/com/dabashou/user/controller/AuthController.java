package com.dabashou.user.controller;

import com.dabashou.common.core.AjaxResult;
import com.dabashou.common.enums.ErrorCode;
import com.dabashou.common.exception.BusinessException;
import com.dabashou.user.dto.LoginDto;
import com.dabashou.user.dto.RegisterDto;
import com.dabashou.user.dto.SmsLoginDto;
import com.dabashou.user.service.UserService;
import com.dabashou.user.vo.LoginVo;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 认证控制器 — 注册、登录、token刷新、退出（无需认证）
 */
@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    /**
     * 用户注册
     */
    @PostMapping("/register")
    public AjaxResult<Void> register(@Valid @RequestBody RegisterDto dto) {
        try {
            userService.register(dto);
            return AjaxResult.ok();
        } catch (BusinessException e) {
            return AjaxResult.fail(e.getCode(), e.getMessage());
        }
    }

    /**
     * 密码登录
     */
    @PostMapping("/login")
    public AjaxResult<LoginVo> login(@Valid @RequestBody LoginDto dto) {
        try {
            LoginVo vo = userService.login(dto);
            return AjaxResult.ok(vo);
        } catch (BusinessException e) {
            return AjaxResult.fail(e.getCode(), e.getMessage());
        }
    }

    /**
     * 发送短信验证码
     */
    @PostMapping("/sms-code")
    public AjaxResult<Void> sendSmsCode(@RequestBody Map<String, String> body) {
        String phone = body.get("phone");
        if (phone == null || phone.isBlank()) {
            return AjaxResult.fail(ErrorCode.BAD_REQUEST, "手机号不能为空");
        }
        userService.sendSmsCode(phone);
        return AjaxResult.ok();
    }

    /**
     * 短信验证码登录
     */
    @PostMapping("/sms-login")
    public AjaxResult<LoginVo> smsLogin(@Valid @RequestBody SmsLoginDto dto) {
        try {
            LoginVo vo = userService.smsLogin(dto);
            return AjaxResult.ok(vo);
        } catch (BusinessException e) {
            return AjaxResult.fail(e.getCode(), e.getMessage());
        }
    }

    /**
     * 刷新token
     */
    @PostMapping("/refresh")
    public AjaxResult<LoginVo> refresh(@RequestBody Map<String, String> body) {
        String refreshToken = body.get("refreshToken");
        if (refreshToken == null || refreshToken.isBlank()) {
            return AjaxResult.fail(ErrorCode.BAD_REQUEST, "refreshToken不能为空");
        }
        try {
            LoginVo vo = userService.refreshToken(refreshToken);
            return AjaxResult.ok(vo);
        } catch (Exception e) {
            return AjaxResult.fail(ErrorCode.UNAUTHORIZED, "刷新令牌无效或已过期");
        }
    }

    /**
     * 退出登录（无状态JWT，客户端丢弃token即可）
     */
    @PostMapping("/logout")
    public AjaxResult<Void> logout() {
        return AjaxResult.ok();
    }
}
