package com.dabashou.admin.controller;

import com.dabashou.common.core.AjaxResult;
import com.dabashou.common.core.PageResult;
import com.dabashou.credit.service.CreditService;
import com.dabashou.credit.vo.AppealVo;
import com.dabashou.credit.vo.ViolationVo;
import com.dabashou.order.api.OrderApi;
import com.dabashou.order.domain.Order;
import com.dabashou.order.mapper.OrderMapper;
import com.dabashou.user.api.UserApi;
import com.dabashou.user.domain.User;
import com.dabashou.user.mapper.UserMapper;
import com.dabashou.system.service.RoleService;
import com.dabashou.system.service.ConfigService;
import com.dabashou.system.service.LogService;
import com.dabashou.system.vo.ConfigVo;
import com.dabashou.system.vo.LogVo;
import com.dabashou.system.vo.RoleVo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Tag(name = "管理后台", description = "管理员专用接口")
@RestController
@RequestMapping("/api/admin/v1")
public class AdminController {

    private final UserMapper userMapper;
    private final OrderMapper orderMapper;
    private final CreditService creditService;
    private final RoleService roleService;
    private final LogService logService;
    private final ConfigService configService;

    public AdminController(UserMapper userMapper, OrderMapper orderMapper,
                           CreditService creditService, RoleService roleService,
                           LogService logService, ConfigService configService) {
        this.userMapper = userMapper;
        this.orderMapper = orderMapper;
        this.creditService = creditService;
        this.roleService = roleService;
        this.logService = logService;
        this.configService = configService;
    }

    @Operation(summary = "用户列表")
    @GetMapping("/users")
    public AjaxResult<PageResult<User>> getUsers(
            @Parameter(description = "关键词") @RequestParam(required = false) String keyword,
            @Parameter(description = "状态") @RequestParam(required = false) Integer status,
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") int pageNum,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "10") int pageSize) {
        com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<User> qw =
            new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<>();
        if (keyword != null) qw.and(w -> w.like(User::getNickname, keyword).or().like(User::getPhone, keyword));
        if (status != null) qw.eq(User::getStatus, status);
        qw.orderByDesc(User::getCreateTime);
        List<User> list = userMapper.selectList(qw.last("LIMIT " + pageSize + " OFFSET " + (pageNum - 1) * pageSize));
        long total = userMapper.selectCount(qw);
        return AjaxResult.ok(PageResult.of(total, list, pageNum, pageSize));
    }

    @Operation(summary = "用户详情")
    @GetMapping("/users/{id}")
    public AjaxResult<User> getUserDetail(@PathVariable Long id) {
        return AjaxResult.ok(userMapper.selectById(id));
    }

    @Operation(summary = "启用/禁用用户")
    @PutMapping("/users/{id}/status")
    public AjaxResult<Void> toggleUserStatus(@PathVariable Long id, @RequestBody Map<String, Integer> body) {
        User user = userMapper.selectById(id);
        if (user == null) return AjaxResult.fail(404, "用户不存在");
        user.setStatus(body.get("status"));
        userMapper.updateById(user);
        return AjaxResult.ok();
    }

    @Operation(summary = "订单列表")
    @GetMapping("/orders")
    public AjaxResult<PageResult<Order>> getOrders(
            @Parameter(description = "状态") @RequestParam(required = false) Integer status,
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") int pageNum,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "10") int pageSize) {
        com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Order> qw =
            new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<>();
        if (status != null) qw.eq(Order::getStatus, status);
        qw.orderByDesc(Order::getCreateTime);
        List<Order> list = orderMapper.selectList(qw.last("LIMIT " + pageSize + " OFFSET " + (pageNum - 1) * pageSize));
        long total = orderMapper.selectCount(qw);
        return AjaxResult.ok(PageResult.of(total, list, pageNum, pageSize));
    }

    @Operation(summary = "订单详情")
    @GetMapping("/orders/{id}")
    public AjaxResult<Order> getOrderDetail(@PathVariable Long id) {
        return AjaxResult.ok(orderMapper.selectById(id));
    }

    @Operation(summary = "违规列表")
    @GetMapping("/violations")
    public AjaxResult<PageResult<ViolationVo>> getViolations(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") int pageNum,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "10") int pageSize) {
        return AjaxResult.ok(creditService.getMyViolations(null, pageNum, pageSize));
    }

    @Operation(summary = "申诉列表")
    @GetMapping("/appeals")
    public AjaxResult<PageResult<AppealVo>> getAppeals(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") int pageNum,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "10") int pageSize) {
        return AjaxResult.ok(creditService.getMyAppeals(null, pageNum, pageSize));
    }

    @Operation(summary = "角色列表")
    @GetMapping("/roles")
    public AjaxResult<List<RoleVo>> getRoles() {
        return AjaxResult.ok(roleService.getRoles());
    }

    @Operation(summary = "日志查询")
    @GetMapping("/logs")
    public AjaxResult<PageResult<LogVo>> getLogs(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") int pageNum,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "10") int pageSize) {
        return AjaxResult.ok(logService.getLogs(null, null, pageNum, pageSize));
    }

    @Operation(summary = "配置列表")
    @GetMapping("/configs")
    public AjaxResult<PageResult<ConfigVo>> getConfigs(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") int pageNum,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "10") int pageSize) {
        return AjaxResult.ok(configService.getConfigs(pageNum, pageSize));
    }
}
