package com.dabashou.user.controller;

import com.dabashou.common.core.AjaxResult;
import com.dabashou.user.service.UserService;
import com.dabashou.user.vo.PublicUserVo;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @projectName: nplab-backend
 * @package: com.dabashou.user.controller
 * @className: PublicUserController
 * @author: HuangYuYan
 * @description: 公开用户资料控制器，提供脱敏只读用户详情
 * @date: 2026-07-01 21:40:09
 * @version: 1.0
 */
@RestController
@RequestMapping("/api/v1/users")
public class PublicUserController {

    private final UserService userService;

    public PublicUserController(UserService userService) {
        this.userService = userService;
    }

    /**
     * 获取公开脱敏用户详情。
     *
     * @param userId 用户ID
     * @return 公开用户资料
     */
    @GetMapping("/{userId}")
    public AjaxResult<PublicUserVo> getPublicUser(@PathVariable Long userId) {
        return AjaxResult.ok(userService.getPublicUser(userId));
    }
}
