package com.dabashou.point.vo;

/**
 * 签到结果VO
 */
public class SignInVo {

    private Boolean todaySigned;
    private Integer reward;
    private Integer consecutiveDays;

    public SignInVo() {
    }

    public SignInVo(Integer reward, Integer consecutiveDays) {
        this.todaySigned = true;
        this.reward = reward;
        this.consecutiveDays = consecutiveDays;
    }

    public Boolean getTodaySigned() { return todaySigned; }
    public void setTodaySigned(Boolean todaySigned) { this.todaySigned = todaySigned; }
    public Integer getReward() { return reward; }
    public void setReward(Integer reward) { this.reward = reward; }
    public Integer getConsecutiveDays() { return consecutiveDays; }
    public void setConsecutiveDays(Integer consecutiveDays) { this.consecutiveDays = consecutiveDays; }
}
