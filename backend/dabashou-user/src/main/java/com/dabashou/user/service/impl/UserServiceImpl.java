package com.dabashou.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.dabashou.common.enums.ErrorCode;
import com.dabashou.common.enums.TrustLevel;
import com.dabashou.common.exception.BusinessException;
import com.dabashou.common.utils.JwtUtil;
import com.dabashou.user.domain.User;
import com.dabashou.user.dto.*;
import com.dabashou.user.mapper.UserMapper;
import com.dabashou.user.service.UserService;
import com.dabashou.user.vo.*;
import io.jsonwebtoken.Claims;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * 用户服务实现
 */
@Service
public class UserServiceImpl implements UserService {

    private static final Logger log = LoggerFactory.getLogger(UserServiceImpl.class);

    private final UserMapper userMapper;
    private final JdbcTemplate jdbcTemplate;
    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Value("${dabashou.jwt.secret}")
    private String jwtSecret;

    @Value("${dabashou.jwt.expiration}")
    private long jwtExpiration;

    @Value("${dabashou.jwt.refresh-expiration}")
    private long jwtRefreshExpiration;

    public UserServiceImpl(UserMapper userMapper, JdbcTemplate jdbcTemplate) {
        this.userMapper = userMapper;
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    @org.springframework.transaction.annotation.Transactional(rollbackFor = Exception.class)
    public void register(RegisterDto dto) {
        // 检查用户名唯一性
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, dto.getUsername());
        if (userMapper.selectCount(wrapper) > 0) {
            throw new BusinessException(ErrorCode.CONFLICT, "用户名已存在");
        }

        // 创建用户
        User user = new User();
        user.setUsername(dto.getUsername());
        user.setPasswordHash(passwordEncoder.encode(dto.getPassword()));
        user.setNickname(dto.getNickname() != null ? dto.getNickname() : dto.getUsername());
        user.setPhone(dto.getPhone());
        user.setPointBalance(100);
        user.setTrustScore(BigDecimal.valueOf(5.0));
        user.setStatus(1);
        userMapper.insert(user);

        // 创建积分账户
        jdbcTemplate.update(
                "INSERT INTO dbs_point_account (user_id, available, frozen, total_earned) VALUES (?, ?, ?, ?)",
                user.getId(), 100, 0, 100
        );

        log.info("用户注册成功: username={}, userId={}", dto.getUsername(), user.getId());
    }

    @Override
    public LoginVo login(LoginDto dto) {
        // 查询用户
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, dto.getUsername());
        User user = userMapper.selectOne(wrapper);
        if (user == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "用户名或密码错误");
        }

        // 验证密码
        if (!passwordEncoder.matches(dto.getPassword(), user.getPasswordHash())) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "用户名或密码错误");
        }

        return buildLoginVo(user);
    }

    @Override
    public void sendSmsCode(String phone) {
        log.info("发送短信验证码(模拟): phone={}", phone);
    }

    @Override
    @org.springframework.transaction.annotation.Transactional(rollbackFor = Exception.class)
    public LoginVo smsLogin(SmsLoginDto dto) {
        log.info("短信验证码登录(模拟): phone={}, code={}", dto.getPhone(), dto.getCode());

        // 模拟：根据手机号查找用户，找不到则自动注册
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getPhone, dto.getPhone());
        User user = userMapper.selectOne(wrapper);

        if (user == null) {
            // 模拟自动注册
            user = new User();
            user.setUsername("u_" + dto.getPhone());
            user.setNickname("用户" + dto.getPhone().substring(Math.max(0, dto.getPhone().length() - 4)));
            user.setPhone(dto.getPhone());
            user.setPasswordHash(passwordEncoder.encode("default"));
            user.setPointBalance(100);
            user.setTrustScore(BigDecimal.valueOf(5.0));
            user.setStatus(1);
            userMapper.insert(user);
            jdbcTemplate.update(
                    "INSERT INTO dbs_point_account (user_id, available, frozen, total_earned) VALUES (?, ?, ?, ?)",
                    user.getId(), 100, 0, 100
            );
        }

        return buildLoginVo(user);
    }

    @Override
    public LoginVo refreshToken(String refreshToken) {
        Claims claims = JwtUtil.parseToken(refreshToken, jwtSecret);
        if (!JwtUtil.isRefreshToken(claims)) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "无效的刷新令牌");
        }

        Long userId = JwtUtil.getUserId(claims);
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "用户不存在");
        }

        return buildLoginVo(user);
    }

    @Override
    public UserProfileVo getProfile(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "用户不存在");
        }
        return toUserProfileVo(user);
    }

    @Override
    public void updateProfile(Long userId, UpdateProfileDto dto) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "用户不存在");
        }

        if (dto.getNickname() != null) {
            user.setNickname(dto.getNickname());
        }
        if (dto.getAvatar() != null) {
            user.setAvatar(dto.getAvatar());
        }
        if (dto.getBio() != null) {
            user.setBio(dto.getBio());
        }
        if (dto.getCampus() != null) {
            user.setCampus(dto.getCampus());
        }
        if (dto.getBuilding() != null) {
            user.setBuilding(dto.getBuilding());
        }

        userMapper.updateById(user);
        log.info("用户信息更新成功: userId={}", userId);
    }

    @Override
    public void changePassword(Long userId, ChangePasswordDto dto) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "用户不存在");
        }

        if (!passwordEncoder.matches(dto.getOldPassword(), user.getPasswordHash())) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "旧密码错误");
        }

        user.setPasswordHash(passwordEncoder.encode(dto.getNewPassword()));
        userMapper.updateById(user);
        log.info("用户密码修改成功: userId={}", userId);
    }

    @Override
    public void updateLocation(Long userId, UpdateLocationDto dto) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "用户不存在");
        }

        user.setLongitude(dto.getLongitude());
        user.setLatitude(dto.getLatitude());
        userMapper.updateById(user);
        log.info("用户位置更新成功: userId={}, lon={}, lat={}", userId, dto.getLongitude(), dto.getLatitude());
    }

    @Override
    public void submitCampusAuth(Long userId, CampusAuthDto dto) {
        jdbcTemplate.update(
                "INSERT INTO dbs_user_campus_auth (user_id, auth_type, student_no, real_name, campus, college, credential_file_id, status, create_time, update_time) " +
                        "VALUES (?, ?, ?, ?, ?, ?, ?, 0, NOW(), NOW())",
                userId, dto.getAuthType(), dto.getStudentNo(), dto.getRealName(),
                dto.getCampus(), dto.getCollege(), dto.getCredentialFileId()
        );
        log.info("校园认证提交成功: userId={}", userId);
    }

    @Override
    public CampusAuthVo getCampusAuth(Long userId) {
        List<Map<String, Object>> results = jdbcTemplate.queryForList(
                "SELECT id, auth_type, student_no, real_name, campus, college, status, " +
                        "review_remark, review_time, create_time " +
                        "FROM dbs_user_campus_auth WHERE user_id = ? ORDER BY create_time DESC LIMIT 1",
                userId
        );

        if (results.isEmpty()) {
            return null;
        }

        Map<String, Object> row = results.get(0);
        CampusAuthVo vo = new CampusAuthVo();
        vo.setId(asLong(row.get("id")));
        vo.setAuthType(asString(row.get("auth_type")));
        vo.setStudentNo(asString(row.get("student_no")));
        vo.setRealName(asString(row.get("real_name")));
        vo.setCampus(asString(row.get("campus")));
        vo.setCollege(asString(row.get("college")));
        vo.setStatus(asInt(row.get("status")));
        vo.setStatusDesc(getStatusDesc(asInt(row.get("status"))));
        vo.setReviewRemark(asString(row.get("review_remark")));
        vo.setReviewTime(asLocalDateTime(row.get("review_time")));
        vo.setCreateTime(asLocalDateTime(row.get("create_time")));
        return vo;
    }

    @Override
    public TrustScoreVo getTrustScore(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "用户不存在");
        }

        TrustScoreVo vo = new TrustScoreVo();
        vo.setScore(user.getTrustScore());

        double score = user.getTrustScore() != null ? user.getTrustScore().doubleValue() : 0;
        TrustLevel level = TrustLevel.ofScore(score);
        vo.setLevel(level.getLabel());

        // 查询最近10条信任分日志
        List<Map<String, Object>> logRows = jdbcTemplate.queryForList(
                "SELECT type, score_change, score_before, score_after, reason, create_time " +
                        "FROM dbs_user_trust_score_log WHERE user_id = ? ORDER BY create_time DESC LIMIT 10",
                userId
        );

        List<TrustLogItem> recentLogs = logRows.stream().map(row -> {
            TrustLogItem item = new TrustLogItem();
            item.setType(asString(row.get("type")));
            item.setScoreChange(asBigDecimal(row.get("score_change")));
            item.setScoreBefore(asBigDecimal(row.get("score_before")));
            item.setScoreAfter(asBigDecimal(row.get("score_after")));
            item.setReason(asString(row.get("reason")));
            item.setCreateTime(asLocalDateTime(row.get("create_time")));
            return item;
        }).toList();
        vo.setRecentLogs(recentLogs);

        return vo;
    }

    // ---- 内部辅助方法 ----

    private LoginVo buildLoginVo(User user) {
        List<String> roles = Collections.singletonList("USER");
        String accessToken = JwtUtil.generateToken(user.getId(), roles, jwtSecret, jwtExpiration);
        String refreshToken = JwtUtil.generateRefreshToken(user.getId(), jwtSecret, jwtRefreshExpiration);

        LoginVo vo = new LoginVo();
        vo.setAccessToken(accessToken);
        vo.setRefreshToken(refreshToken);
        vo.setExpiresIn(jwtExpiration / 1000);
        vo.setUserId(user.getId());
        vo.setNickname(user.getNickname());
        vo.setAvatar(user.getAvatar());
        return vo;
    }

    private UserProfileVo toUserProfileVo(User user) {
        UserProfileVo vo = new UserProfileVo();
        vo.setId(user.getId());
        vo.setUsername(user.getUsername());
        vo.setNickname(user.getNickname());
        vo.setAvatar(user.getAvatar());
        vo.setPhone(maskPhone(user.getPhone()));
        vo.setEmail(user.getEmail());
        vo.setPointBalance(user.getPointBalance());
        vo.setTrustScore(user.getTrustScore());

        double score = user.getTrustScore() != null ? user.getTrustScore().doubleValue() : 0;
        vo.setTrustLevel(TrustLevel.ofScore(score).getLabel());

        vo.setLongitude(user.getLongitude());
        vo.setLatitude(user.getLatitude());
        vo.setCampus(user.getCampus());
        vo.setBuilding(user.getBuilding());
        vo.setBio(user.getBio());
        vo.setStatus(user.getStatus());
        vo.setCreateTime(user.getCreateTime());
        return vo;
    }

    private String maskPhone(String phone) {
        if (phone == null || phone.length() < 11) {
            return phone;
        }
        return phone.substring(0, 3) + "****" + phone.substring(phone.length() - 4);
    }

    private String getStatusDesc(Integer status) {
        if (status == null) return null;
        return switch (status) {
            case 1 -> "已通过";
            case 2 -> "已拒绝";
            default -> "待审核";
        };
    }

    private String asString(Object value) {
        if (value == null) return null;
        return value.toString();
    }

    private Long asLong(Object value) {
        if (value == null) return null;
        if (value instanceof Long l) return l;
        if (value instanceof Number n) return n.longValue();
        return Long.valueOf(value.toString());
    }

    private Integer asInt(Object value) {
        if (value == null) return null;
        if (value instanceof Integer i) return i;
        if (value instanceof Number n) return n.intValue();
        return Integer.valueOf(value.toString());
    }

    private BigDecimal asBigDecimal(Object value) {
        if (value == null) return null;
        if (value instanceof BigDecimal bd) return bd;
        if (value instanceof Number n) return BigDecimal.valueOf(n.doubleValue());
        return new BigDecimal(value.toString());
    }

    private LocalDateTime asLocalDateTime(Object value) {
        if (value == null) return null;
        if (value instanceof LocalDateTime ldt) return ldt;
        if (value instanceof java.sql.Timestamp ts) return ts.toLocalDateTime();
        return null;
    }
}
