package com.dabashou.user.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.dabashou.user.domain.UserCampusAuth;
import com.dabashou.user.dto.CampusAuthDto;
import com.dabashou.user.vo.CampusAuthVo;

public interface UserCampusAuthService extends IService<UserCampusAuth> {

    void applyCampusAuth(Long userId, CampusAuthDto dto);

    CampusAuthVo getCampusAuth(Long userId);

    void reviewCampusAuth(Long authId, Long reviewerId, boolean approved, String remark);
}
