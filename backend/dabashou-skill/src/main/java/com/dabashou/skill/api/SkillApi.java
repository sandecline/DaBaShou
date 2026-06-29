package com.dabashou.skill.api;

import java.util.List;

/**
 * 技能模块跨模块API接口
 */
public interface SkillApi {

    /**
     * 获取标签名称
     */
    String getTagName(Long tagId);

    /**
     * 获取标签所属分类ID
     */
    Long getTagCategoryId(Long tagId);

    /**
     * 获取用户拥有的技能标签ID列表
     */
    List<Long> getUserSkillTagIds(Long userId);

    /**
     * 检查标签是否存在且启用
     */
    boolean isTagActive(Long tagId);
}
