package com.dabashou.demand.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.dabashou.common.core.PageResult;
import com.dabashou.common.enums.ErrorCode;
import com.dabashou.common.exception.BusinessException;
import com.dabashou.demand.domain.Demand;
import com.dabashou.demand.dto.AcceptDto;
import com.dabashou.demand.dto.DemandDto;
import com.dabashou.demand.dto.UpdateDemandDto;
import com.dabashou.demand.mapper.DemandMapper;
import com.dabashou.demand.service.DemandService;
import com.dabashou.demand.vo.AcceptResultVo;
import com.dabashou.demand.vo.DemandDetailVo;
import com.dabashou.demand.vo.DemandItemVo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 需求服务实现 — 接单后由前端续调订单模块 createOrderFromDemand 完成订单创建
 */
@Service
public class DemandServiceImpl extends ServiceImpl<DemandMapper, Demand> implements DemandService {

    private static final Logger log = LoggerFactory.getLogger(DemandServiceImpl.class);

    private final JdbcTemplate jdbcTemplate;

    public DemandServiceImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long publish(Long userId, DemandDto dto) {
        Demand demand = new Demand();
        demand.setUserId(userId);
        demand.setSkillTagId(dto.getSkillTagId());
        demand.setTitle(dto.getTitle());
        demand.setDescription(dto.getDescription());
        demand.setPointReward(dto.getPointReward());
        if (dto.getDeadline() != null && !dto.getDeadline().isBlank()) {
            demand.setDeadline(LocalDateTime.parse(dto.getDeadline().replace(" ", "T")));
        }
        demand.setLocationType(dto.getLocationType());
        demand.setDemandType(dto.getDemandType() != null ? dto.getDemandType() : 1);
        demand.setLongitude(dto.getLongitude());
        demand.setLatitude(dto.getLatitude());
        demand.setStatus(1);
        demand.setCreateTime(LocalDateTime.now());
        demand.setUpdateTime(LocalDateTime.now());
        save(demand);
        log.info("发布需求: demandId={}, userId={}, title={}", demand.getId(), userId, dto.getTitle());
        return demand.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(Long userId, Long demandId, UpdateDemandDto dto) {
        Demand demand = getByIdOrThrow(demandId);
        if (!userId.equals(demand.getUserId())) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "只能编辑自己发布的需求");
        }
        if (!Objects.equals(demand.getStatus(), 1)) {
            throw new BusinessException(ErrorCode.CONFLICT, "只有待接单状态的需求才能编辑");
        }
        if (dto.getSkillTagId() != null) demand.setSkillTagId(dto.getSkillTagId());
        if (dto.getTitle() != null) demand.setTitle(dto.getTitle());
        if (dto.getDescription() != null) demand.setDescription(dto.getDescription());
        if (dto.getPointReward() != null) demand.setPointReward(dto.getPointReward());
        if (dto.getDeadline() != null && !dto.getDeadline().isBlank())
            demand.setDeadline(LocalDateTime.parse(dto.getDeadline().replace(" ", "T")));
        if (dto.getLocationType() != null) demand.setLocationType(dto.getLocationType());
        if (dto.getDemandType() != null) demand.setDemandType(dto.getDemandType());
        if (dto.getLongitude() != null) demand.setLongitude(dto.getLongitude());
        if (dto.getLatitude() != null) demand.setLatitude(dto.getLatitude());
        demand.setUpdateTime(LocalDateTime.now());
        updateById(demand);
        log.info("编辑需求: demandId={}, userId={}", demandId, userId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void close(Long userId, Long demandId) {
        Demand demand = getByIdOrThrow(demandId);
        if (!userId.equals(demand.getUserId())) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "只能关闭自己发布的需求");
        }
        demand.setStatus(0);
        demand.setUpdateTime(LocalDateTime.now());
        updateById(demand);
        log.info("关闭需求: demandId={}, userId={}", demandId, userId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long userId, Long demandId) {
        Demand demand = getByIdOrThrow(demandId);
        if (!userId.equals(demand.getUserId())) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "只能删除自己发布的需求");
        }
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM dbs_order WHERE demand_id = ? AND status IN (2, 3, 4)",
                Integer.class, demandId);
        if (count != null && count > 0) {
            throw new BusinessException(ErrorCode.CONFLICT, "存在进行中的订单，无法删除");
        }
        removeById(demandId);
        log.info("删除需求: demandId={}, userId={}", demandId, userId);
    }

    @Override
    public DemandDetailVo getDetail(Long demandId) {
        Demand demand = getByIdOrThrow(demandId);
        DemandDetailVo vo = new DemandDetailVo();
        fillItemVo(vo, demand);

        Map<String, Object> userInfo = queryUserInfo(demand.getUserId());
        if (userInfo != null) {
            vo.setNickname((String) userInfo.get("nickname"));
            vo.setAvatar((String) userInfo.get("avatar"));
        }
        vo.setSkillTagName(queryTagName(demand.getSkillTagId()));
        vo.setDescription(demand.getDescription());
        vo.setDemandTypeDesc(demandTypeDesc(demand.getDemandType()));
        vo.setStatusDesc(statusDesc(demand.getStatus()));
        vo.setLongitude(demand.getLongitude());
        vo.setLatitude(demand.getLatitude());
        vo.setCampus(demand.getCampus());
        vo.setBuilding(demand.getBuilding());
        return vo;
    }

    @Override
    public PageResult<DemandItemVo> search(String keyword, Long categoryId, Long skillTagId,
                                           Integer demandType, Integer status, String sortBy,
                                           int pageNum, int pageSize) {
        LambdaQueryWrapper<Demand> wrapper = new LambdaQueryWrapper<>();
        if (keyword != null && !keyword.isBlank()) wrapper.like(Demand::getTitle, keyword);
        if (categoryId != null) {
            List<Long> tagIds = queryTagIdsByCategory(categoryId);
            if (tagIds.isEmpty()) return PageResult.empty(pageNum, pageSize);
            wrapper.in(Demand::getSkillTagId, tagIds);
        }
        if (skillTagId != null) wrapper.eq(Demand::getSkillTagId, skillTagId);
        if (demandType != null) wrapper.eq(Demand::getDemandType, demandType);
        if (status != null) wrapper.eq(Demand::getStatus, status);
        if ("budget".equals(sortBy)) wrapper.orderByDesc(Demand::getPointReward);
        else wrapper.orderByDesc(Demand::getCreateTime);

        Page<Demand> page = page(new Page<>(pageNum, pageSize), wrapper);
        if (page.getRecords().isEmpty()) return PageResult.of(page.getTotal(), List.of(), pageNum, pageSize);

        Set<Long> userIds = page.getRecords().stream().map(Demand::getUserId).collect(Collectors.toSet());
        Set<Long> tagIds = page.getRecords().stream().map(Demand::getSkillTagId).filter(Objects::nonNull).collect(Collectors.toSet());
        Map<Long, String> nicknameMap = batchQueryNicknames(userIds);
        Map<Long, String> avatarMap = batchQueryAvatars(userIds);
        Map<Long, String> tagNameMap = batchQueryTagNames(tagIds);

        List<DemandItemVo> list = page.getRecords().stream()
                .map(d -> toItemVo(d, nicknameMap, avatarMap, tagNameMap))
                .collect(Collectors.toList());
        return PageResult.of(page.getTotal(), list, pageNum, pageSize);
    }

    @Override
    public PageResult<DemandItemVo> listMine(Long userId, int pageNum, int pageSize) {
        LambdaQueryWrapper<Demand> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Demand::getUserId, userId);
        wrapper.orderByDesc(Demand::getCreateTime);
        Page<Demand> page = page(new Page<>(pageNum, pageSize), wrapper);
        if (page.getRecords().isEmpty()) return PageResult.of(page.getTotal(), List.of(), pageNum, pageSize);

        Set<Long> userIds = Set.of(userId);
        Set<Long> tagIds = page.getRecords().stream().map(Demand::getSkillTagId).filter(Objects::nonNull).collect(Collectors.toSet());
        Map<Long, String> nicknameMap = batchQueryNicknames(userIds);
        Map<Long, String> avatarMap = batchQueryAvatars(userIds);
        Map<Long, String> tagNameMap = batchQueryTagNames(tagIds);

        List<DemandItemVo> list = page.getRecords().stream()
                .map(d -> toItemVo(d, nicknameMap, avatarMap, tagNameMap))
                .collect(Collectors.toList());
        return PageResult.of(page.getTotal(), list, pageNum, pageSize);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AcceptResultVo accept(Long userId, Long demandId, AcceptDto dto) {
        Demand demand = getByIdOrThrow(demandId);
        if (!Objects.equals(demand.getStatus(), 1)) {
            throw new BusinessException(ErrorCode.CONFLICT, "需求不在待接单状态");
        }
        if (userId.equals(demand.getUserId())) {
            throw new BusinessException(ErrorCode.CONFLICT, "不能接自己发布的需求");
        }
        demand.setStatus(2);
        demand.setUpdateTime(LocalDateTime.now());
        updateById(demand);
        log.info("接单: demandId={}, userId={}, shelfId={}", demandId, userId, dto.getShelfId());

        AcceptResultVo vo = new AcceptResultVo();
        vo.setDemandId(demandId);
        vo.setShelfId(dto.getShelfId());
        vo.setBuyerId(demand.getUserId());
        vo.setSkillTagId(demand.getSkillTagId());
        vo.setTitle(demand.getTitle());
        vo.setPointReward(demand.getPointReward());
        vo.setIdempotentToken(dto.getIdempotentToken());
        vo.setRemark(dto.getRemark());
        return vo;
    }

    // ========== 私有方法 ==========

    private Demand getByIdOrThrow(Long demandId) {
        Demand demand = getById(demandId);
        if (demand == null) throw new BusinessException(ErrorCode.NOT_FOUND, "需求不存在: " + demandId);
        return demand;
    }

    private void fillItemVo(DemandItemVo vo, Demand demand) {
        vo.setId(demand.getId());
        vo.setUserId(demand.getUserId());
        vo.setTitle(demand.getTitle());
        vo.setPointReward(demand.getPointReward());
        vo.setDeadline(demand.getDeadline());
        vo.setLocationType(demand.getLocationType());
        vo.setDemandType(demand.getDemandType());
        vo.setStatus(demand.getStatus());
        vo.setCreateTime(demand.getCreateTime());
    }

    private DemandItemVo toItemVo(Demand demand, Map<Long, String> nicknameMap,
                                   Map<Long, String> avatarMap, Map<Long, String> tagNameMap) {
        DemandItemVo vo = new DemandItemVo();
        fillItemVo(vo, demand);
        vo.setNickname(nicknameMap.get(demand.getUserId()));
        vo.setAvatar(avatarMap.get(demand.getUserId()));
        vo.setSkillTagName(tagNameMap.get(demand.getSkillTagId()));
        return vo;
    }

    private String demandTypeDesc(Integer type) {
        return switch (type == null ? 0 : type) {
            case 1 -> "求助悬赏";
            case 2 -> "技能置换";
            default -> "未知";
        };
    }

    private String statusDesc(Integer status) {
        return switch (status == null ? -1 : status) {
            case 0 -> "已关闭";
            case 1 -> "开放中";
            case 2 -> "进行中";
            case 3 -> "已完成";
            default -> "未知";
        };
    }

    private Map<String, Object> queryUserInfo(Long userId) {
        try { return jdbcTemplate.queryForMap("SELECT nickname, avatar FROM dbs_user WHERE id = ?", userId); }
        catch (EmptyResultDataAccessException e) { return null; }
    }

    private String queryTagName(Long tagId) {
        if (tagId == null) return null;
        try { return jdbcTemplate.queryForObject("SELECT name FROM dbs_skill_tag WHERE id = ?", String.class, tagId); }
        catch (EmptyResultDataAccessException e) { return null; }
    }

    private List<Long> queryTagIdsByCategory(Long categoryId) {
        return jdbcTemplate.queryForList("SELECT id FROM dbs_skill_tag WHERE category_id = ? AND status = 1", categoryId)
                .stream().map(r -> ((Number) r.get("id")).longValue()).collect(Collectors.toList());
    }

    private Map<Long, String> batchQueryNicknames(Collection<Long> userIds) {
        if (userIds == null || userIds.isEmpty()) return Map.of();
        String ph = String.join(", ", Collections.nCopies(userIds.size(), "?"));
        return jdbcTemplate.queryForList("SELECT id, nickname FROM dbs_user WHERE id IN (" + ph + ")", userIds.toArray())
                .stream().collect(Collectors.toMap(r -> ((Number) r.get("id")).longValue(), r -> (String) r.get("nickname"), (a, b) -> a));
    }

    private Map<Long, String> batchQueryAvatars(Collection<Long> userIds) {
        if (userIds == null || userIds.isEmpty()) return Map.of();
        String ph = String.join(", ", Collections.nCopies(userIds.size(), "?"));
        return jdbcTemplate.queryForList("SELECT id, avatar FROM dbs_user WHERE id IN (" + ph + ")", userIds.toArray())
                .stream().collect(Collectors.toMap(r -> ((Number) r.get("id")).longValue(), r -> (String) r.get("avatar"), (a, b) -> a));
    }

    private Map<Long, String> batchQueryTagNames(Collection<Long> tagIds) {
        if (tagIds == null || tagIds.isEmpty()) return Map.of();
        String ph = String.join(", ", Collections.nCopies(tagIds.size(), "?"));
        return jdbcTemplate.queryForList("SELECT id, name FROM dbs_skill_tag WHERE id IN (" + ph + ")", tagIds.toArray())
                .stream().collect(Collectors.toMap(r -> ((Number) r.get("id")).longValue(), r -> (String) r.get("name"), (a, b) -> a));
    }
}
