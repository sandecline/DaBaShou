package com.dabashou.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.dabashou.user.domain.User;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserMapper extends BaseMapper<User> {
}
