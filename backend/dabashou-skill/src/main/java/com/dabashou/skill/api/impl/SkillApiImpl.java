package com.dabashou.skill.api.impl;

import com.dabashou.skill.api.SkillApi;
import com.dabashou.skill.domain.SkillTag;
import com.dabashou.skill.service.SkillTagService;
import com.dabashou.skill.service.UserSkillService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SkillApiImpl implements SkillApi {

    private final SkillTagService tagService;
    private final UserSkillService userSkillService;

    public SkillApiImpl(SkillTagService tagService, UserSkillService userSkillService) {
        this.tagService = tagService;
        this.userSkillService = userSkillService;
    }

    @Override
    public String getTagName(Long tagId) {
        SkillTag tag = tagService.getById(tagId);
        return tag != null ? tag.getName() : null;
    }

    @Override
    public Long getTagCategoryId(Long tagId) {
        SkillTag tag = tagService.getById(tagId);
        return tag != null ? tag.getCategoryId() : null;
    }

    @Override
    public List<Long> getUserSkillTagIds(Long userId) {
        return userSkillService.getUserSkillTagIds(userId);
    }

    @Override
    public boolean isTagActive(Long tagId) {
        SkillTag tag = tagService.getById(tagId);
        return tag != null && tag.getStatus() == 1;
    }
}
