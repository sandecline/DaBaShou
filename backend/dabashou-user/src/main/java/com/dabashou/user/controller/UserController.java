package com.dabashou.user.controller;

import com.dabashou.common.core.AjaxResult;
import com.dabashou.common.exception.BusinessException;
import com.dabashou.common.utils.SecurityUtil;
import com.dabashou.user.dto.CampusAuthDto;
import com.dabashou.user.dto.ChangePasswordDto;
import com.dabashou.user.dto.UpdateLocationDto;
import com.dabashou.user.dto.UpdateProfileDto;
import com.dabashou.user.service.UserService;
import com.dabashou.user.vo.CampusAuthVo;
import com.dabashou.user.vo.TrustScoreVo;
import com.dabashou.user.vo.UserProfileVo;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

/**
 * 用户控制器 — 个人信息、校园认证、信任分（需认证）
 */
@RestController
@RequestMapping("/api/v1/user")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    /**
     * 获取个人信息
     */
    @GetMapping("/profile")
    public AjaxResult<UserProfileVo> getProfile() {
        Long userId = SecurityUtil.requireCurrentUserId();
        try {
            UserProfileVo vo = userService.getProfile(userId);
            return AjaxResult.ok(vo);
        } catch (BusinessException e) {
            return AjaxResult.fail(e.getCode(), e.getMessage());
        }
    }

    /**
     * 更新个人信息
     */
    @PutMapping("/profile")
    public AjaxResult<Void> updateProfile(@Valid @RequestBody UpdateProfileDto dto) {
        Long userId = SecurityUtil.requireCurrentUserId();
        try {
            userService.updateProfile(userId, dto);
            return AjaxResult.ok();
        } catch (BusinessException e) {
            return AjaxResult.fail(e.getCode(), e.getMessage());
        }
    }

    /**
     * 修改密码
     */
    @PutMapping("/password")
    public AjaxResult<Void> changePassword(@Valid @RequestBody ChangePasswordDto dto) {
        Long userId = SecurityUtil.requireCurrentUserId();
        try {
            userService.changePassword(userId, dto);
            return AjaxResult.ok();
        } catch (BusinessException e) {
            return AjaxResult.fail(e.getCode(), e.getMessage());
        }
    }

    /**
     * 更新位置
     */
    @PutMapping("/location")
    public AjaxResult<Void> updateLocation(@Valid @RequestBody UpdateLocationDto dto) {
        Long userId = SecurityUtil.requireCurrentUserId();
        try {
            userService.updateLocation(userId, dto);
            return AjaxResult.ok();
        } catch (BusinessException e) {
            return AjaxResult.fail(e.getCode(), e.getMessage());
        }
    }

    /**
     * 提交校园认证
     */
    @PostMapping("/campus-auth")
    public AjaxResult<Void> submitCampusAuth(@Valid @RequestBody CampusAuthDto dto) {
        Long userId = SecurityUtil.requireCurrentUserId();
        try {
            userService.submitCampusAuth(userId, dto);
            return AjaxResult.ok();
        } catch (BusinessException e) {
            return AjaxResult.fail(e.getCode(), e.getMessage());
        }
    }

    /**
     * 获取校园认证信息
     */
    @GetMapping("/campus-auth")
    public AjaxResult<CampusAuthVo> getCampusAuth() {
        Long userId = SecurityUtil.requireCurrentUserId();
        try {
            CampusAuthVo vo = userService.getCampusAuth(userId);
            return AjaxResult.ok(vo);
        } catch (BusinessException e) {
            return AjaxResult.fail(e.getCode(), e.getMessage());
        }
    }

    /**
     * 获取信任分及最近日志
     */
    @GetMapping("/trust-score")
    public AjaxResult<TrustScoreVo> getTrustScore() {
        Long userId = SecurityUtil.requireCurrentUserId();
        try {
            TrustScoreVo vo = userService.getTrustScore(userId);
            return AjaxResult.ok(vo);
        } catch (BusinessException e) {
            return AjaxResult.fail(e.getCode(), e.getMessage());
        }
    }
}
