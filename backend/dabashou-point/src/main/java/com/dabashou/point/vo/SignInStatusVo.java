package com.dabashou.point.vo;

public class SignInStatusVo {
    private boolean todaySigned;
    private int consecutiveDays;
    private int reward;

    public boolean isTodaySigned() { return todaySigned; }
    public void setTodaySigned(boolean todaySigned) { this.todaySigned = todaySigned; }
    public int getConsecutiveDays() { return consecutiveDays; }
    public void setConsecutiveDays(int consecutiveDays) { this.consecutiveDays = consecutiveDays; }
    public int getReward() { return reward; }
    public void setReward(int reward) { this.reward = reward; }
}
