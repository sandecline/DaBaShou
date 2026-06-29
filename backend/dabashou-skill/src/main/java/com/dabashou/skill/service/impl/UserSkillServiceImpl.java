package com.dabashou.skill.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.dabashou.common.enums.ErrorCode;
import com.dabashou.common.exception.BusinessException;
import com.dabashou.skill.domain.SkillTag;
import com.dabashou.skill.domain.UserSkill;
import com.dabashou.skill.dto.UpdateUserSkillDto;
import com.dabashou.skill.dto.UserSkillDto;
import com.dabashou.skill.mapper.SkillTagMapper;
import com.dabashou.skill.mapper.UserSkillMapper;
import com.dabashou.skill.service.UserSkillService;
import com.dabashou.skill.vo.UserSkillVo;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 用户技能服务实现
 */
@Service
public class UserSkillServiceImpl implements UserSkillService {

    private final UserSkillMapper userSkillMapper;
    private final SkillTagMapper skillTagMapper;
    private final JdbcTemplate jdbcTemplate;

    public UserSkillServiceImpl(UserSkillMapper userSkillMapper,
                                SkillTagMapper skillTagMapper,
                                JdbcTemplate jdbcTemplate) {
        this.userSkillMapper = userSkillMapper;
        this.skillTagMapper = skillTagMapper;
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public List<UserSkillVo> listMySkills(Long userId) {
        String sql = "SELECT us.id, us.skill_tag_id, us.proficiency, us.description, us.create_time, " +
                "st.name AS skill_tag_name, sc.name AS category_name " +
                "FROM dbs_user_skill us " +
                "LEFT JOIN dbs_skill_tag st ON us.skill_tag_id = st.id " +
                "LEFT JOIN dbs_skill_category sc ON st.category_id = sc.id " +
                "WHERE us.user_id = ? " +
                "ORDER BY us.create_time DESC";

        List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql, userId);

        List<UserSkillVo> result = new ArrayList<>();
        for (Map<String, Object> row : rows) {
            UserSkillVo vo = new UserSkillVo();
            vo.setId(asLong(row.get("id")));
            vo.setSkillTagId(asLong(row.get("skill_tag_id")));
            vo.setSkillTagName(asString(row.get("skill_tag_name")));
            vo.setCategoryName(asString(row.get("category_name")));
            Integer proficiency = asInt(row.get("proficiency"));
            vo.setProficiency(proficiency);
            vo.setProficiencyDesc(toProficiencyDesc(proficiency));
            vo.setDescription(asString(row.get("description")));
            vo.setCreateTime(asLocalDateTime(row.get("create_time")));
            result.add(vo);
        }
        return result;
    }

    @Override
    @Transactional
    public Long addSkill(Long userId, UserSkillDto dto) {
        // 校验标签存在
        SkillTag tag = skillTagMapper.selectById(dto.getSkillTagId());
        if (tag == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "技能标签不存在");
        }

        // 校验是否已添加该技能（唯一约束 userId + skillTagId）
        LambdaQueryWrapper<UserSkill> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserSkill::getUserId, userId)
                .eq(UserSkill::getSkillTagId, dto.getSkillTagId());
        if (userSkillMapper.selectCount(wrapper) > 0) {
            throw new BusinessException(ErrorCode.CONFLICT, "技能已存在");
        }

        // 插入
        UserSkill userSkill = new UserSkill();
        userSkill.setUserId(userId);
        userSkill.setSkillTagId(dto.getSkillTagId());
        userSkill.setProficiency(dto.getProficiency());
        userSkill.setDescription(dto.getDescription());
        userSkillMapper.insert(userSkill);

        return userSkill.getId();
    }

    @Override
    @Transactional
    public void updateSkill(Long userId, Long id, UpdateUserSkillDto dto) {
        UserSkill userSkill = userSkillMapper.selectById(id);
        if (userSkill == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "技能记录不存在");
        }
        if (!userSkill.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "无权修改他人技能");
        }

        if (dto.getProficiency() != null) {
            userSkill.setProficiency(dto.getProficiency());
        }
        if (dto.getDescription() != null) {
            userSkill.setDescription(dto.getDescription());
        }
        userSkillMapper.updateById(userSkill);
    }

    @Override
    @Transactional
    public void deleteSkill(Long userId, Long id) {
        UserSkill userSkill = userSkillMapper.selectById(id);
        if (userSkill == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "技能记录不存在");
        }
        if (!userSkill.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "无权删除他人技能");
        }

        userSkillMapper.deleteById(id);
    }

    /**
     * 熟练度 → 中文描述映射
     */
    private String toProficiencyDesc(Integer proficiency) {
        if (proficiency == null) return null;
        return switch (proficiency) {
            case 1 -> "了解";
            case 2 -> "熟悉";
            case 3 -> "精通";
            case 4 -> "专家";
            default -> "未知";
        };
    }

    // ---- JDBC 类型转换辅助方法 ----

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

    private LocalDateTime asLocalDateTime(Object value) {
        if (value == null) return null;
        if (value instanceof LocalDateTime ldt) return ldt;
        if (value instanceof java.sql.Timestamp ts) return ts.toLocalDateTime();
        return null;
    }
}
