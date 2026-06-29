package com.dabashou.credit.controller;

import com.dabashou.common.core.AjaxResult;
import com.dabashou.common.core.PageResult;
import com.dabashou.common.utils.SecurityUtil;
import com.dabashou.credit.dto.ViolationDto;
import com.dabashou.credit.service.ViolationService;
import com.dabashou.credit.vo.ViolationVo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

/**
 * 违规举报控制器
 */
@Tag(name = "违规管理", description = "违规举报的提交与查询")
@RestController
@RequestMapping("/api/v1/violations")
public class ViolationController {

    private final ViolationService violationService;

    public ViolationController(ViolationService violationService) {
        this.violationService = violationService;
    }

    @Operation(summary = "举报违规")
    @PostMapping
    public AjaxResult<Long> report(@Valid @RequestBody ViolationDto dto) {
        Long userId = SecurityUtil.requireCurrentUserId();
        return AjaxResult.ok(violationService.report(userId, dto));
    }

    @Operation(summary = "我的违规记录")
    @GetMapping("/mine")
    public AjaxResult<PageResult<ViolationVo>> listMine(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") int pageNum,
            @Parameter(description = "每页大小") @RequestParam(defaultValue = "10") int pageSize) {
        Long userId = SecurityUtil.requireCurrentUserId();
        return AjaxResult.ok(violationService.listMine(userId, pageNum, pageSize));
    }
}
