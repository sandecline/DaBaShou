package com.dabashou.skill.service;

import com.dabashou.skill.dto.UpdateUserSkillDto;
import com.dabashou.skill.dto.UserSkillDto;
import com.dabashou.skill.vo.UserSkillVo;

import java.util.List;

/**
 * 用户技能服务接口
 */
public interface UserSkillService {

    /**
     * 获取当前用户的所有技能
     */
    List<UserSkillVo> listMySkills(Long userId);

    /**
     * 添加用户技能
     *
     * @return 新增技能ID
     */
    Long addSkill(Long userId, UserSkillDto dto);

    /**
     * 更新用户技能
     */
    void updateSkill(Long userId, Long id, UpdateUserSkillDto dto);

    /**
     * 删除用户技能
     */
    void deleteSkill(Long userId, Long id);
}
