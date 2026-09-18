package com.dabashou.credit.controller;

import com.dabashou.common.core.AjaxResult;
import com.dabashou.common.enums.ErrorCode;
import com.dabashou.common.exception.BusinessException;
import com.dabashou.common.utils.SecurityUtil;
import com.dabashou.credit.service.ReviewService;
import com.dabashou.credit.vo.ReviewVo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

@Tag(name = "评价管理", description = "订单评价查询")
@RestController
@RequestMapping("/api/v1/orders")
public class OrderReviewController {

    private final ReviewService reviewService;

    public OrderReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    @Operation(summary = "查询订单评价")
    @GetMapping("/{orderId}/review")
    public AjaxResult<ReviewVo> getOrderReview(@PathVariable Long orderId) {
        Long userId = SecurityUtil.requireCurrentUserId();
        ReviewVo review = reviewService.getOrderReview(userId, orderId);
        if (review == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "该订单暂无评价");
        }
        return AjaxResult.ok(review);
    }
}
