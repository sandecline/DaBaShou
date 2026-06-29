package com.dabashou.credit.controller;

import com.dabashou.common.core.AjaxResult;
import com.dabashou.common.core.PageResult;
import com.dabashou.common.utils.SecurityUtil;
import com.dabashou.credit.dto.ReviewDto;
import com.dabashou.credit.service.ReviewService;
import com.dabashou.credit.vo.ReviewVo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

/**
 * 评价控制器
 */
@Tag(name = "评价管理", description = "订单评价的提交与查询")
@RestController
@RequestMapping("/api/v1/reviews")
public class ReviewController {

    private final ReviewService reviewService;

    public ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    @Operation(summary = "提交评价")
    @PostMapping
    public AjaxResult<Long> submit(@Valid @RequestBody ReviewDto dto) {
        Long userId = SecurityUtil.requireCurrentUserId();
        return AjaxResult.ok(reviewService.submit(userId, dto));
    }

    @Operation(summary = "我发出的评价")
    @GetMapping("/mine")
    public AjaxResult<PageResult<ReviewVo>> listMine(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") int pageNum,
            @Parameter(description = "每页大小") @RequestParam(defaultValue = "10") int pageSize) {
        Long userId = SecurityUtil.requireCurrentUserId();
        return AjaxResult.ok(reviewService.listMine(userId, pageNum, pageSize));
    }

    @Operation(summary = "我收到的评价")
    @GetMapping("/received")
    public AjaxResult<PageResult<ReviewVo>> listReceived(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") int pageNum,
            @Parameter(description = "每页大小") @RequestParam(defaultValue = "10") int pageSize) {
        Long userId = SecurityUtil.requireCurrentUserId();
        return AjaxResult.ok(reviewService.listReceived(userId, pageNum, pageSize));
    }
}
