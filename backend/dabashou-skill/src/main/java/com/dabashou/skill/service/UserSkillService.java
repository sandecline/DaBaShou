package com.dabashou.skill.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.dabashou.skill.domain.UserSkill;
import com.dabashou.skill.dto.UserSkillDto;
import com.dabashou.skill.vo.UserSkillVo;
import java.util.List;

public interface UserSkillService extends IService<UserSkill> {
    List<UserSkillVo> getUserSkills(Long userId);
    void addSkill(Long userId, UserSkillDto dto);
    void updateSkill(Long userId, Long skillId, UserSkillDto dto);
    void removeSkill(Long userId, Long skillId);
    List<Long> getUserSkillTagIds(Long userId);
}
