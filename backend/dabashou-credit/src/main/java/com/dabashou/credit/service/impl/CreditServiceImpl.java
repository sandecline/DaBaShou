package com.dabashou.credit.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.dabashou.common.core.PageResult;
import com.dabashou.common.enums.ErrorCode;
import com.dabashou.common.exception.BusinessException;
import com.dabashou.credit.domain.Appeal;
import com.dabashou.credit.domain.Review;
import com.dabashou.credit.domain.Violation;
import com.dabashou.credit.dto.AppealDto;
import com.dabashou.credit.dto.ReviewDto;
import com.dabashou.credit.dto.ViolationDto;
import com.dabashou.credit.mapper.AppealMapper;
import com.dabashou.credit.mapper.ReviewMapper;
import com.dabashou.credit.mapper.ViolationMapper;
import com.dabashou.credit.service.CreditService;
import com.dabashou.credit.vo.AppealVo;
import com.dabashou.credit.vo.ReviewVo;
import com.dabashou.credit.vo.ViolationVo;
import com.dabashou.order.api.OrderApi;
import com.dabashou.user.api.UserApi;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CreditServiceImpl extends ServiceImpl<ReviewMapper, Review> implements CreditService {

    private final ViolationMapper violationMapper;
    private final AppealMapper appealMapper;
    private final OrderApi orderApi;
    private final UserApi userApi;

    public CreditServiceImpl(ViolationMapper violationMapper, AppealMapper appealMapper,
                             OrderApi orderApi, UserApi userApi) {
        this.violationMapper = violationMapper;
        this.appealMapper = appealMapper;
        this.orderApi = orderApi;
        this.userApi = userApi;
    }

    @Override
    @Transactional
    public Long submitReview(Long userId, ReviewDto dto) {
        Long orderStatus = orderApi.getStatus(dto.getOrderId());
        if (orderStatus == null || orderStatus != 5) {
            throw new BusinessException(ErrorCode.CONFLICT, "只能评价已完成的订单");
        }
        Long sellerId = orderApi.getSellerId(dto.getOrderId());
        Long buyerId = orderApi.getBuyerId(dto.getOrderId());
        if (!userId.equals(buyerId) && !userId.equals(sellerId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "无权评价此订单");
        }
        Long revieweeId = userId.equals(buyerId) ? sellerId : buyerId;
        LambdaQueryWrapper<Review> qw = new LambdaQueryWrapper<>();
        qw.eq(Review::getOrderId, dto.getOrderId()).eq(Review::getReviewerId, userId);
        if (baseMapper.selectCount(qw) > 0) {
            throw new BusinessException(ErrorCode.CONFLICT, "已评价过此订单");
        }
        Review review = new Review();
        review.setOrderId(dto.getOrderId());
        review.setReviewerId(userId);
        review.setRevieweeId(revieweeId);
        review.setRating(dto.getRating());
        review.setContent(dto.getContent());
        baseMapper.insert(review);
        return review.getId();
    }

    @Override
    public PageResult<ReviewVo> getMyReviews(Long userId, int pageNum, int pageSize) {
        LambdaQueryWrapper<Review> qw = new LambdaQueryWrapper<>();
        qw.eq(Review::getReviewerId, userId).orderByDesc(Review::getCreateTime);
        List<Review> list = baseMapper.selectList(qw.last("LIMIT " + pageSize + " OFFSET " + (pageNum - 1) * pageSize));
        long total = baseMapper.selectCount(new LambdaQueryWrapper<Review>().eq(Review::getReviewerId, userId));
        return PageResult.of(total, toReviewVoList(list), pageNum, pageSize);
    }

    @Override
    public PageResult<ReviewVo> getReceivedReviews(Long userId, int pageNum, int pageSize) {
        LambdaQueryWrapper<Review> qw = new LambdaQueryWrapper<>();
        qw.eq(Review::getRevieweeId, userId).orderByDesc(Review::getCreateTime);
        List<Review> list = baseMapper.selectList(qw.last("LIMIT " + pageSize + " OFFSET " + (pageNum - 1) * pageSize));
        long total = baseMapper.selectCount(new LambdaQueryWrapper<Review>().eq(Review::getRevieweeId, userId));
        return PageResult.of(total, toReviewVoList(list), pageNum, pageSize);
    }

    @Override
    public ReviewVo getOrderReview(Long orderId) {
        LambdaQueryWrapper<Review> qw = new LambdaQueryWrapper<>();
        qw.eq(Review::getOrderId, orderId);
        Review review = baseMapper.selectOne(qw);
        if (review == null) return null;
        return toReviewVo(review);
    }

    @Override
    @Transactional
    public Long reportViolation(Long userId, ViolationDto dto) {
        if (userId.equals(dto.getTargetUserId())) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "不能举报自己");
        }
        Violation v = new Violation();
        v.setUserId(dto.getTargetUserId());
        v.setOrderId(dto.getOrderId());
        v.setType(dto.getType());
        v.setDescription(dto.getDescription());
        v.setReporterId(userId);
        v.setStatus(1);
        violationMapper.insert(v);
        return v.getId();
    }

    @Override
    public PageResult<ViolationVo> getMyViolations(Long userId, int pageNum, int pageSize) {
        LambdaQueryWrapper<Violation> qw = new LambdaQueryWrapper<>();
        if (userId != null) {
            qw.eq(Violation::getUserId, userId);
        }
        qw.orderByDesc(Violation::getCreateTime);
        List<Violation> list = violationMapper.selectList(qw.last("LIMIT " + pageSize + " OFFSET " + (pageNum - 1) * pageSize));
        long total = violationMapper.selectCount(userId != null ?
                new LambdaQueryWrapper<Violation>().eq(Violation::getUserId, userId) : new LambdaQueryWrapper<>());
        return PageResult.of(total, list.stream().map(this::toViolationVo).collect(Collectors.toList()), pageNum, pageSize);
    }

    @Override
    @Transactional
    public Long submitAppeal(Long userId, AppealDto dto) {
        Violation violation = violationMapper.selectById(dto.getViolationId());
        if (violation == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "违规记录不存在");
        }
        if (!violation.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "只能申诉自己的违规");
        }
        LambdaQueryWrapper<Appeal> qw = new LambdaQueryWrapper<>();
        qw.eq(Appeal::getViolationId, dto.getViolationId()).eq(Appeal::getAppellantId, userId);
        if (appealMapper.selectCount(qw) > 0) {
            throw new BusinessException(ErrorCode.CONFLICT, "已提交过申诉");
        }
        Appeal appeal = new Appeal();
        appeal.setViolationId(dto.getViolationId());
        appeal.setAppellantId(userId);
        appeal.setReason(dto.getReason());
        appeal.setEvidenceFileId(dto.getEvidenceFileId());
        appeal.setStatus(0);
        appealMapper.insert(appeal);
        return appeal.getId();
    }

    @Override
    public PageResult<AppealVo> getMyAppeals(Long userId, int pageNum, int pageSize) {
        LambdaQueryWrapper<Appeal> qw = new LambdaQueryWrapper<>();
        if (userId != null) {
            qw.eq(Appeal::getAppellantId, userId);
        }
        qw.orderByDesc(Appeal::getCreateTime);
        List<Appeal> list = appealMapper.selectList(qw.last("LIMIT " + pageSize + " OFFSET " + (pageNum - 1) * pageSize));
        long total = appealMapper.selectCount(userId != null ?
                new LambdaQueryWrapper<Appeal>().eq(Appeal::getAppellantId, userId) : new LambdaQueryWrapper<>());
        return PageResult.of(total, list.stream().map(this::toAppealVo).collect(Collectors.toList()), pageNum, pageSize);
    }

    @Override
    @Transactional
    public void processViolation(Long violationId, String result) {
        Violation violation = violationMapper.selectById(violationId);
        if (violation == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "违规记录不存在");
        }
        violation.setStatus(1);
        violationMapper.updateById(violation);
    }

    @Override
    @Transactional
    public void reviewAppeal(Long appealId, boolean approved, String reason) {
        Appeal appeal = appealMapper.selectById(appealId);
        if (appeal == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "申诉记录不存在");
        }
        if (appeal.getStatus() != 0) {
            throw new BusinessException(ErrorCode.CONFLICT, "该申诉已处理");
        }
        appeal.setStatus(approved ? 1 : 2);
        appeal.setReviewRemark(reason);
        appeal.setReviewTime(LocalDateTime.now());
        appealMapper.updateById(appeal);
    }

    private List<ReviewVo> toReviewVoList(List<Review> list) {
        return list.stream().map(this::toReviewVo).collect(Collectors.toList());
    }

    private ReviewVo toReviewVo(Review r) {
        ReviewVo vo = new ReviewVo();
        vo.setId(r.getId());
        vo.setOrderId(r.getOrderId());
        vo.setReviewerId(r.getReviewerId());
        vo.setReviewerNickname(userApi.getNickname(r.getReviewerId()));
        vo.setRevieweeId(r.getRevieweeId());
        vo.setRevieweeNickname(userApi.getNickname(r.getRevieweeId()));
        vo.setRating(r.getRating());
        vo.setContent(r.getContent());
        vo.setCreateTime(r.getCreateTime());
        return vo;
    }

    private ViolationVo toViolationVo(Violation v) {
        ViolationVo vo = new ViolationVo();
        vo.setId(v.getId());
        vo.setUserId(v.getUserId());
        vo.setUserNickname(userApi.getNickname(v.getUserId()));
        vo.setOrderId(v.getOrderId());
        vo.setType(v.getType());
        vo.setDescription(v.getDescription());
        vo.setPenaltyScore(v.getPenaltyScore());
        vo.setReporterId(v.getReporterId());
        vo.setReporterNickname(userApi.getNickname(v.getReporterId()));
        vo.setStatus(v.getStatus());
        vo.setCreateTime(v.getCreateTime());
        return vo;
    }

    private AppealVo toAppealVo(Appeal a) {
        AppealVo vo = new AppealVo();
        vo.setId(a.getId());
        vo.setViolationId(a.getViolationId());
        vo.setAppellantId(a.getAppellantId());
        vo.setAppellantNickname(userApi.getNickname(a.getAppellantId()));
        vo.setReason(a.getReason());
        vo.setEvidenceFileId(a.getEvidenceFileId());
        vo.setStatus(a.getStatus());
        vo.setReviewerId(a.getReviewerId());
        vo.setReviewRemark(a.getReviewRemark());
        vo.setReviewTime(a.getReviewTime());
        vo.setCreateTime(a.getCreateTime());
        return vo;
    }
}
