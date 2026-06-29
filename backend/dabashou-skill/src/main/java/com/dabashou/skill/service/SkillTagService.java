package com.dabashou.skill.service;

import com.dabashou.skill.domain.SkillTag;

import java.util.List;

/**
 * 技能标签服务接口
 */
public interface SkillTagService {

    /**
     * 根据分类查询标签列表
     */
    List<SkillTag> listByCategory(Long categoryId);

    /**
     * 获取热门标签列表
     */
    List<SkillTag> getHotList(int limit);
}
