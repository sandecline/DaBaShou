package com.dabashou.skill.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.dabashou.skill.domain.SkillTag;
import com.dabashou.skill.vo.SkillTagVo;
import java.util.List;

public interface SkillTagService extends IService<SkillTag> {
    List<SkillTagVo> getTagsByCategoryId(Long categoryId);
    List<SkillTagVo> getHotTags(int limit);
}
