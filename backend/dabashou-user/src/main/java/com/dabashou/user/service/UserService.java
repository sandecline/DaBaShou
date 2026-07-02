package com.dabashou.user.service;

import com.dabashou.user.dto.*;
import com.dabashou.user.vo.*;

/**
 * 用户服务接口
 */
public interface UserService {

    /**
     * 注册
     */
    void register(RegisterDto dto);

    /**
     * 密码登录
     */
    LoginVo login(LoginDto dto);

    /**
     * 发送短信验证码（stub）
     */
    void sendSmsCode(String phone);

    /**
     * 短信验证码登录（stub）
     */
    LoginVo smsLogin(SmsLoginDto dto);

    /**
     * 刷新token
     */
    LoginVo refreshToken(String refreshToken);

    /**
     * 获取个人信息
     */
    UserProfileVo getProfile(Long userId);

    /**
     * 获取公开脱敏用户资料
     */
    PublicUserVo getPublicUser(Long userId);

    /**
     * 更新个人信息
     */
    void updateProfile(Long userId, UpdateProfileDto dto);

    /**
     * 修改密码
     */
    void changePassword(Long userId, ChangePasswordDto dto);

    /**
     * 更新位置
     */
    void updateLocation(Long userId, UpdateLocationDto dto);

    /**
     * 提交校园认证
     */
    void submitCampusAuth(Long userId, CampusAuthDto dto);

    /**
     * 获取校园认证信息
     */
    CampusAuthVo getCampusAuth(Long userId);

    /**
     * 获取信任分及日志
     */
    TrustScoreVo getTrustScore(Long userId);
}
