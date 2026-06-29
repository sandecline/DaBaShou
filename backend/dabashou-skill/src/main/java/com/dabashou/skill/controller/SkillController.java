package com.dabashou.skill.controller;

import com.dabashou.common.core.AjaxResult;
import com.dabashou.common.utils.SecurityUtil;
import com.dabashou.skill.dto.UserSkillDto;
import com.dabashou.skill.service.SkillCategoryService;
import com.dabashou.skill.service.SkillTagService;
import com.dabashou.skill.service.UserSkillService;
import com.dabashou.skill.vo.CategoryTreeVo;
import com.dabashou.skill.vo.SkillTagVo;
import com.dabashou.skill.vo.UserSkillVo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "技能管理", description = "技能分类、标签、用户技能")
@RestController
@RequestMapping("/api/v1/skills")
public class SkillController {

    private final SkillCategoryService categoryService;
    private final SkillTagService tagService;
    private final UserSkillService userSkillService;

    public SkillController(SkillCategoryService categoryService,
                           SkillTagService tagService,
                           UserSkillService userSkillService) {
        this.categoryService = categoryService;
        this.tagService = tagService;
        this.userSkillService = userSkillService;
    }

    @Operation(summary = "获取分类树")
    @GetMapping("/categories/tree")
    public AjaxResult<List<CategoryTreeVo>> getCategoryTree() {
        return AjaxResult.ok(categoryService.getCategoryTree());
    }

    @Operation(summary = "获取标签列表")
    @GetMapping("/tags")
    public AjaxResult<List<SkillTagVo>> getTags(
            @Parameter(description = "分类ID") @RequestParam(required = false) Long categoryId) {
        return AjaxResult.ok(tagService.getTagsByCategoryId(categoryId));
    }

    @Operation(summary = "获取热门标签")
    @GetMapping("/tags/hot")
    public AjaxResult<List<SkillTagVo>> getHotTags(
            @Parameter(description = "数量限制") @RequestParam(defaultValue = "20") int limit) {
        return AjaxResult.ok(tagService.getHotTags(limit));
    }

    @Operation(summary = "获取我的技能列表")
    @GetMapping("/users/me/skills")
    public AjaxResult<List<UserSkillVo>> getMySkills() {
        Long userId = SecurityUtil.requireCurrentUserId();
        return AjaxResult.ok(userSkillService.getUserSkills(userId));
    }

    @Operation(summary = "添加技能")
    @PostMapping("/users/me/skills")
    public AjaxResult<Void> addSkill(@Valid @RequestBody UserSkillDto dto) {
        Long userId = SecurityUtil.requireCurrentUserId();
        userSkillService.addSkill(userId, dto);
        return AjaxResult.ok();
    }

    @Operation(summary = "更新技能")
    @PutMapping("/users/me/skills/{skillId}")
    public AjaxResult<Void> updateSkill(
            @Parameter(description = "技能ID") @PathVariable Long skillId,
            @Valid @RequestBody UserSkillDto dto) {
        Long userId = SecurityUtil.requireCurrentUserId();
        userSkillService.updateSkill(userId, skillId, dto);
        return AjaxResult.ok();
    }

    @Operation(summary = "删除技能")
    @DeleteMapping("/users/me/skills/{skillId}")
    public AjaxResult<Void> removeSkill(
            @Parameter(description = "技能ID") @PathVariable Long skillId) {
        Long userId = SecurityUtil.requireCurrentUserId();
        userSkillService.removeSkill(userId, skillId);
        return AjaxResult.ok();
    }
}
