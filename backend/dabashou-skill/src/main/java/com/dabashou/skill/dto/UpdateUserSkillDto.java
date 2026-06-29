package com.dabashou.skill.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

/**
 * 更新用户技能请求 DTO
 */
public class UpdateUserSkillDto {

    @Min(value = 1, message = "熟练度最小为1")
    @Max(value = 4, message = "熟练度最大为4")
    private Integer proficiency;

    private String description;

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
