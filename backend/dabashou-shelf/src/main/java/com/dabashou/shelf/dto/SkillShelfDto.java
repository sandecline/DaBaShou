package com.dabashou.shelf.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * 创建/上架技能货架 DTO
 */
public class SkillShelfDto {

    @NotNull(message = "技能标签不能为空")
    private Long skillTagId;

    @NotBlank(message = "标题不能为空")
    private String title;

    private String description;

    @NotNull(message = "积分价格不能为空")
    private Integer pointPrice;

    @NotNull(message = "服务时长不能为空")
    private Integer durationMinutes;

    @NotNull(message = "服务方式不能为空")
    private Integer locationType;

    // ---- Getters and Setters ----

    public Long getSkillTagId() {
        return skillTagId;
    }

    public void setSkillTagId(Long skillTagId) {
        this.skillTagId = skillTagId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getPointPrice() {
        return pointPrice;
    }

    public void setPointPrice(Integer pointPrice) {
        this.pointPrice = pointPrice;
    }

    public Integer getDurationMinutes() {
        return durationMinutes;
    }

    public void setDurationMinutes(Integer durationMinutes) {
        this.durationMinutes = durationMinutes;
    }

    public Integer getLocationType() {
        return locationType;
    }

    public void setLocationType(Integer locationType) {
        this.locationType = locationType;
    }
}
