package com.dabashou.order.controller;

import com.dabashou.common.core.AjaxResult;
import com.dabashou.common.core.PageResult;
import com.dabashou.common.utils.SecurityUtil;
import com.dabashou.order.dto.*;
import com.dabashou.order.service.OrderService;
import com.dabashou.order.vo.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

/**
 * 订单控制器
 */
@Tag(name = "订单管理", description = "订单创建、状态流转、支付、核销等")
@RestController
@RequestMapping("/api/v1/order")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @Operation(summary = "从货架创建订单")
    @PostMapping("/from-shelf")
    public AjaxResult<Long> createFromShelf(@Valid @RequestBody CreateOrderDto dto) {
        Long userId = SecurityUtil.requireCurrentUserId();
        return AjaxResult.ok(orderService.createOrderFromShelf(userId, dto));
    }

    @Operation(summary = "从需求创建订单(揭榜接单)")
    @PostMapping("/from-demand")
    public AjaxResult<Long> createFromDemand(@Valid @RequestBody CreateOrderFromDemandDto dto) {
        Long userId = SecurityUtil.requireCurrentUserId();
        return AjaxResult.ok(orderService.createOrderFromDemand(userId, dto));
    }

    @Operation(summary = "订单列表")
    @GetMapping
    public AjaxResult<PageResult<OrderItemVo>> listOrders(
            @Parameter(description = "角色: buyer/seller") @RequestParam(required = false) String role,
            @Parameter(description = "状态码") @RequestParam(required = false) Integer status,
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") int pageNum,
            @Parameter(description = "每页大小") @RequestParam(defaultValue = "10") int pageSize) {
        Long userId = SecurityUtil.requireCurrentUserId();
        return AjaxResult.ok(orderService.listOrders(userId, role, status, pageNum, pageSize));
    }

    @Operation(summary = "订单详情")
    @GetMapping("/{orderId}")
    public AjaxResult<OrderDetailVo> getOrderDetail(@PathVariable Long orderId) {
        Long userId = SecurityUtil.requireCurrentUserId();
        return AjaxResult.ok(orderService.getOrderDetail(userId, orderId));
    }

    @Operation(summary = "查询订单状态")
    @GetMapping("/{orderId}/status")
    public AjaxResult<OrderStatusVo> getOrderStatus(@PathVariable Long orderId) {
        Long userId = SecurityUtil.requireCurrentUserId();
        return AjaxResult.ok(orderService.getOrderStatus(userId, orderId));
    }

    @Operation(summary = "支付订单(1→2)")
    @PostMapping("/{orderId}/pay")
    public AjaxResult<PayResultVo> payOrder(
            @PathVariable Long orderId,
            @RequestHeader("X-Idempotent-Token") String idempotentToken) {
        Long userId = SecurityUtil.requireCurrentUserId();
        return AjaxResult.ok(orderService.payOrder(userId, orderId, idempotentToken));
    }

    @Operation(summary = "取消订单(1→0)")
    @PostMapping("/{orderId}/cancel")
    public AjaxResult<Void> cancelOrder(@PathVariable Long orderId, @Valid @RequestBody CancelDto dto) {
        Long userId = SecurityUtil.requireCurrentUserId();
        orderService.cancelOrder(userId, orderId, dto);
        return AjaxResult.ok();
    }

    @Operation(summary = "开始服务(2→3)")
    @PostMapping("/{orderId}/start")
    public AjaxResult<Void> startService(@PathVariable Long orderId) {
        Long userId = SecurityUtil.requireCurrentUserId();
        orderService.startService(userId, orderId);
        return AjaxResult.ok();
    }

    @Operation(summary = "获取核销码")
    @GetMapping("/{orderId}/verify-code")
    public AjaxResult<VerifyCodeVo> getVerifyCode(@PathVariable Long orderId) {
        Long userId = SecurityUtil.requireCurrentUserId();
        return AjaxResult.ok(orderService.getVerifyCode(userId, orderId));
    }

    @Operation(summary = "刷新核销码")
    @PutMapping("/{orderId}/verify-code")
    public AjaxResult<VerifyCodeVo> refreshVerifyCode(@PathVariable Long orderId) {
        Long userId = SecurityUtil.requireCurrentUserId();
        return AjaxResult.ok(orderService.refreshVerifyCode(userId, orderId));
    }

    @Operation(summary = "核销订单(3→4)")
    @PostMapping("/{orderId}/verify")
    public AjaxResult<Void> verifyOrder(@PathVariable Long orderId, @Valid @RequestBody VerifyDto dto) {
        Long userId = SecurityUtil.requireCurrentUserId();
        orderService.verifyOrder(userId, orderId, dto);
        return AjaxResult.ok();
    }

    @Operation(summary = "买家确认完成(4→5)")
    @PostMapping("/{orderId}/confirm")
    public AjaxResult<Void> confirmOrder(@PathVariable Long orderId) {
        Long userId = SecurityUtil.requireCurrentUserId();
        orderService.confirmOrder(userId, orderId);
        return AjaxResult.ok();
    }

    @Operation(summary = "发起争议(4→7)")
    @PostMapping("/{orderId}/dispute")
    public AjaxResult<Void> disputeOrder(@PathVariable Long orderId, @Valid @RequestBody DisputeDto dto) {
        Long userId = SecurityUtil.requireCurrentUserId();
        orderService.disputeOrder(userId, orderId, dto);
        return AjaxResult.ok();
    }

    @Operation(summary = "仲裁订单(7→5|6) - 仅管理员")
    @org.springframework.security.access.prepost.PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/{orderId}/arbitrate")
    public AjaxResult<Void> arbitrateOrder(@PathVariable Long orderId, @Valid @RequestBody ArbitrateDto dto) {
        orderService.arbitrateOrder(orderId, dto);
        return AjaxResult.ok();
    }

    @Operation(summary = "申请退款")
    @PostMapping("/{orderId}/refund")
    public AjaxResult<Void> refundOrder(@PathVariable Long orderId, @Valid @RequestBody RefundDto dto) {
        Long userId = SecurityUtil.requireCurrentUserId();
        orderService.refundOrder(userId, orderId, dto);
        return AjaxResult.ok();
    }
}
