package com.dabashou.skill.controller;

import com.dabashou.common.core.AjaxResult;
import com.dabashou.common.utils.SecurityUtil;
import com.dabashou.skill.dto.UpdateUserSkillDto;
import com.dabashou.skill.dto.UserSkillDto;
import com.dabashou.skill.service.UserSkillService;
import com.dabashou.skill.vo.UserSkillVo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 用户技能控制器
 */
@Tag(name = "用户技能", description = "我的技能增删改查")
@RestController
@RequestMapping("/api/v1/users/me/skills")
public class UserSkillController {

    private final UserSkillService userSkillService;

    public UserSkillController(UserSkillService userSkillService) {
        this.userSkillService = userSkillService;
    }

    @Operation(summary = "获取我的技能列表")
    @GetMapping
    public AjaxResult<List<UserSkillVo>> listMySkills() {
        Long userId = SecurityUtil.requireCurrentUserId();
        return AjaxResult.ok(userSkillService.listMySkills(userId));
    }

    @Operation(summary = "添加技能")
    @PostMapping
    public AjaxResult<Map<String, Long>> addSkill(@Valid @RequestBody UserSkillDto dto) {
        Long userId = SecurityUtil.requireCurrentUserId();
        Long id = userSkillService.addSkill(userId, dto);
        return AjaxResult.ok(Map.of("id", id));
    }

    @Operation(summary = "更新技能")
    @PutMapping("/{id}")
    public AjaxResult<Void> updateSkill(@PathVariable Long id, @Valid @RequestBody UpdateUserSkillDto dto) {
        Long userId = SecurityUtil.requireCurrentUserId();
        userSkillService.updateSkill(userId, id, dto);
        return AjaxResult.ok();
    }

    @Operation(summary = "删除技能")
    @DeleteMapping("/{id}")
    public AjaxResult<Void> deleteSkill(@PathVariable Long id) {
        Long userId = SecurityUtil.requireCurrentUserId();
        userSkillService.deleteSkill(userId, id);
        return AjaxResult.ok();
    }
}
