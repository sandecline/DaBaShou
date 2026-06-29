package com.dabashou.system.service;

import com.dabashou.system.domain.SysRole;
import com.dabashou.system.dto.RoleDto;
import com.dabashou.system.vo.PermissionVo;
import com.dabashou.system.vo.RoleVo;

import java.util.List;

public interface RoleService {

    List<RoleVo> getRoles();

    Long createRole(RoleDto dto);

    void updateRole(Long id, RoleDto dto);

    void deleteRole(Long id);

    void assignPermissions(Long roleId, List<Long> permissionIds);

    void assignRoles(Long userId, List<Long> roleIds);

    List<String> getRoleCodesByUserId(Long userId);
}
