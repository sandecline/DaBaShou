package com.dabashou.system.controller;

import com.dabashou.common.core.AjaxResult;
import com.dabashou.common.core.PageResult;
import com.dabashou.system.service.ConfigService;
import com.dabashou.system.service.LogService;
import com.dabashou.system.vo.ConfigVo;
import com.dabashou.system.vo.LogVo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

@Tag(name = "系统管理", description = "日志、配置")
@RestController
@RequestMapping("/api/admin/v1/system")
public class SystemController {

    private final LogService logService;
    private final ConfigService configService;

    public SystemController(LogService logService, ConfigService configService) {
        this.logService = logService;
        this.configService = configService;
    }

    @Operation(summary = "日志查询")
    @GetMapping("/logs")
    public AjaxResult<PageResult<LogVo>> getLogs(
            @Parameter(description = "操作人ID") @RequestParam(required = false) Long operatorId,
            @Parameter(description = "操作类型") @RequestParam(required = false) String operationType,
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") int pageNum,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "10") int pageSize) {
        return AjaxResult.ok(logService.getLogs(operatorId, operationType, pageNum, pageSize));
    }

    @Operation(summary = "配置列表")
    @GetMapping("/configs")
    public AjaxResult<PageResult<ConfigVo>> getConfigs(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") int pageNum,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "10") int pageSize) {
        return AjaxResult.ok(configService.getConfigs(pageNum, pageSize));
    }

    @Operation(summary = "更新配置")
    @PutMapping("/configs/{key}")
    public AjaxResult<Void> updateConfig(@PathVariable String key, @RequestBody java.util.Map<String, String> body) {
        configService.updateConfig(key, body.get("configValue"));
        return AjaxResult.ok();
    }
}
