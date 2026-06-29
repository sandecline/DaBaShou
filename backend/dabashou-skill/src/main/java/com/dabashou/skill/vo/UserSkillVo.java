package com.dabashou.skill.vo;

import java.time.LocalDateTime;

/**
 * 用户技能响应 VO
 */
public class UserSkillVo {

    private Long id;
    private Long skillTagId;
    private String skillTagName;
    private String categoryName;
    private Integer proficiency;
    private String proficiencyDesc;
    private String description;
    private LocalDateTime createTime;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getSkillTagId() {
        return skillTagId;
    }

    public void setSkillTagId(Long skillTagId) {
        this.skillTagId = skillTagId;
    }

    public String getSkillTagName() {
        return skillTagName;
    }

    public void setSkillTagName(String skillTagName) {
        this.skillTagName = skillTagName;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public Integer getProficiency() {
        return proficiency;
    }

    public void setProficiency(Integer proficiency) {
        this.proficiency = proficiency;
    }

    public String getProficiencyDesc() {
        return proficiencyDesc;
    }

    public void setProficiencyDesc(String proficiencyDesc) {
        this.proficiencyDesc = proficiencyDesc;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }
}
