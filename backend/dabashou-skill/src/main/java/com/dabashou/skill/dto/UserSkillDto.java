package com.dabashou.skill.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/**
 * 添加用户技能请求 DTO
 */
public class UserSkillDto {

    @NotNull(message = "技能标签ID不能为空")
    private Long skillTagId;

    @NotNull(message = "熟练度不能为空")
    @Min(value = 1, message = "熟练度最小为1")
    @Max(value = 4, message = "熟练度最大为4")
    private Integer proficiency;

    private String description;

    public Long getSkillTagId() {
        return skillTagId;
    }

    public void setSkillTagId(Long skillTagId) {
        this.skillTagId = skillTagId;
    }

    public Integer getProficiency() {
        return proficiency;
    }

    public void setProficiency(Integer proficiency) {
        this.proficiency = proficiency;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
