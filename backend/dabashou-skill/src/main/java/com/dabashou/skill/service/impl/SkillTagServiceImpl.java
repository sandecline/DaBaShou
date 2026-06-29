package com.dabashou.skill.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.dabashou.skill.domain.SkillTag;
import com.dabashou.skill.mapper.SkillTagMapper;
import com.dabashou.skill.service.SkillTagService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 技能标签服务实现
 */
@Service
public class SkillTagServiceImpl implements SkillTagService {

    private final SkillTagMapper skillTagMapper;

    public SkillTagServiceImpl(SkillTagMapper skillTagMapper) {
        this.skillTagMapper = skillTagMapper;
    }

    @Override
    public List<SkillTag> listByCategory(Long categoryId) {
        LambdaQueryWrapper<SkillTag> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SkillTag::getStatus, 1);
        if (categoryId != null) {
            wrapper.eq(SkillTag::getCategoryId, categoryId);
        }
        wrapper.orderByAsc(SkillTag::getId);
        return skillTagMapper.selectList(wrapper);
    }

    @Override
    public List<SkillTag> getHotList(int limit) {
        LambdaQueryWrapper<SkillTag> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SkillTag::getStatus, 1)
                .orderByAsc(SkillTag::getId);
        Page<SkillTag> page = new Page<>(1, limit);
        Page<SkillTag> result = skillTagMapper.selectPage(page, wrapper);
        return result.getRecords();
    }
}
