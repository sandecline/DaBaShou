package com.dabashou.point.vo;

/**
 * 积分统计VO
 */
public class PointStatsVo {

    private Integer totalIncome;
    private Integer totalExpense;
    private Integer monthIncome;
    private Integer monthExpense;

    public Integer getTotalIncome() { return totalIncome; }
    public void setTotalIncome(Integer totalIncome) { this.totalIncome = totalIncome; }
    public Integer getTotalExpense() { return totalExpense; }
    public void setTotalExpense(Integer totalExpense) { this.totalExpense = totalExpense; }
    public Integer getMonthIncome() { return monthIncome; }
    public void setMonthIncome(Integer monthIncome) { this.monthIncome = monthIncome; }
    public Integer getMonthExpense() { return monthExpense; }
    public void setMonthExpense(Integer monthExpense) { this.monthExpense = monthExpense; }
}
