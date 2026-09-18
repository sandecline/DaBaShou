package com.dabashou.order.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/**
 * 核销订单 — 双方核销码驱动
 *
 * phase=start: 开始核销（1→3），双方输入对方的开始核销码
 * phase=complete: 完成确认（3→5），双方输入对方的完成确认码
 */
public class VerifyDto {

    @NotBlank(message = "核销码不能为空")
    private String code;

    @NotBlank(message = "核销阶段不能为空")
    @Pattern(regexp = "^(start|complete)$", message = "核销阶段必须是 start 或 complete")
    private String phase;

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public String getPhase() { return phase; }
    public void setPhase(String phase) { this.phase = phase; }
}
