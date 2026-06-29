package com.dabashou.skill.service;

import com.dabashou.skill.vo.CategoryTreeVo;

import java.util.List;

/**
 * 技能分类服务接口
 */
public interface SkillCategoryService {

    /**
     * 获取分类树（含标签子节点）
     */
    List<CategoryTreeVo> getTree();
}
