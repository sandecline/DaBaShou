package com.dabashou.point.vo;

/**
 * 积分余额VO
 */
public class PointBalanceVo {

    private Integer available;
    private Integer frozen;
    private Integer total;

    public PointBalanceVo() {
    }

    public PointBalanceVo(Integer available, Integer frozen) {
        this.available = available;
        this.frozen = frozen;
        this.total = (available != null ? available : 0) + (frozen != null ? frozen : 0);
    }

    public Integer getAvailable() { return available; }
    public void setAvailable(Integer available) { this.available = available; }
    public Integer getFrozen() { return frozen; }
    public void setFrozen(Integer frozen) { this.frozen = frozen; }
    public Integer getTotal() { return total; }
    public void setTotal(Integer total) { this.total = total; }
}
