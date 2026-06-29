package com.dabashou.point.service.impl;

import com.dabashou.common.enums.ErrorCode;
import com.dabashou.common.exception.BusinessException;
import com.dabashou.point.domain.PointTransaction;
import com.dabashou.point.mapper.PointTransactionMapper;
import com.dabashou.point.vo.PointBalanceVo;
import com.dabashou.user.api.UserApi;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PointServiceImplTest {

    @InjectMocks
    private PointServiceImpl pointService;

    @Mock
    private UserApi userApi;
    @Mock
    private StringRedisTemplate redisTemplate;
    @Mock
    private PointTransactionMapper pointTransactionMapper;
    @Mock
    private ValueOperations<String, String> valueOperations;

    @Captor
    private ArgumentCaptor<PointTransaction> transCaptor;

    private static final Long USER_ID = 1L;
    private static final Long SELLER_ID = 2L;
    private static final Long ORDER_ID = 100L;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(pointService, "baseMapper", pointTransactionMapper);
        lenient().when(redisTemplate.opsForValue()).thenReturn(valueOperations);
    }

    private void mockUserBalance(int balance) {
        lenient().when(userApi.getPointBalance(USER_ID)).thenReturn(balance);
    }

    @Nested
    @DisplayName("余额查询")
    class BalanceTests {

        @Test
        @DisplayName("查询余额成功")
        void getBalance_success() {
            when(userApi.getPointBalance(USER_ID)).thenReturn(1000);
            when(pointTransactionMapper.selectList(any())).thenReturn(java.util.List.of());

            PointBalanceVo vo = pointService.getBalance(USER_ID);

            assertEquals(1000, vo.getAvailable());
            assertEquals(0, vo.getFrozen());
            assertEquals(1000, vo.getTotal());
        }
    }

    @Nested
    @DisplayName("冻结与解冻")
    class FreezeTests {

        @Test
        @DisplayName("冻结积分成功")
        void freeze_success() {
            mockUserBalance(1000);

            pointService.freeze(USER_ID, ORDER_ID, 500, "订单支付冻结");

            verify(userApi).deductPointBalance(USER_ID, 500);
            verify(pointTransactionMapper).insert(transCaptor.capture());
            PointTransaction captured = transCaptor.getValue();
            assertEquals(3, captured.getType());
            assertEquals(500, captured.getAmount());
        }

        @Test
        @DisplayName("解冻并转账成功")
        void unfreezeAndTransfer_success() {
            when(userApi.getPointBalance(SELLER_ID)).thenReturn(500);
            when(userApi.getPointBalance(USER_ID)).thenReturn(500);

            pointService.unfreezeAndTransfer(USER_ID, SELLER_ID, ORDER_ID, 500, "订单结算");

            verify(userApi).addPointBalance(SELLER_ID, 500);
            verify(pointTransactionMapper, times(2)).insert(transCaptor.capture());
            assertEquals(2, transCaptor.getAllValues().size());
        }

        @Test
        @DisplayName("退款解冻成功")
        void refundFrozen_success() {
            mockUserBalance(500);

            pointService.refundFrozen(USER_ID, ORDER_ID, 500, "订单取消退款");

            verify(userApi).addPointBalance(USER_ID, 500);
            verify(pointTransactionMapper).insert(transCaptor.capture());
            assertEquals(4, transCaptor.getValue().getType());
            assertEquals(500, transCaptor.getValue().getAmount());
        }
    }

    @Nested
    @DisplayName("扣减与奖励")
    class DeductRewardTests {

        @Test
        @DisplayName("扣减积分成功")
        void deduct_success() {
            mockUserBalance(1000);

            pointService.deduct(USER_ID, 200, "违规扣分");

            verify(userApi).deductPointBalance(USER_ID, 200);
            verify(pointTransactionMapper).insert(transCaptor.capture());
            assertEquals(2, transCaptor.getValue().getType());
            assertEquals(200, transCaptor.getValue().getAmount());
        }

        @Test
        @DisplayName("奖励积分成功")
        void reward_success() {
            mockUserBalance(1000);

            pointService.reward(USER_ID, 100, "系统奖励");

            verify(userApi).addPointBalance(USER_ID, 100);
            verify(pointTransactionMapper).insert(transCaptor.capture());
            assertEquals(5, transCaptor.getValue().getType());
            assertEquals(100, transCaptor.getValue().getAmount());
        }
    }

    @Nested
    @DisplayName("每日签到")
    class SignInTests {

        @Test
        @DisplayName("首次签到成功")
        void signIn_firstTime() {
            when(valueOperations.setIfAbsent(anyString(), eq("1"), eq(25L), eq(TimeUnit.HOURS)))
                    .thenReturn(true);
            when(valueOperations.get("dbs:sign:streak:" + USER_ID)).thenReturn(null);
            doNothing().when(valueOperations).set(anyString(), anyString(), anyLong(), any(TimeUnit.class));
            when(userApi.getPointBalance(USER_ID)).thenReturn(0);

            pointService.signIn(USER_ID);

            verify(userApi).addPointBalance(USER_ID, 10);
        }

        @Test
        @DisplayName("重复签到抛409")
        void signIn_alreadySigned() {
            when(valueOperations.setIfAbsent(anyString(), eq("1"), eq(25L), eq(TimeUnit.HOURS)))
                    .thenReturn(false);

            BusinessException ex = assertThrows(BusinessException.class,
                    () -> pointService.signIn(USER_ID));
            assertEquals(ErrorCode.CONFLICT.getCode(), ex.getCode());
        }

        @Test
        @DisplayName("连续签到7天奖励上限70")
        void signIn_maxReward() {
            when(valueOperations.setIfAbsent(anyString(), eq("1"), eq(25L), eq(TimeUnit.HOURS)))
                    .thenReturn(true);
            when(valueOperations.get("dbs:sign:streak:" + USER_ID)).thenReturn("6");
            doNothing().when(valueOperations).set(anyString(), anyString(), anyLong(), any(TimeUnit.class));
            when(userApi.getPointBalance(USER_ID)).thenReturn(0);

            pointService.signIn(USER_ID);

            verify(userApi).addPointBalance(USER_ID, 70);
        }
    }

    @Nested
    @DisplayName("签到状态")
    class SignInStatusTests {

        @Test
        @DisplayName("今日已签到")
        void hasSignedToday_true() {
            when(redisTemplate.hasKey(anyString())).thenReturn(true);
            assertTrue(pointService.hasSignedToday(USER_ID));
        }

        @Test
        @DisplayName("今日未签到")
        void hasSignedToday_false() {
            when(redisTemplate.hasKey(anyString())).thenReturn(false);
            assertFalse(pointService.hasSignedToday(USER_ID));
        }
    }
}
