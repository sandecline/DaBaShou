package com.dabashou.skill.controller;

import com.dabashou.common.core.AjaxResult;
import com.dabashou.skill.domain.SkillTag;
import com.dabashou.skill.service.SkillTagService;
import com.dabashou.skill.vo.SkillTagVo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 技能标签控制器
 */
@Tag(name = "技能标签", description = "标签列表、热门标签查询")
@RestController
@RequestMapping("/api/v1/skills/tags")
public class SkillTagController {

    private final SkillTagService skillTagService;

    public SkillTagController(SkillTagService skillTagService) {
        this.skillTagService = skillTagService;
    }

    @Operation(summary = "获取标签列表")
    @GetMapping
    public AjaxResult<List<SkillTagVo>> listTags(
            @Parameter(description = "分类ID筛选") @RequestParam(required = false) Long categoryId) {
        List<SkillTag> tags = skillTagService.listByCategory(categoryId);
        List<SkillTagVo> voList = tags.stream().map(this::toVo).collect(Collectors.toList());
        return AjaxResult.ok(voList);
    }

    @Operation(summary = "获取热门标签")
    @GetMapping("/hot")
    public AjaxResult<List<SkillTagVo>> getHotTags(
            @Parameter(description = "数量，默认10") @RequestParam(defaultValue = "10") int limit) {
        List<SkillTag> tags = skillTagService.getHotList(limit);
        List<SkillTagVo> voList = tags.stream().map(this::toVo).collect(Collectors.toList());
        return AjaxResult.ok(voList);
    }

    private SkillTagVo toVo(SkillTag tag) {
        SkillTagVo vo = new SkillTagVo();
        vo.setId(tag.getId());
        vo.setCategoryId(tag.getCategoryId());
        vo.setName(tag.getName());
        vo.setStatus(tag.getStatus());
        return vo;
    }
}
