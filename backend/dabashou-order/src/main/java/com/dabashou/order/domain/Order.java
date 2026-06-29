package com.dabashou.order.domain;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.dabashou.common.core.BaseEntity;

import java.time.LocalDateTime;

/**
 * 订单实体 — 对应 dbs_order 表(V1.0.0:127)
 * 继承 BaseEntity，获得 id / createTime / updateTime / deleted（逻辑删除）
 */
@TableName("dbs_order")
public class Order extends BaseEntity {

    private String orderNo;
    private Long buyerId;
    private Long sellerId;
    private Long demandId;
    private Long skillShelfId;
    private Long skillTagId;
    private String title;
    private Integer pointAmount;
    private Integer status;
    private String verifyCode;
    private LocalDateTime verifyCodeExpire;
    private Long timeSlotId;
    private LocalDateTime serviceStartTime;
    private LocalDateTime serviceEndTime;
    private LocalDateTime completeTime;
    private LocalDateTime cancelTime;
    private String cancelReason;
    private String remark;
    // id / createTime / updateTime / deleted 继承自 BaseEntity

    public String getOrderNo() { return orderNo; }
    public void setOrderNo(String orderNo) { this.orderNo = orderNo; }
    public Long getBuyerId() { return buyerId; }
    public void setBuyerId(Long buyerId) { this.buyerId = buyerId; }
    public Long getSellerId() { return sellerId; }
    public void setSellerId(Long sellerId) { this.sellerId = sellerId; }
    public Long getDemandId() { return demandId; }
    public void setDemandId(Long demandId) { this.demandId = demandId; }
    public Long getSkillShelfId() { return skillShelfId; }
    public void setSkillShelfId(Long skillShelfId) { this.skillShelfId = skillShelfId; }
    public Long getSkillTagId() { return skillTagId; }
    public void setSkillTagId(Long skillTagId) { this.skillTagId = skillTagId; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public Integer getPointAmount() { return pointAmount; }
    public void setPointAmount(Integer pointAmount) { this.pointAmount = pointAmount; }
    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }
    public String getVerifyCode() { return verifyCode; }
    public void setVerifyCode(String verifyCode) { this.verifyCode = verifyCode; }
    public LocalDateTime getVerifyCodeExpire() { return verifyCodeExpire; }
    public void setVerifyCodeExpire(LocalDateTime verifyCodeExpire) { this.verifyCodeExpire = verifyCodeExpire; }
    public Long getTimeSlotId() { return timeSlotId; }
    public void setTimeSlotId(Long timeSlotId) { this.timeSlotId = timeSlotId; }
    public LocalDateTime getServiceStartTime() { return serviceStartTime; }
    public void setServiceStartTime(LocalDateTime serviceStartTime) { this.serviceStartTime = serviceStartTime; }
    public LocalDateTime getServiceEndTime() { return serviceEndTime; }
    public void setServiceEndTime(LocalDateTime serviceEndTime) { this.serviceEndTime = serviceEndTime; }
    public LocalDateTime getCompleteTime() { return completeTime; }
    public void setCompleteTime(LocalDateTime completeTime) { this.completeTime = completeTime; }
    public LocalDateTime getCancelTime() { return cancelTime; }
    public void setCancelTime(LocalDateTime cancelTime) { this.cancelTime = cancelTime; }
    public String getCancelReason() { return cancelReason; }
    public void setCancelReason(String cancelReason) { this.cancelReason = cancelReason; }
    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }
}
