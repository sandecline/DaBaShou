package com.dabashou.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.dabashou.common.core.PageResult;
import com.dabashou.common.enums.ErrorCode;
import com.dabashou.common.exception.BusinessException;
import com.dabashou.system.domain.SysConfig;
import com.dabashou.system.mapper.SysConfigMapper;
import com.dabashou.system.service.ConfigService;
import com.dabashou.system.vo.ConfigVo;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ConfigServiceImpl implements ConfigService {

    private final SysConfigMapper configMapper;

    public ConfigServiceImpl(SysConfigMapper configMapper) {
        this.configMapper = configMapper;
    }

    @Override
    public PageResult<ConfigVo> getConfigs(int pageNum, int pageSize) {
        LambdaQueryWrapper<SysConfig> qw = new LambdaQueryWrapper<>();
        qw.orderByDesc(SysConfig::getCreateTime);
        List<SysConfig> list = configMapper.selectList(qw.last("LIMIT " + pageSize + " OFFSET " + (pageNum - 1) * pageSize));
        long total = configMapper.selectCount(null);
        return PageResult.of(total, list.stream().map(this::toVo).collect(Collectors.toList()), pageNum, pageSize);
    }

    @Override
    public void updateConfig(String key, String value) {
        LambdaUpdateWrapper<SysConfig> uw = new LambdaUpdateWrapper<>();
        uw.eq(SysConfig::getConfigKey, key).set(SysConfig::getConfigValue, value);
        configMapper.update(null, uw);
    }

    private ConfigVo toVo(SysConfig c) {
        ConfigVo vo = new ConfigVo();
        vo.setId(c.getId());
        vo.setConfigKey(c.getConfigKey());
        vo.setConfigValue(c.getConfigValue());
        vo.setConfigName(c.getConfigName());
        vo.setDescription(c.getDescription());
        vo.setCreateTime(c.getCreateTime());
        return vo;
    }
}
