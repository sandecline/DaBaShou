package com.dabashou.skill.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class UserSkillDto {

    @NotNull(message = "技能标签ID不能为空")
    private Long skillTagId;

    @Size(max = 500, message = "技能描述最长500")
    private String description;

    private Integer proficiency;

    public Long getSkillTagId() { return skillTagId; }
    public void setSkillTagId(Long skillTagId) { this.skillTagId = skillTagId; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public Integer getProficiency() { return proficiency; }
    public void setProficiency(Integer proficiency) { this.proficiency = proficiency; }
}
