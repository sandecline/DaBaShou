package com.dabashou.skill.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.dabashou.skill.domain.SkillCategory;
import com.dabashou.skill.vo.CategoryTreeVo;
import java.util.List;

public interface SkillCategoryService extends IService<SkillCategory> {
    List<CategoryTreeVo> getCategoryTree();
}
