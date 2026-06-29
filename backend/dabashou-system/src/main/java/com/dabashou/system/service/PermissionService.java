package com.dabashou.system.service;

import com.dabashou.system.vo.PermissionVo;

import java.util.List;

public interface PermissionService {

    List<PermissionVo> getPermissionTree();
}
