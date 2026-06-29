package com.dabashou.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.dabashou.common.enums.ErrorCode;
import com.dabashou.common.exception.BusinessException;
import com.dabashou.system.domain.SysRole;
import com.dabashou.system.domain.SysRolePermission;
import com.dabashou.system.domain.SysUserRole;
import com.dabashou.system.dto.RoleDto;
import com.dabashou.system.mapper.SysRoleMapper;
import com.dabashou.system.mapper.SysRolePermissionMapper;
import com.dabashou.system.mapper.SysUserRoleMapper;
import com.dabashou.system.service.RoleService;
import com.dabashou.system.vo.RoleVo;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class RoleServiceImpl implements RoleService {

    private final SysRoleMapper roleMapper;
    private final SysRolePermissionMapper rolePermissionMapper;
    private final SysUserRoleMapper userRoleMapper;

    public RoleServiceImpl(SysRoleMapper roleMapper, SysRolePermissionMapper rolePermissionMapper,
                           SysUserRoleMapper userRoleMapper) {
        this.roleMapper = roleMapper;
        this.rolePermissionMapper = rolePermissionMapper;
        this.userRoleMapper = userRoleMapper;
    }

    @Override
    public List<RoleVo> getRoles() {
        return roleMapper.selectList(null).stream().map(this::toVo).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public Long createRole(RoleDto dto) {
        LambdaQueryWrapper<SysRole> qw = new LambdaQueryWrapper<>();
        qw.eq(SysRole::getRoleCode, dto.getRoleCode());
        if (roleMapper.selectCount(qw) > 0) {
            throw new BusinessException(ErrorCode.CONFLICT, "角色编码已存在");
        }
        SysRole role = new SysRole();
        role.setRoleCode(dto.getRoleCode());
        role.setRoleName(dto.getRoleName());
        role.setDescription(dto.getDescription());
        role.setStatus(1);
        roleMapper.insert(role);
        return role.getId();
    }

    @Override
    @Transactional
    public void updateRole(Long id, RoleDto dto) {
        SysRole role = roleMapper.selectById(id);
        if (role == null) throw new BusinessException(ErrorCode.NOT_FOUND, "角色不存在");
        role.setRoleCode(dto.getRoleCode());
        role.setRoleName(dto.getRoleName());
        role.setDescription(dto.getDescription());
        roleMapper.updateById(role);
    }

    @Override
    @Transactional
    public void deleteRole(Long id) {
        roleMapper.deleteById(id);
        rolePermissionMapper.delete(new LambdaQueryWrapper<SysRolePermission>().eq(SysRolePermission::getRoleId, id));
    }

    @Override
    @Transactional
    public void assignPermissions(Long roleId, List<Long> permissionIds) {
        rolePermissionMapper.delete(new LambdaQueryWrapper<SysRolePermission>().eq(SysRolePermission::getRoleId, roleId));
        for (Long pid : permissionIds) {
            SysRolePermission rp = new SysRolePermission();
            rp.setRoleId(roleId);
            rp.setPermissionId(pid);
            rolePermissionMapper.insert(rp);
        }
    }

    @Override
    @Transactional
    public void assignRoles(Long userId, List<Long> roleIds) {
        userRoleMapper.delete(new LambdaQueryWrapper<SysUserRole>().eq(SysUserRole::getUserId, userId));
        for (Long rid : roleIds) {
            SysUserRole ur = new SysUserRole();
            ur.setUserId(userId);
            ur.setRoleId(rid);
            userRoleMapper.insert(ur);
        }
    }

    @Override
    public List<String> getRoleCodesByUserId(Long userId) {
        List<SysUserRole> userRoles = userRoleMapper.selectList(
            new LambdaQueryWrapper<SysUserRole>().eq(SysUserRole::getUserId, userId));
        List<Long> roleIds = userRoles.stream().map(SysUserRole::getRoleId).collect(Collectors.toList());
        if (roleIds.isEmpty()) return List.of();
        return roleMapper.selectBatchIds(roleIds).stream()
            .map(SysRole::getRoleCode).collect(Collectors.toList());
    }

    private RoleVo toVo(SysRole r) {
        RoleVo vo = new RoleVo();
        vo.setId(r.getId());
        vo.setRoleCode(r.getRoleCode());
        vo.setRoleName(r.getRoleName());
        vo.setDescription(r.getDescription());
        vo.setStatus(r.getStatus());
        vo.setCreateTime(r.getCreateTime());
        return vo;
    }
}
