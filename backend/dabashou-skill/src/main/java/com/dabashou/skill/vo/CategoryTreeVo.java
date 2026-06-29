package com.dabashou.skill.vo;

import java.util.List;

/**
 * 分类树响应 VO — 技能标签作为子节点
 */
public class CategoryTreeVo {

    private Long id;
    private String name;
    private String icon;
    private Integer sortOrder;
    private List<CategoryTreeVo> children;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public Integer getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(Integer sortOrder) {
        this.sortOrder = sortOrder;
    }

    public List<CategoryTreeVo> getChildren() {
        return children;
    }

    public void setChildren(List<CategoryTreeVo> children) {
        this.children = children;
    }
}
