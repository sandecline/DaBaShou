package com.dabashou.system.api;

import com.dabashou.system.service.RoleService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SystemApiImpl implements SystemApi {

    private final RoleService roleService;

    public SystemApiImpl(RoleService roleService) {
        this.roleService = roleService;
    }

    @Override
    public List<String> getRoleCodesByUserId(Long userId) {
        return roleService.getRoleCodesByUserId(userId);
    }
}
