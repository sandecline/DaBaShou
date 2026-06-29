package com.dabashou.order.vo;

/**
 * 支付结果VO
 */
public class PayResultVo {

    private Long orderId;
    private String orderNo;
    private Integer pointAmount;
    private String verifyCode;
    private java.time.LocalDateTime verifyCodeExpire;

    public Long getOrderId() { return orderId; }
    public void setOrderId(Long orderId) { this.orderId = orderId; }
    public String getOrderNo() { return orderNo; }
    public void setOrderNo(String orderNo) { this.orderNo = orderNo; }
    public Integer getPointAmount() { return pointAmount; }
    public void setPointAmount(Integer pointAmount) { this.pointAmount = pointAmount; }
    public String getVerifyCode() { return verifyCode; }
    public void setVerifyCode(String verifyCode) { this.verifyCode = verifyCode; }
    public java.time.LocalDateTime getVerifyCodeExpire() { return verifyCodeExpire; }
    public void setVerifyCodeExpire(java.time.LocalDateTime verifyCodeExpire) { this.verifyCodeExpire = verifyCodeExpire; }
}
