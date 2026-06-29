package com.dabashou.point.vo;

public class SignInVo {
    private int reward;
    private int consecutiveDays;

    public SignInVo() {}
    public SignInVo(int reward, int consecutiveDays) {
        this.reward = reward;
        this.consecutiveDays = consecutiveDays;
    }

    public int getReward() { return reward; }
    public void setReward(int reward) { this.reward = reward; }
    public int getConsecutiveDays() { return consecutiveDays; }
    public void setConsecutiveDays(int consecutiveDays) { this.consecutiveDays = consecutiveDays; }
}
