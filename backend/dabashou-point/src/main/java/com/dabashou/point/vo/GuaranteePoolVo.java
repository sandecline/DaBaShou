package com.dabashou.point.vo;

/**
 * 担保池概览VO
 */
public class GuaranteePoolVo {

    private Integer totalPool;
    private Integer frozenAmount;
    private Integer availableAmount;

    public GuaranteePoolVo() {
    }

    public GuaranteePoolVo(Integer totalPool, Integer frozenAmount, Integer availableAmount) {
        this.totalPool = totalPool;
        this.frozenAmount = frozenAmount;
        this.availableAmount = availableAmount;
    }

    public Integer getTotalPool() { return totalPool; }
    public void setTotalPool(Integer totalPool) { this.totalPool = totalPool; }
    public Integer getFrozenAmount() { return frozenAmount; }
    public void setFrozenAmount(Integer frozenAmount) { this.frozenAmount = frozenAmount; }
    public Integer getAvailableAmount() { return availableAmount; }
    public void setAvailableAmount(Integer availableAmount) { this.availableAmount = availableAmount; }
}
