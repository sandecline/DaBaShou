package com.dabashou.shelf.dto;

import jakarta.validation.constraints.*;
import java.util.List;

public class SkillShelfDto {

    @NotNull(message = "技能标签ID不能为空")
    private Long skillTagId;

    @NotBlank(message = "标题不能为空")
    @Size(max = 100, message = "标题最长100")
    private String title;

    private String description;

    @NotNull(message = "积分价格不能为空")
    @Min(value = 1, message = "积分价格至少1")
    private Integer pointPrice;

    private Integer durationMinutes;

    @Min(value = 1, message = "地点类型1-3")
    @Max(value = 3, message = "地点类型1-3")
    private Integer locationType;

    private List<String> images;

    public Long getSkillTagId() { return skillTagId; }
    public void setSkillTagId(Long skillTagId) { this.skillTagId = skillTagId; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public Integer getPointPrice() { return pointPrice; }
    public void setPointPrice(Integer pointPrice) { this.pointPrice = pointPrice; }
    public Integer getDurationMinutes() { return durationMinutes; }
    public void setDurationMinutes(Integer durationMinutes) { this.durationMinutes = durationMinutes; }
    public Integer getLocationType() { return locationType; }
    public void setLocationType(Integer locationType) { this.locationType = locationType; }
    public List<String> getImages() { return images; }
    public void setImages(List<String> images) { this.images = images; }
}
