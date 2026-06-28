package com.dabashou.order.vo;

/**
 * 订单状态查询VO
 */
public class OrderStatusVo {

    private Integer status;
    private String statusName;

    public OrderStatusVo() {}

    public OrderStatusVo(Integer status, String statusName) {
        this.status = status;
        this.statusName = statusName;
    }

    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }
    public String getStatusName() { return statusName; }
    public void setStatusName(String statusName) { this.statusName = statusName; }
}
