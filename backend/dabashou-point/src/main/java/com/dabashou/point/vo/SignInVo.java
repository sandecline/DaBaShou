package com.dabashou.point.vo;

/**
 * 签到结果VO
 */
public class SignInVo {

    private Integer reward;
    private Integer consecutiveDays;

    public SignInVo() {
    }

    public SignInVo(Integer reward, Integer consecutiveDays) {
        this.reward = reward;
        this.consecutiveDays = consecutiveDays;
    }

    public Integer getReward() { return reward; }
    public void setReward(Integer reward) { this.reward = reward; }
    public Integer getConsecutiveDays() { return consecutiveDays; }
    public void setConsecutiveDays(Integer consecutiveDays) { this.consecutiveDays = consecutiveDays; }
}
