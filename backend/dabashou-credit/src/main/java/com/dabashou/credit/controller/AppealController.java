package com.dabashou.credit.controller;

import com.dabashou.common.core.AjaxResult;
import com.dabashou.common.core.PageResult;
import com.dabashou.common.utils.SecurityUtil;
import com.dabashou.credit.dto.AppealDto;
import com.dabashou.credit.service.AppealService;
import com.dabashou.credit.vo.AppealVo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

/**
 * 申诉控制器
 */
@Tag(name = "申诉管理", description = "违规申诉的提交与查询")
@RestController
@RequestMapping("/api/v1/appeals")
public class AppealController {

    private final AppealService appealService;

    public AppealController(AppealService appealService) {
        this.appealService = appealService;
    }

    @Operation(summary = "提交申诉")
    @PostMapping
    public AjaxResult<Long> submit(@Valid @RequestBody AppealDto dto) {
        Long userId = SecurityUtil.requireCurrentUserId();
        return AjaxResult.ok(appealService.submit(userId, dto));
    }

    @Operation(summary = "我的申诉记录")
    @GetMapping("/mine")
    public AjaxResult<PageResult<AppealVo>> listMine(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") int pageNum,
            @Parameter(description = "每页大小") @RequestParam(defaultValue = "10") int pageSize) {
        Long userId = SecurityUtil.requireCurrentUserId();
        return AjaxResult.ok(appealService.listMine(userId, pageNum, pageSize));
    }
}
