package com.dabashou.system.service;

import com.dabashou.common.core.PageResult;
import com.dabashou.system.vo.ConfigVo;

public interface ConfigService {

    PageResult<ConfigVo> getConfigs(int pageNum, int pageSize);

    void updateConfig(String key, String value);
}
