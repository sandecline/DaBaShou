package com.dabashou.system.service.impl;

import com.dabashou.system.domain.SysPermission;
import com.dabashou.system.mapper.SysPermissionMapper;
import com.dabashou.system.service.PermissionService;
import com.dabashou.system.vo.PermissionVo;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class PermissionServiceImpl implements PermissionService {

    private final SysPermissionMapper permissionMapper;

    public PermissionServiceImpl(SysPermissionMapper permissionMapper) {
        this.permissionMapper = permissionMapper;
    }

    @Override
    public List<PermissionVo> getPermissionTree() {
        List<SysPermission> all = permissionMapper.selectList(null);
        Map<Long, List<SysPermission>> grouped = all.stream()
            .collect(Collectors.groupingBy(SysPermission::getParentId));
        return buildTree(grouped, 0L);
    }

    private List<PermissionVo> buildTree(Map<Long, List<SysPermission>> grouped, Long parentId) {
        List<SysPermission> children = grouped.getOrDefault(parentId, List.of());
        return children.stream().map(p -> {
            PermissionVo vo = new PermissionVo();
            vo.setId(p.getId());
            vo.setParentId(p.getParentId());
            vo.setPermissionCode(p.getPermissionCode());
            vo.setPermissionName(p.getPermissionName());
            vo.setType(p.getType());
            vo.setPath(p.getPath());
            vo.setIcon(p.getIcon());
            vo.setSortOrder(p.getSortOrder());
            vo.setStatus(p.getStatus());
            vo.setChildren(buildTree(grouped, p.getId()));
            return vo;
        }).collect(Collectors.toList());
    }
}
