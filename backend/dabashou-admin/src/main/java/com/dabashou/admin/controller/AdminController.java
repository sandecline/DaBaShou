package com.dabashou.admin.controller;

import com.dabashou.admin.dto.AdminDto;
import com.dabashou.admin.service.AdminService;
import com.dabashou.common.core.AjaxResult;
import com.dabashou.common.core.PageResult;
import com.dabashou.common.utils.SecurityUtil;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/admin/v1")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    @GetMapping("/users")
    public AjaxResult<PageResult<Map<String, Object>>> users(@RequestParam(required = false) String keyword,
                                                             @RequestParam(required = false) Integer status,
                                                             @RequestParam(defaultValue = "1") int pageNum,
                                                             @RequestParam(defaultValue = "10") int pageSize) {
        return AjaxResult.ok(adminService.listUsers(keyword, status, pageNum, pageSize));
    }

    @GetMapping("/users/{id}")
    public AjaxResult<Map<String, Object>> userDetail(@PathVariable Long id) {
        return AjaxResult.ok(adminService.getUserDetail(id));
    }

    @PutMapping("/users/{id}/status")
    public AjaxResult<Void> updateUserStatus(@PathVariable Long id, @Valid @RequestBody AdminDto.UserStatusDto dto) {
        adminService.updateUserStatus(SecurityUtil.requireCurrentUserId(), id, dto.getStatus());
        return AjaxResult.ok();
    }

    @PostMapping("/users/{id}/reset-password")
    public AjaxResult<Map<String, Object>> resetPassword(@PathVariable Long id) {
        return AjaxResult.ok(adminService.resetPassword(SecurityUtil.requireCurrentUserId(), id));
    }

    @GetMapping("/orders")
    public AjaxResult<PageResult<Map<String, Object>>> orders(@RequestParam(required = false) String keyword,
                                                              @RequestParam(required = false) Integer status,
                                                              @RequestParam(defaultValue = "1") int pageNum,
                                                              @RequestParam(defaultValue = "10") int pageSize) {
        return AjaxResult.ok(adminService.listOrders(keyword, status, pageNum, pageSize));
    }

    @GetMapping("/orders/{id}")
    public AjaxResult<Map<String, Object>> orderDetail(@PathVariable Long id) {
        return AjaxResult.ok(adminService.getOrderDetail(id));
    }

    @PostMapping("/orders/{id}/arbitrate")
    public AjaxResult<Void> arbitrate(@PathVariable Long id, @Valid @RequestBody AdminDto.ArbitrateRequest request) {
        adminService.arbitrateOrder(SecurityUtil.requireCurrentUserId(), id, request);
        return AjaxResult.ok();
    }

    @GetMapping("/violations")
    public AjaxResult<PageResult<Map<String, Object>>> violations(@RequestParam(defaultValue = "1") int pageNum,
                                                                  @RequestParam(defaultValue = "10") int pageSize) {
        return AjaxResult.ok(adminService.listViolations(pageNum, pageSize));
    }

    @PostMapping("/violations/{id}")
    public AjaxResult<Void> handleViolation(@PathVariable Long id, @Valid @RequestBody AdminDto.ViolationHandleRequest request) {
        adminService.handleViolation(SecurityUtil.requireCurrentUserId(), id, request);
        return AjaxResult.ok();
    }

    @GetMapping("/appeals")
    public AjaxResult<PageResult<Map<String, Object>>> appeals(@RequestParam(defaultValue = "1") int pageNum,
                                                               @RequestParam(defaultValue = "10") int pageSize) {
        return AjaxResult.ok(adminService.listAppeals(pageNum, pageSize));
    }

    @PostMapping("/appeals/{id}")
    public AjaxResult<Void> handleAppeal(@PathVariable Long id, @Valid @RequestBody AdminDto.AppealHandleRequest request) {
        adminService.handleAppeal(SecurityUtil.requireCurrentUserId(), id, request);
        return AjaxResult.ok();
    }

    @GetMapping("/reviews")
    public AjaxResult<PageResult<Map<String, Object>>> reviews(@RequestParam(defaultValue = "1") int pageNum,
                                                               @RequestParam(defaultValue = "10") int pageSize) {
        return AjaxResult.ok(adminService.listReviews(pageNum, pageSize));
    }

    @DeleteMapping("/reviews/{id}")
    public AjaxResult<Void> deleteReview(@PathVariable Long id) {
        adminService.hideReview(SecurityUtil.requireCurrentUserId(), id);
        return AjaxResult.ok();
    }

    @GetMapping("/campus-auths")
    public AjaxResult<PageResult<Map<String, Object>>> campusAuths(@RequestParam(required = false) Integer status,
                                                                   @RequestParam(defaultValue = "1") int pageNum,
                                                                   @RequestParam(defaultValue = "10") int pageSize) {
        return AjaxResult.ok(adminService.listCampusAuths(status, pageNum, pageSize));
    }

    @PostMapping("/campus-auths/{id}")
    public AjaxResult<Void> reviewCampusAuth(@PathVariable Long id, @Valid @RequestBody AdminDto.CampusAuthReviewRequest request) {
        adminService.reviewCampusAuth(SecurityUtil.requireCurrentUserId(), id, request);
        return AjaxResult.ok();
    }

    @GetMapping("/config")
    public AjaxResult<Map<String, Object>> config() {
        return AjaxResult.ok(adminService.getConfig());
    }

    @PutMapping("/config")
    public AjaxResult<Void> updateConfig(@RequestBody Map<String, Object> config) {
        adminService.updateConfig(SecurityUtil.requireCurrentUserId(), config);
        return AjaxResult.ok();
    }
}
