package com.dabashou.system.controller;

import com.dabashou.common.core.AjaxResult;
import com.dabashou.system.dto.RoleDto;
import com.dabashou.system.service.PermissionService;
import com.dabashou.system.service.RoleService;
import com.dabashou.system.vo.PermissionVo;
import com.dabashou.system.vo.RoleVo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Tag(name = "角色权限", description = "角色权限管理")
@RestController
@RequestMapping("/api/admin/v1/roles")
public class RoleController {

    private final RoleService roleService;
    private final PermissionService permissionService;

    public RoleController(RoleService roleService, PermissionService permissionService) {
        this.roleService = roleService;
        this.permissionService = permissionService;
    }

    @Operation(summary = "角色列表")
    @GetMapping
    public AjaxResult<List<RoleVo>> getRoles() {
        return AjaxResult.ok(roleService.getRoles());
    }

    @Operation(summary = "创建角色")
    @PostMapping
    public AjaxResult<Long> createRole(@Valid @RequestBody RoleDto dto) {
        return AjaxResult.ok(roleService.createRole(dto));
    }

    @Operation(summary = "更新角色")
    @PutMapping("/{id}")
    public AjaxResult<Void> updateRole(@PathVariable Long id, @Valid @RequestBody RoleDto dto) {
        roleService.updateRole(id, dto);
        return AjaxResult.ok();
    }

    @Operation(summary = "删除角色")
    @DeleteMapping("/{id}")
    public AjaxResult<Void> deleteRole(@PathVariable Long id) {
        roleService.deleteRole(id);
        return AjaxResult.ok();
    }

    @Operation(summary = "权限树")
    @GetMapping("/permissions/tree")
    public AjaxResult<List<PermissionVo>> getPermissionTree() {
        return AjaxResult.ok(permissionService.getPermissionTree());
    }

    @Operation(summary = "分配权限")
    @PostMapping("/{roleId}/permissions")
    public AjaxResult<Void> assignPermissions(@PathVariable Long roleId, @RequestBody Map<String, List<Long>> body) {
        roleService.assignPermissions(roleId, body.get("permissionIds"));
        return AjaxResult.ok();
    }

    @Operation(summary = "分配用户角色")
    @PostMapping("/users/{userId}/roles")
    public AjaxResult<Void> assignRoles(@PathVariable Long userId, @RequestBody Map<String, List<Long>> body) {
        roleService.assignRoles(userId, body.get("roleIds"));
        return AjaxResult.ok();
    }
}
