package com.dabashou.shelf.vo;

import java.time.LocalDateTime;

/**
 * 货架详情 VO — 继承 ShelfItemVo
 */
public class ShelfDetailVo extends ShelfItemVo {

    private String description;

    private String locationTypeDesc;

    private String statusDesc;

    private LocalDateTime createTime;

    // ---- Getters and Setters ----

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getLocationTypeDesc() {
        return locationTypeDesc;
    }

    public void setLocationTypeDesc(String locationTypeDesc) {
        this.locationTypeDesc = locationTypeDesc;
    }

    public String getStatusDesc() {
        return statusDesc;
    }

    public void setStatusDesc(String statusDesc) {
        this.statusDesc = statusDesc;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }
}
