package com.dabashou.user.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.dabashou.user.domain.User;
import com.dabashou.user.dto.*;
import com.dabashou.user.vo.LoginVo;
import com.dabashou.user.vo.UserProfileVo;
import com.dabashou.user.vo.TrustScoreOverviewVo;

public interface UserService extends IService<User> {

    void register(RegisterDto dto);

    LoginVo login(LoginDto dto);

    LoginVo smsLogin(SmsLoginDto dto);

    void sendSmsCode(String phone);

    LoginVo refreshToken(String refreshToken);

    void logout(Long userId, String token);

    UserProfileVo getProfile(Long userId);

    void updateProfile(Long userId, UpdateProfileDto dto);

    void updatePassword(Long userId, UpdatePasswordDto dto);

    void updateLocation(Long userId, UpdateLocationDto dto);

    TrustScoreOverviewVo getTrustScore(Long userId);

    void resetPassword(Long userId);
}
