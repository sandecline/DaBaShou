package com.dabashou.credit.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
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
import com.dabashou.order.api.OrderApi;
import com.dabashou.user.api.UserApi;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CreditServiceImplTest {

    @InjectMocks
    private CreditServiceImpl creditService;

    @Mock
    private ViolationMapper violationMapper;
    @Mock
    private AppealMapper appealMapper;
    @Mock
    private OrderApi orderApi;
    @Mock
    private UserApi userApi;
    @Mock
    private ReviewMapper reviewMapper;

    @Captor
    private ArgumentCaptor<Review> reviewCaptor;
    @Captor
    private ArgumentCaptor<Violation> violationCaptor;
    @Captor
    private ArgumentCaptor<Appeal> appealCaptor;

    private static final Long USER_ID = 1L;
    private static final Long OTHER_USER_ID = 2L;
    private static final Long ORDER_ID = 100L;
    private static final Long VIOLATION_ID = 50L;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(creditService, "baseMapper", reviewMapper);
    }

    @Nested
    @DisplayName("提交评价")
    class SubmitReviewTests {

        @Test
        @DisplayName("买家评价成功")
        void submitReview_buyerSuccess() {
            when(orderApi.getStatus(ORDER_ID)).thenReturn(5L);
            when(orderApi.getSellerId(ORDER_ID)).thenReturn(OTHER_USER_ID);
            when(orderApi.getBuyerId(ORDER_ID)).thenReturn(USER_ID);
            when(reviewMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(0L);
            doAnswer(invocation -> {
                Review entity = invocation.getArgument(0);
                entity.setId(1L);
                return 1;
            }).when(reviewMapper).insert((Review) any());

            ReviewDto dto = new ReviewDto();
            dto.setOrderId(ORDER_ID);
            dto.setRating(5);
            dto.setContent("很好的服务");

            Long reviewId = creditService.submitReview(USER_ID, dto);

            assertNotNull(reviewId);
            verify(reviewMapper).insert(reviewCaptor.capture());
            Review captured = reviewCaptor.getValue();
            assertEquals(OTHER_USER_ID, captured.getRevieweeId());
            assertEquals(5, captured.getRating());
        }

        @Test
        @DisplayName("卖家评价成功")
        void submitReview_sellerSuccess() {
            when(orderApi.getStatus(ORDER_ID)).thenReturn(5L);
            when(orderApi.getSellerId(ORDER_ID)).thenReturn(USER_ID);
            when(orderApi.getBuyerId(ORDER_ID)).thenReturn(OTHER_USER_ID);
            when(reviewMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(0L);
            doAnswer(invocation -> {
                Review entity = invocation.getArgument(0);
                entity.setId(2L);
                return 1;
            }).when(reviewMapper).insert((Review) any());

            ReviewDto dto = new ReviewDto();
            dto.setOrderId(ORDER_ID);
            dto.setRating(4);

            Long reviewId = creditService.submitReview(USER_ID, dto);

            assertNotNull(reviewId);
        }

        @Test
        @DisplayName("未完成订单不能评价")
        void submitReview_orderNotCompleted() {
            when(orderApi.getStatus(ORDER_ID)).thenReturn(2L);

            ReviewDto dto = new ReviewDto();
            dto.setOrderId(ORDER_ID);
            dto.setRating(5);

            BusinessException ex = assertThrows(BusinessException.class,
                    () -> creditService.submitReview(USER_ID, dto));
            assertEquals(ErrorCode.CONFLICT.getCode(), ex.getCode());
        }

        @Test
        @DisplayName("非参与者不能评价")
        void submitReview_notParticipant() {
            when(orderApi.getStatus(ORDER_ID)).thenReturn(5L);
            when(orderApi.getSellerId(ORDER_ID)).thenReturn(OTHER_USER_ID);
            when(orderApi.getBuyerId(ORDER_ID)).thenReturn(3L);

            ReviewDto dto = new ReviewDto();
            dto.setOrderId(ORDER_ID);
            dto.setRating(5);

            BusinessException ex = assertThrows(BusinessException.class,
                    () -> creditService.submitReview(USER_ID, dto));
            assertEquals(ErrorCode.FORBIDDEN.getCode(), ex.getCode());
        }

        @Test
        @DisplayName("重复评价抛409")
        void submitReview_duplicate() {
            when(orderApi.getStatus(ORDER_ID)).thenReturn(5L);
            when(orderApi.getSellerId(ORDER_ID)).thenReturn(OTHER_USER_ID);
            when(orderApi.getBuyerId(ORDER_ID)).thenReturn(USER_ID);
            when(reviewMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(1L);

            ReviewDto dto = new ReviewDto();
            dto.setOrderId(ORDER_ID);
            dto.setRating(5);

            BusinessException ex = assertThrows(BusinessException.class,
                    () -> creditService.submitReview(USER_ID, dto));
            assertEquals(ErrorCode.CONFLICT.getCode(), ex.getCode());
        }
    }

    @Nested
    @DisplayName("举报违规")
    class ReportViolationTests {

        @Test
        @DisplayName("举报成功")
        void reportViolation_success() {
            doAnswer(invocation -> {
                Violation entity = invocation.getArgument(0);
                entity.setId(1L);
                return 1;
            }).when(violationMapper).insert((Violation) any());

            ViolationDto dto = new ViolationDto();
            dto.setTargetUserId(OTHER_USER_ID);
            dto.setOrderId(ORDER_ID);
            dto.setType("虚假服务");
            dto.setDescription("卖家没有提供承诺的服务");

            Long violationId = creditService.reportViolation(USER_ID, dto);

            assertNotNull(violationId);
            verify(violationMapper).insert(violationCaptor.capture());
            Violation captured = violationCaptor.getValue();
            assertEquals(OTHER_USER_ID, captured.getUserId());
            assertEquals(USER_ID, captured.getReporterId());
        }

        @Test
        @DisplayName("不能举报自己")
        void reportViolation_selfReport() {
            ViolationDto dto = new ViolationDto();
            dto.setTargetUserId(USER_ID);

            BusinessException ex = assertThrows(BusinessException.class,
                    () -> creditService.reportViolation(USER_ID, dto));
            assertEquals(ErrorCode.BAD_REQUEST.getCode(), ex.getCode());
        }
    }

    @Nested
    @DisplayName("申诉")
    class AppealTests {

        @Test
        @DisplayName("申诉成功")
        void submitAppeal_success() {
            Violation violation = new Violation();
            violation.setId(VIOLATION_ID);
            violation.setUserId(USER_ID);
            when(violationMapper.selectById(VIOLATION_ID)).thenReturn(violation);
            when(appealMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(0L);
            doAnswer(invocation -> {
                Appeal entity = invocation.getArgument(0);
                entity.setId(1L);
                return 1;
            }).when(appealMapper).insert((Appeal) any());

            AppealDto dto = new AppealDto();
            dto.setViolationId(VIOLATION_ID);
            dto.setReason("我不是故意的");

            Long appealId = creditService.submitAppeal(USER_ID, dto);

            assertNotNull(appealId);
        }

        @Test
        @DisplayName("违规不存在抛404")
        void submitAppeal_violationNotFound() {
            when(violationMapper.selectById(VIOLATION_ID)).thenReturn(null);

            AppealDto dto = new AppealDto();
            dto.setViolationId(VIOLATION_ID);
            dto.setReason("测试");

            BusinessException ex = assertThrows(BusinessException.class,
                    () -> creditService.submitAppeal(USER_ID, dto));
            assertEquals(ErrorCode.NOT_FOUND.getCode(), ex.getCode());
        }

        @Test
        @DisplayName("不能申诉他人违规")
        void submitAppeal_notOwner() {
            Violation violation = new Violation();
            violation.setId(VIOLATION_ID);
            violation.setUserId(OTHER_USER_ID);
            when(violationMapper.selectById(VIOLATION_ID)).thenReturn(violation);

            AppealDto dto = new AppealDto();
            dto.setViolationId(VIOLATION_ID);
            dto.setReason("测试");

            BusinessException ex = assertThrows(BusinessException.class,
                    () -> creditService.submitAppeal(USER_ID, dto));
            assertEquals(ErrorCode.FORBIDDEN.getCode(), ex.getCode());
        }

        @Test
        @DisplayName("重复申诉抛409")
        void submitAppeal_duplicate() {
            Violation violation = new Violation();
            violation.setId(VIOLATION_ID);
            violation.setUserId(USER_ID);
            when(violationMapper.selectById(VIOLATION_ID)).thenReturn(violation);
            when(appealMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(1L);

            AppealDto dto = new AppealDto();
            dto.setViolationId(VIOLATION_ID);
            dto.setReason("再次申诉");

            BusinessException ex = assertThrows(BusinessException.class,
                    () -> creditService.submitAppeal(USER_ID, dto));
            assertEquals(ErrorCode.CONFLICT.getCode(), ex.getCode());
        }
    }
}
