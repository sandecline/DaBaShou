package com.dabashou.skill.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.dabashou.skill.domain.SkillCategory;
import com.dabashou.skill.mapper.SkillCategoryMapper;
import com.dabashou.skill.service.SkillCategoryService;
import com.dabashou.skill.vo.CategoryTreeVo;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class SkillCategoryServiceImpl extends ServiceImpl<SkillCategoryMapper, SkillCategory>
        implements SkillCategoryService {

    @Override
    public List<CategoryTreeVo> getCategoryTree() {
        List<SkillCategory> all = lambdaQuery()
                .eq(SkillCategory::getStatus, 1)
                .orderByAsc(SkillCategory::getSortOrder)
                .list();
        Map<Long, List<SkillCategory>> grouped = all.stream()
                .filter(c -> c.getId() != null)
                .collect(Collectors.groupingBy(c -> c.getId()));
        List<CategoryTreeVo> roots = new ArrayList<>();
        for (SkillCategory cat : all) {
            if (cat.getSortOrder() != null && cat.getSortOrder() >= 0) {
                CategoryTreeVo vo = toTreeVo(cat);
                roots.add(vo);
            }
        }
        return roots;
    }

    private CategoryTreeVo toTreeVo(SkillCategory cat) {
        CategoryTreeVo vo = new CategoryTreeVo();
        vo.setId(cat.getId());
        vo.setName(cat.getName());
        vo.setIcon(cat.getIcon());
        vo.setSortOrder(cat.getSortOrder());
        vo.setChildren(List.of());
        return vo;
    }
}
