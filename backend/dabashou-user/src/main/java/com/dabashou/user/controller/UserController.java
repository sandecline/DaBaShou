package com.dabashou.user.controller;

import com.dabashou.common.core.AjaxResult;
import com.dabashou.common.utils.SecurityUtil;
import com.dabashou.user.dto.CampusAuthDto;
import com.dabashou.user.dto.UpdateLocationDto;
import com.dabashou.user.dto.UpdatePasswordDto;
import com.dabashou.user.dto.UpdateProfileDto;
import com.dabashou.user.service.UserCampusAuthService;
import com.dabashou.user.service.UserService;
import com.dabashou.user.vo.CampusAuthVo;
import com.dabashou.user.vo.UserProfileVo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "用户管理", description = "个人资料、密码、位置、校园认证、信任分")
@RestController
@RequestMapping("/api/v1/user")
public class UserController {

    private final UserService userService;
    private final UserCampusAuthService campusAuthService;

    public UserController(UserService userService, UserCampusAuthService campusAuthService) {
        this.userService = userService;
        this.campusAuthService = campusAuthService;
    }

    @Operation(summary = "获取个人资料")
    @GetMapping("/profile")
    public AjaxResult<UserProfileVo> getProfile() {
        Long userId = SecurityUtil.requireCurrentUserId();
        return AjaxResult.ok(userService.getProfile(userId));
    }

    @Operation(summary = "更新个人资料")
    @PutMapping("/profile")
    public AjaxResult<Void> updateProfile(@Valid @RequestBody UpdateProfileDto dto) {
        Long userId = SecurityUtil.requireCurrentUserId();
        userService.updateProfile(userId, dto);
        return AjaxResult.ok();
    }

    @Operation(summary = "修改密码")
    @PutMapping("/password")
    public AjaxResult<Void> updatePassword(@Valid @RequestBody UpdatePasswordDto dto) {
        Long userId = SecurityUtil.requireCurrentUserId();
        userService.updatePassword(userId, dto);
        return AjaxResult.ok();
    }

    @Operation(summary = "更新位置")
    @PutMapping("/location")
    public AjaxResult<Void> updateLocation(@Valid @RequestBody UpdateLocationDto dto) {
        Long userId = SecurityUtil.requireCurrentUserId();
        userService.updateLocation(userId, dto);
        return AjaxResult.ok();
    }

    @Operation(summary = "申请校园认证")
    @PostMapping("/campus-auth")
    public AjaxResult<Void> applyCampusAuth(@Valid @RequestBody CampusAuthDto dto) {
        Long userId = SecurityUtil.requireCurrentUserId();
        campusAuthService.applyCampusAuth(userId, dto);
        return AjaxResult.ok();
    }

    @Operation(summary = "查询我的校园认证")
    @GetMapping("/campus-auth")
    public AjaxResult<CampusAuthVo> getCampusAuth() {
        Long userId = SecurityUtil.requireCurrentUserId();
        return AjaxResult.ok(campusAuthService.getCampusAuth(userId));
    }

    @Operation(summary = "查询用户公开资料")
    @GetMapping("/{userId}")
    public AjaxResult<UserProfileVo> getUserProfile(
            @Parameter(description = "用户ID") @PathVariable Long userId) {
        return AjaxResult.ok(userService.getProfile(userId));
    }
}
