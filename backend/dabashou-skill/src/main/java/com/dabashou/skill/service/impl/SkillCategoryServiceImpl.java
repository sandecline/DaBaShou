package com.dabashou.skill.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.dabashou.skill.domain.SkillCategory;
import com.dabashou.skill.domain.SkillTag;
import com.dabashou.skill.mapper.SkillCategoryMapper;
import com.dabashou.skill.mapper.SkillTagMapper;
import com.dabashou.skill.service.SkillCategoryService;
import com.dabashou.skill.vo.CategoryTreeVo;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * 技能分类服务实现
 */
@Service
public class SkillCategoryServiceImpl implements SkillCategoryService {

    private final SkillCategoryMapper skillCategoryMapper;
    private final SkillTagMapper skillTagMapper;

    public SkillCategoryServiceImpl(SkillCategoryMapper skillCategoryMapper, SkillTagMapper skillTagMapper) {
        this.skillCategoryMapper = skillCategoryMapper;
        this.skillTagMapper = skillTagMapper;
    }

    @Override
    public List<CategoryTreeVo> getTree() {
        // 查询所有启用分类
        LambdaQueryWrapper<SkillCategory> categoryWrapper = new LambdaQueryWrapper<>();
        categoryWrapper.eq(SkillCategory::getStatus, 1)
                .orderByAsc(SkillCategory::getSortOrder);
        List<SkillCategory> categories = skillCategoryMapper.selectList(categoryWrapper);

        // 查询所有启用标签
        LambdaQueryWrapper<SkillTag> tagWrapper = new LambdaQueryWrapper<>();
        tagWrapper.eq(SkillTag::getStatus, 1);
        List<SkillTag> allTags = skillTagMapper.selectList(tagWrapper);

        // 按分类分组标签
        List<CategoryTreeVo> result = new ArrayList<>();
        for (SkillCategory category : categories) {
            CategoryTreeVo categoryVo = new CategoryTreeVo();
            categoryVo.setId(category.getId());
            categoryVo.setName(category.getName());
            categoryVo.setIcon(category.getIcon());
            categoryVo.setSortOrder(category.getSortOrder());

            List<CategoryTreeVo> children = new ArrayList<>();
            for (SkillTag tag : allTags) {
                if (tag.getCategoryId().equals(category.getId())) {
                    CategoryTreeVo tagVo = new CategoryTreeVo();
                    tagVo.setId(tag.getId());
                    tagVo.setName(tag.getName());
                    tagVo.setIcon(null);
                    tagVo.setSortOrder(0);
                    tagVo.setChildren(null);
                    children.add(tagVo);
                }
            }
            categoryVo.setChildren(children);
            result.add(categoryVo);
        }

        return result;
    }
}
