package com.dabashou.system.vo;

import java.util.List;

public class PermissionVo {

    private Long id;
    private Long parentId;
    private String permissionCode;
    private String permissionName;
    private Integer type;
    private String path;
    private String icon;
    private Integer sortOrder;
    private Integer status;
    private List<PermissionVo> children;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getParentId() { return parentId; }
    public void setParentId(Long parentId) { this.parentId = parentId; }
    public String getPermissionCode() { return permissionCode; }
    public void setPermissionCode(String permissionCode) { this.permissionCode = permissionCode; }
    public String getPermissionName() { return permissionName; }
    public void setPermissionName(String permissionName) { this.permissionName = permissionName; }
    public Integer getType() { return type; }
    public void setType(Integer type) { this.type = type; }
    public String getPath() { return path; }
    public void setPath(String path) { this.path = path; }
    public String getIcon() { return icon; }
    public void setIcon(String icon) { this.icon = icon; }
    public Integer getSortOrder() { return sortOrder; }
    public void setSortOrder(Integer sortOrder) { this.sortOrder = sortOrder; }
    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }
    public List<PermissionVo> getChildren() { return children; }
    public void setChildren(List<PermissionVo> children) { this.children = children; }
}
