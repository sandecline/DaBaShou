package com.dabashou.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.dabashou.common.enums.PointTransType;
import com.dabashou.common.enums.TrustLevel;
import com.dabashou.common.exception.BusinessException;
import com.dabashou.common.enums.ErrorCode;
import com.dabashou.common.utils.JwtUtil;
import com.dabashou.user.domain.User;
import com.dabashou.user.dto.*;
import com.dabashou.user.mapper.UserMapper;
import com.dabashou.user.service.UserService;
import com.dabashou.user.vo.LoginVo;
import com.dabashou.user.vo.UserProfileVo;
import com.dabashou.system.api.SystemApi;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    private final StringRedisTemplate redisTemplate;
    private final SystemApi systemApi;

    @Value("${dabashou.jwt.secret}")
    private String jwtSecret;

    @Value("${dabashou.jwt.expiration}")
    private Long jwtExpiration;

    @Value("${dabashou.jwt.refresh-expiration}")
    private Long refreshExpiration;

    public UserServiceImpl(StringRedisTemplate redisTemplate, SystemApi systemApi) {
        this.redisTemplate = redisTemplate;
        this.systemApi = systemApi;
    }

    @Override
    @Transactional
    public void register(RegisterDto dto) {
        boolean exists = lambdaQuery()
                .eq(User::getUsername, dto.getUsername())
                .exists();
        if (exists) {
            throw new BusinessException(ErrorCode.CONFLICT, "用户名已存在");
        }
        if (dto.getPhone() != null) {
            boolean phoneExists = lambdaQuery()
                    .eq(User::getPhone, dto.getPhone())
                    .exists();
            if (phoneExists) {
                throw new BusinessException(ErrorCode.CONFLICT, "手机号已注册");
            }
        }
        User user = new User();
        user.setUsername(dto.getUsername());
        user.setPasswordHash(passwordEncoder.encode(dto.getPassword()));
        user.setNickname(dto.getNickname() != null ? dto.getNickname() : dto.getUsername());
        user.setPhone(dto.getPhone());
        user.setPointBalance(0);
        user.setTrustScore(new BigDecimal("5.0"));
        user.setStatus(1);
        save(user);
    }

    @Override
    public LoginVo login(LoginDto dto) {
        User user = lambdaQuery()
                .eq(User::getUsername, dto.getUsername())
                .one();
        if (user == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "用户名或密码错误");
        }
        if (user.getStatus() == 0) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "账号已被禁用");
        }
        if (!passwordEncoder.matches(dto.getPassword(), user.getPasswordHash())) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "用户名或密码错误");
        }
        return buildLoginVo(user);
    }

    @Override
    @Transactional
    public LoginVo smsLogin(SmsLoginDto dto) {
        String cacheKey = "dbs:sms:code:" + dto.getPhone();
        String cachedCode = redisTemplate.opsForValue().get(cacheKey);
        if (cachedCode == null || !cachedCode.equals(dto.getCode())) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "验证码错误或已过期");
        }
        redisTemplate.delete(cacheKey);

        User user = lambdaQuery()
                .eq(User::getPhone, dto.getPhone())
                .one();
        if (user == null) {
            user = new User();
            user.setUsername("phone_" + dto.getPhone());
            user.setPhone(dto.getPhone());
            user.setPasswordHash(passwordEncoder.encode(UUID.randomUUID().toString()));
            user.setNickname("用户" + dto.getPhone().substring(7));
            user.setPointBalance(0);
            user.setTrustScore(new BigDecimal("5.0"));
            user.setStatus(1);
            save(user);
        }
        if (user.getStatus() == 0) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "账号已被禁用");
        }
        return buildLoginVo(user);
    }

    @Override
    public void sendSmsCode(String phone) {
        String cacheKey = "dbs:sms:code:" + phone;
        String lastKey = "dbs:sms:last:" + phone;
        if (Boolean.TRUE.equals(redisTemplate.hasKey(lastKey))) {
            throw new BusinessException(ErrorCode.TOO_MANY_REQUESTS, "验证码发送过于频繁，请稍后再试");
        }
        String code = String.valueOf((int) ((Math.random() * 9 + 1) * 100000));
        redisTemplate.opsForValue().set(cacheKey, code, 5, TimeUnit.MINUTES);
        redisTemplate.opsForValue().set(lastKey, "1", 60, TimeUnit.SECONDS);
        // TODO: 调用短信网关发送验证码
    }

    @Override
    public LoginVo refreshToken(String refreshToken) {
        Long userId = JwtUtil.getUserId(JwtUtil.parseToken(refreshToken, jwtSecret));
        if (userId == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "refreshToken无效");
        }
        User user = getById(userId);
        if (user == null || user.getStatus() == 0) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "用户不存在或已禁用");
        }
        return buildLoginVo(user);
    }

    @Override
    public void logout(Long userId, String token) {
        if (token != null && !token.isEmpty()) {
            redisTemplate.opsForValue().set("dbs:blacklist:token:" + token, "1", jwtExpiration, TimeUnit.MILLISECONDS);
        }
    }

    @Override
    public UserProfileVo getProfile(Long userId) {
        User user = getById(userId);
        if (user == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "用户不存在");
        }
        return toProfileVo(user);
    }

    @Override
    @Transactional
    public void updateProfile(Long userId, UpdateProfileDto dto) {
        User user = getById(userId);
        if (user == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "用户不存在");
        }
        if (dto.getNickname() != null) user.setNickname(dto.getNickname());
        if (dto.getAvatar() != null) user.setAvatar(dto.getAvatar());
        if (dto.getEmail() != null) user.setEmail(dto.getEmail());
        if (dto.getCampus() != null) user.setCampus(dto.getCampus());
        if (dto.getBuilding() != null) user.setBuilding(dto.getBuilding());
        if (dto.getBio() != null) user.setBio(dto.getBio());
        updateById(user);
    }

    @Override
    @Transactional
    public void updatePassword(Long userId, UpdatePasswordDto dto) {
        User user = getById(userId);
        if (user == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "用户不存在");
        }
        if (!passwordEncoder.matches(dto.getOldPassword(), user.getPasswordHash())) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "原密码错误");
        }
        user.setPasswordHash(passwordEncoder.encode(dto.getNewPassword()));
        updateById(user);
    }

    @Override
    @Transactional
    public void updateLocation(Long userId, UpdateLocationDto dto) {
        User user = getById(userId);
        if (user == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "用户不存在");
        }
        user.setLongitude(dto.getLongitude());
        user.setLatitude(dto.getLatitude());
        if (dto.getCampus() != null) user.setCampus(dto.getCampus());
        if (dto.getBuilding() != null) user.setBuilding(dto.getBuilding());
        updateById(user);
    }

    private LoginVo buildLoginVo(User user) {
        List<String> roles = systemApi.getRoleCodesByUserId(user.getId());
        if (roles == null || roles.isEmpty()) {
            roles = Collections.singletonList("USER");
        }
        String accessToken = JwtUtil.generateToken(user.getId(), roles, jwtSecret, jwtExpiration);
        String refreshToken = JwtUtil.generateRefreshToken(user.getId(), jwtSecret, refreshExpiration);
        LoginVo vo = new LoginVo();
        vo.setAccessToken(accessToken);
        vo.setRefreshToken(refreshToken);
        vo.setExpiresIn(jwtExpiration / 1000);
        vo.setUserId(user.getId());
        vo.setUsername(user.getUsername());
        vo.setNickname(user.getNickname());
        vo.setAvatar(user.getAvatar());
        vo.setTrustScore(user.getTrustScore());
        return vo;
    }

    private UserProfileVo toProfileVo(User user) {
        UserProfileVo vo = new UserProfileVo();
        vo.setId(user.getId());
        vo.setUsername(user.getUsername());
        vo.setNickname(user.getNickname());
        vo.setAvatar(user.getAvatar());
        vo.setPhone(user.getPhone());
        vo.setEmail(user.getEmail());
        vo.setPointBalance(user.getPointBalance());
        vo.setTrustScore(user.getTrustScore());
        vo.setLongitude(user.getLongitude());
        vo.setLatitude(user.getLatitude());
        vo.setCampus(user.getCampus());
        vo.setBuilding(user.getBuilding());
        vo.setBio(user.getBio());
        vo.setStatus(user.getStatus());
        vo.setCreateTime(user.getCreateTime());
        return vo;
    }
}
