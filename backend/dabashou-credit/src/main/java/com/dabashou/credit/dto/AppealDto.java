package com.dabashou.credit.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class AppealDto {

    @NotNull(message = "违规记录ID不能为空")
    private Long violationId;

    @NotBlank(message = "申诉理由不能为空")
    private String reason;

    private Long evidenceFileId;

    public Long getViolationId() { return violationId; }
    public void setViolationId(Long violationId) { this.violationId = violationId; }
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
    public Long getEvidenceFileId() { return evidenceFileId; }
    public void setEvidenceFileId(Long evidenceFileId) { this.evidenceFileId = evidenceFileId; }
}
