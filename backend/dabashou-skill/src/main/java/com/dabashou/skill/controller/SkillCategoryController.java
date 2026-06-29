package com.dabashou.skill.controller;

import com.dabashou.common.core.AjaxResult;
import com.dabashou.skill.service.SkillCategoryService;
import com.dabashou.skill.vo.CategoryTreeVo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 技能分类控制器
 */
@Tag(name = "技能分类", description = "分类树查询")
@RestController
@RequestMapping("/api/v1/skills/categories")
public class SkillCategoryController {

    private final SkillCategoryService skillCategoryService;

    public SkillCategoryController(SkillCategoryService skillCategoryService) {
        this.skillCategoryService = skillCategoryService;
    }

    @Operation(summary = "获取分类树")
    @GetMapping("/tree")
    public AjaxResult<List<CategoryTreeVo>> getTree() {
        return AjaxResult.ok(skillCategoryService.getTree());
    }
}
