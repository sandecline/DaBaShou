package com.dabashou.shelf.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.dabashou.common.core.PageResult;
import com.dabashou.common.enums.ErrorCode;
import com.dabashou.common.exception.BusinessException;
import com.dabashou.shelf.domain.SkillShelf;
import com.dabashou.shelf.domain.TimeSlot;
import com.dabashou.shelf.dto.SkillShelfDto;
import com.dabashou.shelf.dto.TimeSlotDto;
import com.dabashou.shelf.dto.UpdateShelfDto;
import com.dabashou.shelf.mapper.SkillShelfMapper;
import com.dabashou.shelf.mapper.TimeSlotMapper;
import com.dabashou.shelf.service.ShelfService;
import com.dabashou.shelf.vo.ShelfDetailVo;
import com.dabashou.shelf.vo.ShelfItemVo;
import com.dabashou.shelf.vo.TimeSlotVo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 技能货架服务实现
 */
@Service
public class ShelfServiceImpl implements ShelfService {

    private static final Logger log = LoggerFactory.getLogger(ShelfServiceImpl.class);

    private final SkillShelfMapper skillShelfMapper;
    private final TimeSlotMapper timeSlotMapper;
    private final JdbcTemplate jdbcTemplate;

    public ShelfServiceImpl(SkillShelfMapper skillShelfMapper,
                            TimeSlotMapper timeSlotMapper,
                            JdbcTemplate jdbcTemplate) {
        this.skillShelfMapper = skillShelfMapper;
        this.timeSlotMapper = timeSlotMapper;
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long publish(Long userId, SkillShelfDto dto) {
        SkillShelf shelf = new SkillShelf();
        shelf.setUserId(userId);
        shelf.setSkillTagId(dto.getSkillTagId());
        shelf.setTitle(dto.getTitle());
        shelf.setDescription(dto.getDescription());
        shelf.setPointPrice(dto.getPointPrice());
        shelf.setDurationMinutes(dto.getDurationMinutes());
        shelf.setLocationType(dto.getLocationType());
        shelf.setStatus(1);
        shelf.setCreateTime(LocalDateTime.now());
        shelf.setUpdateTime(LocalDateTime.now());

        skillShelfMapper.insert(shelf);
        log.info("货架发布成功: shelfId={}, userId={}", shelf.getId(), userId);
        return shelf.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(Long userId, Long shelfId, UpdateShelfDto dto) {
        SkillShelf shelf = getShelfOrThrow(shelfId);
        checkOwner(shelf, userId);

        boolean changed = false;
        if (dto.getSkillTagId() != null) {
            shelf.setSkillTagId(dto.getSkillTagId());
            changed = true;
        }
        if (dto.getTitle() != null) {
            shelf.setTitle(dto.getTitle());
            changed = true;
        }
        if (dto.getDescription() != null) {
            shelf.setDescription(dto.getDescription());
            changed = true;
        }
        if (dto.getPointPrice() != null) {
            shelf.setPointPrice(dto.getPointPrice());
            changed = true;
        }
        if (dto.getDurationMinutes() != null) {
            shelf.setDurationMinutes(dto.getDurationMinutes());
            changed = true;
        }
        if (dto.getLocationType() != null) {
            shelf.setLocationType(dto.getLocationType());
            changed = true;
        }

        if (changed) {
            shelf.setUpdateTime(LocalDateTime.now());
            skillShelfMapper.updateById(shelf);
            log.info("货架更新成功: shelfId={}, userId={}", shelfId, userId);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void on(Long userId, Long shelfId) {
        SkillShelf shelf = getShelfOrThrow(shelfId);
        checkOwner(shelf, userId);

        if (shelf.getStatus() == 1) {
            throw new BusinessException(ErrorCode.CONFLICT, "货架已上架");
        }

        shelf.setStatus(1);
        shelf.setUpdateTime(LocalDateTime.now());
        skillShelfMapper.updateById(shelf);
        log.info("货架上架成功: shelfId={}, userId={}", shelfId, userId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void off(Long userId, Long shelfId) {
        SkillShelf shelf = getShelfOrThrow(shelfId);
        checkOwner(shelf, userId);

        if (shelf.getStatus() == 0) {
            throw new BusinessException(ErrorCode.CONFLICT, "货架已下架");
        }

        shelf.setStatus(0);
        shelf.setUpdateTime(LocalDateTime.now());
        skillShelfMapper.updateById(shelf);
        log.info("货架下架成功: shelfId={}, userId={}", shelfId, userId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long userId, Long shelfId) {
        SkillShelf shelf = getShelfOrThrow(shelfId);
        checkOwner(shelf, userId);

        // 检查是否有进行中的订单
        List<Long> orderIds = jdbcTemplate.queryForList(
                "SELECT id FROM dbs_order WHERE skill_shelf_id = ? AND status IN (2,3,4)",
                Long.class, shelfId
        );
        if (!orderIds.isEmpty()) {
            throw new BusinessException(ErrorCode.CONFLICT, "存在进行中的订单，无法删除货架");
        }

        skillShelfMapper.deleteById(shelfId);
        log.info("货架删除成功: shelfId={}, userId={}", shelfId, userId);
    }

    @Override
    public ShelfDetailVo getDetail(Long shelfId) {
        SkillShelf shelf = getShelfOrThrow(shelfId);

        ShelfDetailVo vo = new ShelfDetailVo();
        vo.setId(shelf.getId());
        vo.setUserId(shelf.getUserId());
        vo.setTitle(shelf.getTitle());
        vo.setDescription(shelf.getDescription());
        vo.setPointPrice(shelf.getPointPrice());
        vo.setDurationMinutes(shelf.getDurationMinutes());
        vo.setLocationType(shelf.getLocationType());
        vo.setLocationTypeDesc(getLocationTypeDesc(shelf.getLocationType()));
        vo.setStatus(shelf.getStatus());
        vo.setStatusDesc(getStatusDesc(shelf.getStatus()));
        vo.setCreateTime(shelf.getCreateTime());

        // 查询用户昵称、头像
        Map<String, Object> userInfo = queryUserInfo(shelf.getUserId());
        if (userInfo != null) {
            vo.setNickname(asString(userInfo.get("nickname")));
            vo.setAvatar(asString(userInfo.get("avatar")));
            vo.setTrustScore(asBigDecimal(userInfo.get("trust_score")));
        }

        // 查询 tag 名称
        String tagName = queryTagName(shelf.getSkillTagId());
        vo.setSkillTagName(tagName);

        return vo;
    }

    @Override
    public PageResult<ShelfItemVo> search(String keyword, Long categoryId, Long skillTagId,
                                           Integer locationType, String sortBy,
                                           int pageNum, int pageSize) {
        LambdaQueryWrapper<SkillShelf> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SkillShelf::getStatus, 1);

        if (keyword != null && !keyword.isBlank()) {
            wrapper.like(SkillShelf::getTitle, keyword);
        }

        if (categoryId != null) {
            // 先查该分类下的 tagId 列表
            List<Long> tagIds = jdbcTemplate.queryForList(
                    "SELECT id FROM dbs_skill_tag WHERE category_id = ? AND status = 1",
                    Long.class, categoryId
            );
            if (tagIds.isEmpty()) {
                return PageResult.empty(pageNum, pageSize);
            }
            wrapper.in(SkillShelf::getSkillTagId, tagIds);
        }

        if (skillTagId != null) {
            wrapper.eq(SkillShelf::getSkillTagId, skillTagId);
        }

        if (locationType != null) {
            wrapper.eq(SkillShelf::getLocationType, locationType);
        }

        // 排序
        if ("price".equals(sortBy)) {
            wrapper.orderByAsc(SkillShelf::getPointPrice);
        } else {
            // heat 默认按 createTime（id）倒序
            wrapper.orderByDesc(SkillShelf::getCreateTime);
        }

        Page<SkillShelf> page = new Page<>(pageNum, pageSize);
        Page<SkillShelf> result = skillShelfMapper.selectPage(page, wrapper);

        List<ShelfItemVo> list = assembleItemVos(result.getRecords());
        return PageResult.of(result.getTotal(), list, pageNum, pageSize);
    }

    @Override
    public PageResult<ShelfItemVo> listMine(Long userId, int pageNum, int pageSize) {
        LambdaQueryWrapper<SkillShelf> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SkillShelf::getUserId, userId);
        wrapper.orderByDesc(SkillShelf::getCreateTime);

        Page<SkillShelf> page = new Page<>(pageNum, pageSize);
        Page<SkillShelf> result = skillShelfMapper.selectPage(page, wrapper);

        List<ShelfItemVo> list = assembleItemVos(result.getRecords());
        return PageResult.of(result.getTotal(), list, pageNum, pageSize);
    }

    @Override
    public PageResult<ShelfItemVo> listUserShelves(Long userId, int pageNum, int pageSize) {
        LambdaQueryWrapper<SkillShelf> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SkillShelf::getUserId, userId);
        wrapper.eq(SkillShelf::getStatus, 1);
        wrapper.orderByDesc(SkillShelf::getCreateTime);

        Page<SkillShelf> page = new Page<>(pageNum, pageSize);
        Page<SkillShelf> result = skillShelfMapper.selectPage(page, wrapper);

        List<ShelfItemVo> list = assembleItemVos(result.getRecords());
        return PageResult.of(result.getTotal(), list, pageNum, pageSize);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addTimeSlots(Long userId, Long shelfId, List<TimeSlotDto> slots) {
        SkillShelf shelf = getShelfOrThrow(shelfId);
        checkOwner(shelf, userId);

        LocalDate monday = LocalDate.now().with(TemporalAdjusters.previousOrSame(java.time.DayOfWeek.MONDAY));
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");

        for (TimeSlotDto dto : slots) {
            TimeSlot slot = new TimeSlot();
            slot.setUserId(userId);
            slot.setDate(monday.plusDays(dto.getDayOfWeek() - 1));
            slot.setStartTime(LocalTime.parse(dto.getStartTime(), timeFormatter));
            slot.setEndTime(LocalTime.parse(dto.getEndTime(), timeFormatter));
            slot.setStatus(1);
            slot.setCreateTime(LocalDateTime.now());
            slot.setUpdateTime(LocalDateTime.now());
            timeSlotMapper.insert(slot);
        }

        log.info("时间格子添加成功: shelfId={}, userId={}, count={}", shelfId, userId, slots.size());
    }

    @Override
    public List<TimeSlotVo> getTimeSlots(Long shelfId) {
        SkillShelf shelf = getShelfOrThrow(shelfId);
        List<TimeSlot> slots = timeSlotMapper.selectByUserId(shelf.getUserId());

        return slots.stream().map(slot -> {
            TimeSlotVo vo = new TimeSlotVo();
            vo.setId(slot.getId());
            vo.setDayOfWeek(slot.getDate().getDayOfWeek().getValue());
            vo.setStartTime(slot.getStartTime().format(DateTimeFormatter.ofPattern("HH:mm")));
            vo.setEndTime(slot.getEndTime().format(DateTimeFormatter.ofPattern("HH:mm")));
            vo.setAvailable(slot.getStatus() == 1);
            return vo;
        }).toList();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteTimeSlot(Long userId, Long shelfId, Long slotId) {
        SkillShelf shelf = getShelfOrThrow(shelfId);
        checkOwner(shelf, userId);

        int affected = timeSlotMapper.deleteByIdAndUserId(slotId, userId);
        if (affected == 0) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "时间格子不存在或无权删除");
        }

        log.info("时间格子删除成功: slotId={}, shelfId={}, userId={}", slotId, shelfId, userId);
    }

    // ---- 私有辅助方法 ----

    private SkillShelf getShelfOrThrow(Long shelfId) {
        SkillShelf shelf = skillShelfMapper.selectById(shelfId);
        if (shelf == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "货架不存在");
        }
        return shelf;
    }

    private void checkOwner(SkillShelf shelf, Long userId) {
        if (!shelf.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "无权限操作该货架");
        }
    }

    private String getLocationTypeDesc(Integer locationType) {
        if (locationType == null) {
            return null;
        }
        return switch (locationType) {
            case 1 -> "线上";
            case 2 -> "线下";
            case 3 -> "线上/线下";
            default -> "未知";
        };
    }

    private String getStatusDesc(Integer status) {
        if (status == null) {
            return null;
        }
        return switch (status) {
            case 0 -> "已下架";
            case 1 -> "上架中";
            case 2 -> "审核中";
            default -> "未知";
        };
    }

    /**
     * 批量组装 ShelfItemVo
     */
    private List<ShelfItemVo> assembleItemVos(List<SkillShelf> shelves) {
        if (shelves.isEmpty()) {
            return Collections.emptyList();
        }

        // 收集用户ID列表
        List<Long> userIds = shelves.stream()
                .map(SkillShelf::getUserId)
                .distinct()
                .toList();

        // 收集 tagId 列表
        List<Long> tagIds = shelves.stream()
                .map(SkillShelf::getSkillTagId)
                .distinct()
                .toList();

        // 批量查用户信息
        Map<Long, Map<String, Object>> userMap = batchQueryUsers(userIds);

        // 批量查 tag 名称
        Map<Long, String> tagNameMap = batchQueryTagNames(tagIds);

        // 组装
        List<ShelfItemVo> result = new ArrayList<>();
        for (SkillShelf shelf : shelves) {
            ShelfItemVo vo = new ShelfItemVo();
            vo.setId(shelf.getId());
            vo.setUserId(shelf.getUserId());
            vo.setTitle(shelf.getTitle());
            vo.setPointPrice(shelf.getPointPrice());
            vo.setDurationMinutes(shelf.getDurationMinutes());
            vo.setLocationType(shelf.getLocationType());
            vo.setStatus(shelf.getStatus());

            // 用户信息
            Map<String, Object> userInfo = userMap.get(shelf.getUserId());
            if (userInfo != null) {
                vo.setNickname(asString(userInfo.get("nickname")));
                vo.setAvatar(asString(userInfo.get("avatar")));
                vo.setTrustScore(asBigDecimal(userInfo.get("trust_score")));
            }

            // tag 名称
            vo.setSkillTagName(tagNameMap.getOrDefault(shelf.getSkillTagId(), null));

            result.add(vo);
        }
        return result;
    }

    private Map<String, Object> queryUserInfo(Long userId) {
        List<Map<String, Object>> results = jdbcTemplate.queryForList(
                "SELECT nickname, avatar, trust_score FROM dbs_user WHERE id = ?", userId
        );
        return results.isEmpty() ? null : results.get(0);
    }

    private String queryTagName(Long tagId) {
        List<String> results = jdbcTemplate.queryForList(
                "SELECT name FROM dbs_skill_tag WHERE id = ?", String.class, tagId
        );
        return results.isEmpty() ? null : results.get(0);
    }

    private Map<Long, Map<String, Object>> batchQueryUsers(List<Long> userIds) {
        if (userIds.isEmpty()) {
            return Collections.emptyMap();
        }

        String placeholders = userIds.stream().map(id -> "?").collect(Collectors.joining(","));
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(
                "SELECT id, nickname, avatar, trust_score FROM dbs_user WHERE id IN (" + placeholders + ")",
                userIds.toArray()
        );
        return rows.stream().collect(Collectors.toMap(
                row -> asLong(row.get("id")),
                row -> row
        ));
    }

    private Map<Long, String> batchQueryTagNames(List<Long> tagIds) {
        if (tagIds.isEmpty()) {
            return Collections.emptyMap();
        }

        String placeholders = tagIds.stream().map(id -> "?").collect(Collectors.joining(","));
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(
                "SELECT id, name FROM dbs_skill_tag WHERE id IN (" + placeholders + ")",
                tagIds.toArray()
        );
        return rows.stream().collect(Collectors.toMap(
                row -> asLong(row.get("id")),
                row -> asString(row.get("name"))
        ));
    }

    // ---- JdbcTemplate 结果类型转换 ----

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

    private BigDecimal asBigDecimal(Object value) {
        if (value == null) return null;
        if (value instanceof BigDecimal bd) return bd;
        if (value instanceof Number n) return BigDecimal.valueOf(n.doubleValue());
        return new BigDecimal(value.toString());
    }
}
