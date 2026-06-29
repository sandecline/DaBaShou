package com.dabashou.skill.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.dabashou.skill.domain.SkillCategory;
import com.dabashou.skill.domain.SkillTag;
import com.dabashou.skill.mapper.SkillTagMapper;
import com.dabashou.skill.service.SkillCategoryService;
import com.dabashou.skill.service.SkillTagService;
import com.dabashou.skill.vo.SkillTagVo;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class SkillTagServiceImpl extends ServiceImpl<SkillTagMapper, SkillTag>
        implements SkillTagService {

    private final SkillCategoryService categoryService;

    public SkillTagServiceImpl(SkillCategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @Override
    public List<SkillTagVo> getTagsByCategoryId(Long categoryId) {
        List<SkillTag> tags = lambdaQuery()
                .eq(SkillTag::getStatus, 1)
                .eq(categoryId != null, SkillTag::getCategoryId, categoryId)
                .list();
        Map<Long, String> categoryMap = categoryService.list().stream()
                .collect(Collectors.toMap(SkillCategory::getId, SkillCategory::getName));
        return tags.stream().map(t -> {
            SkillTagVo vo = new SkillTagVo();
            vo.setId(t.getId());
            vo.setCategoryId(t.getCategoryId());
            vo.setCategoryName(categoryMap.get(t.getCategoryId()));
            vo.setName(t.getName());
            return vo;
        }).toList();
    }

    @Override
    public List<SkillTagVo> getHotTags(int limit) {
        List<SkillTag> tags = lambdaQuery()
                .eq(SkillTag::getStatus, 1)
                .orderByDesc(SkillTag::getId)
                .last("LIMIT " + limit)
                .list();
        return tags.stream().map(t -> {
            SkillTagVo vo = new SkillTagVo();
            vo.setId(t.getId());
            vo.setCategoryId(t.getCategoryId());
            vo.setName(t.getName());
            return vo;
        }).toList();
    }
}
