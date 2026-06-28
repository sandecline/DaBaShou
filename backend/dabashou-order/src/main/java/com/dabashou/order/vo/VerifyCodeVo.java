package com.dabashou.order.vo;

import java.time.LocalDateTime;

/**
 * 核销码VO
 */
public class VerifyCodeVo {

    private String verifyCode;
    private LocalDateTime expireTime;

    public String getVerifyCode() { return verifyCode; }
    public void setVerifyCode(String verifyCode) { this.verifyCode = verifyCode; }
    public LocalDateTime getExpireTime() { return expireTime; }
    public void setExpireTime(LocalDateTime expireTime) { this.expireTime = expireTime; }
}
