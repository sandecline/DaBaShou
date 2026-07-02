package com.dabashou.admin.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.Map;

public final class AdminDto {

    private AdminDto() {
    }

    public static class UserStatusDto {
        @NotNull(message = "状态不能为空")
        private Integer status;

        public Integer getStatus() { return status; }
        public void setStatus(Integer status) { this.status = status; }
    }

    public static class ArbitrateRequest {
        @NotBlank(message = "仲裁结果不能为空")
        private String result;
        private String reason;
        private Integer refundAmount;

        public String getResult() { return result; }
        public void setResult(String result) { this.result = result; }
        public String getReason() { return reason; }
        public void setReason(String reason) { this.reason = reason; }
        public Integer getRefundAmount() { return refundAmount; }
        public void setRefundAmount(Integer refundAmount) { this.refundAmount = refundAmount; }
    }

    public static class ViolationHandleRequest {
        @NotBlank(message = "处理结果不能为空")
        private String result;

        public String getResult() { return result; }
        public void setResult(String result) { this.result = result; }
    }

    public static class AppealHandleRequest {
        @NotNull(message = "审核结果不能为空")
        private Boolean approved;
        private String reason;

        public Boolean getApproved() { return approved; }
        public void setApproved(Boolean approved) { this.approved = approved; }
        public String getReason() { return reason; }
        public void setReason(String reason) { this.reason = reason; }
    }

    public static class CampusAuthReviewRequest {
        @NotNull(message = "审核结果不能为空")
        private Boolean approved;
        private String reason;

        public Boolean getApproved() { return approved; }
        public void setApproved(Boolean approved) { this.approved = approved; }
        public String getReason() { return reason; }
        public void setReason(String reason) { this.reason = reason; }
    }

    public static class ConfigUpdateRequest {
        private Map<String, Object> config;

        public Map<String, Object> getConfig() { return config; }
        public void setConfig(Map<String, Object> config) { this.config = config; }
    }
}
