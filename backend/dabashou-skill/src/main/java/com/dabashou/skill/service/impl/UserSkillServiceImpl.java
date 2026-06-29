package com.dabashou.skill.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.dabashou.common.enums.ErrorCode;
import com.dabashou.common.exception.BusinessException;
import com.dabashou.skill.domain.SkillCategory;
import com.dabashou.skill.domain.SkillTag;
import com.dabashou.skill.domain.UserSkill;
import com.dabashou.skill.dto.UserSkillDto;
import com.dabashou.skill.mapper.UserSkillMapper;
import com.dabashou.skill.service.SkillCategoryService;
import com.dabashou.skill.service.SkillTagService;
import com.dabashou.skill.service.UserSkillService;
import com.dabashou.skill.vo.UserSkillVo;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class UserSkillServiceImpl extends ServiceImpl<UserSkillMapper, UserSkill>
        implements UserSkillService {

    private final SkillTagService tagService;
    private final SkillCategoryService categoryService;

    public UserSkillServiceImpl(SkillTagService tagService, SkillCategoryService categoryService) {
        this.tagService = tagService;
        this.categoryService = categoryService;
    }

    @Override
    public List<UserSkillVo> getUserSkills(Long userId) {
        List<UserSkill> skills = lambdaQuery()
                .eq(UserSkill::getUserId, userId)
                .list();
        Map<Long, String> tagMap = tagService.list().stream()
                .collect(Collectors.toMap(SkillTag::getId, SkillTag::getName));
        Map<Long, String> catMap = categoryService.list().stream()
                .collect(Collectors.toMap(SkillCategory::getId, SkillCategory::getName));
        Map<Long, Long> tagCatMap = tagService.list().stream()
                .collect(Collectors.toMap(SkillTag::getId, SkillTag::getCategoryId));
        return skills.stream().map(s -> {
            UserSkillVo vo = new UserSkillVo();
            vo.setId(s.getId());
            vo.setSkillTagId(s.getSkillTagId());
            vo.setTagName(tagMap.get(s.getSkillTagId()));
            Long catId = tagCatMap.get(s.getSkillTagId());
            vo.setCategoryName(catId != null ? catMap.get(catId) : null);
            vo.setProficiency(s.getProficiency());
            vo.setDescription(s.getDescription());
            return vo;
        }).toList();
    }

    @Override
    @Transactional
    public void addSkill(Long userId, UserSkillDto dto) {
        boolean exists = lambdaQuery()
                .eq(UserSkill::getUserId, userId)
                .eq(UserSkill::getSkillTagId, dto.getSkillTagId())
                .exists();
        if (exists) {
            throw new BusinessException(ErrorCode.CONFLICT, "该技能已添加");
        }
        UserSkill skill = new UserSkill();
        skill.setUserId(userId);
        skill.setSkillTagId(dto.getSkillTagId());
        skill.setProficiency(dto.getProficiency() != null ? dto.getProficiency() : 1);
        skill.setDescription(dto.getDescription());
        save(skill);
    }

    @Override
    @Transactional
    public void updateSkill(Long userId, Long skillId, UserSkillDto dto) {
        UserSkill skill = getById(skillId);
        if (skill == null || !skill.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "技能不存在");
        }
        if (dto.getSkillTagId() != null) skill.setSkillTagId(dto.getSkillTagId());
        if (dto.getProficiency() != null) skill.setProficiency(dto.getProficiency());
        if (dto.getDescription() != null) skill.setDescription(dto.getDescription());
        updateById(skill);
    }

    @Override
    @Transactional
    public void removeSkill(Long userId, Long skillId) {
        UserSkill skill = getById(skillId);
        if (skill == null || !skill.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "技能不存在");
        }
        removeById(skillId);
    }

    @Override
    public List<Long> getUserSkillTagIds(Long userId) {
        return lambdaQuery()
                .eq(UserSkill::getUserId, userId)
                .list()
                .stream()
                .map(UserSkill::getSkillTagId)
                .toList();
    }
}
