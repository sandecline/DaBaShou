package com.dabashou.credit.controller;

import com.dabashou.common.core.AjaxResult;
import com.dabashou.common.core.PageResult;
import com.dabashou.common.utils.SecurityUtil;
import com.dabashou.credit.dto.AppealDto;
import com.dabashou.credit.dto.ReviewDto;
import com.dabashou.credit.dto.ViolationDto;
import com.dabashou.credit.service.CreditService;
import com.dabashou.credit.vo.AppealVo;
import com.dabashou.credit.vo.ReviewVo;
import com.dabashou.credit.vo.ViolationVo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@Tag(name = "信用评价", description = "评价、违规、申诉")
@RestController
@RequestMapping("/api/v1/credit")
public class CreditController {

    private final CreditService creditService;

    public CreditController(CreditService creditService) {
        this.creditService = creditService;
    }

    @Operation(summary = "提交评价")
    @PostMapping("/reviews")
    public AjaxResult<Long> submitReview(@Valid @RequestBody ReviewDto dto) {
        Long id = creditService.submitReview(SecurityUtil.requireCurrentUserId(), dto);
        return AjaxResult.ok(id);
    }

    @Operation(summary = "我发出的评价")
    @GetMapping("/reviews/mine")
    public AjaxResult<PageResult<ReviewVo>> getMyReviews(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") int pageNum,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "10") int pageSize) {
        return AjaxResult.ok(creditService.getMyReviews(SecurityUtil.requireCurrentUserId(), pageNum, pageSize));
    }

    @Operation(summary = "我收到的评价")
    @GetMapping("/reviews/received")
    public AjaxResult<PageResult<ReviewVo>> getReceivedReviews(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") int pageNum,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "10") int pageSize) {
        return AjaxResult.ok(creditService.getReceivedReviews(SecurityUtil.requireCurrentUserId(), pageNum, pageSize));
    }

    @Operation(summary = "订单评价")
    @GetMapping("/orders/{orderId}/review")
    public AjaxResult<ReviewVo> getOrderReview(@PathVariable Long orderId) {
        return AjaxResult.ok(creditService.getOrderReview(orderId));
    }

    @Operation(summary = "举报违规")
    @PostMapping("/violations")
    public AjaxResult<Long> reportViolation(@Valid @RequestBody ViolationDto dto) {
        Long id = creditService.reportViolation(SecurityUtil.requireCurrentUserId(), dto);
        return AjaxResult.ok(id);
    }

    @Operation(summary = "我的违规")
    @GetMapping("/violations/mine")
    public AjaxResult<PageResult<ViolationVo>> getMyViolations(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") int pageNum,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "10") int pageSize) {
        return AjaxResult.ok(creditService.getMyViolations(SecurityUtil.requireCurrentUserId(), pageNum, pageSize));
    }

    @Operation(summary = "提交申诉")
    @PostMapping("/appeals")
    public AjaxResult<Long> submitAppeal(@Valid @RequestBody AppealDto dto) {
        Long id = creditService.submitAppeal(SecurityUtil.requireCurrentUserId(), dto);
        return AjaxResult.ok(id);
    }

    @Operation(summary = "我的申诉")
    @GetMapping("/appeals/mine")
    public AjaxResult<PageResult<AppealVo>> getMyAppeals(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") int pageNum,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "10") int pageSize) {
        return AjaxResult.ok(creditService.getMyAppeals(SecurityUtil.requireCurrentUserId(), pageNum, pageSize));
    }
}
