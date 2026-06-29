package com.dabashou.order.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.dabashou.common.enums.ErrorCode;
import com.dabashou.common.enums.OrderStatus;
import com.dabashou.common.exception.BusinessException;
import com.dabashou.demand.api.DemandApi;
import com.dabashou.order.domain.Order;
import com.dabashou.order.dto.*;
import com.dabashou.order.mapper.OrderMapper;
import com.dabashou.order.vo.*;
import com.dabashou.point.api.PointApi;
import com.dabashou.shelf.api.ShelfApi;
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

import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceImplTest {

    @InjectMocks
    private OrderServiceImpl orderService;

    @Mock
    private StringRedisTemplate redisTemplate;
    @Mock
    private ShelfApi shelfApi;
    @Mock
    private DemandApi demandApi;
    @Mock
    private PointApi pointApi;
    @Mock
    private UserApi userApi;
    @Mock
    private OrderMapper orderMapper;
    @Mock
    private ValueOperations<String, String> valueOperations;

    private static final Long BUYER_ID = 1L;
    private static final Long SELLER_ID = 2L;
    private static final Long SHELF_ID = 100L;
    private static final Long DEMAND_ID = 200L;
    private static final Long ORDER_ID = 1L;
    private static final Long TAG_ID = 10L;
    private static final int POINT_AMOUNT = 500;
    private static final String ORDER_NO = "DBS1234567890ABCD";

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(orderService, "baseMapper", orderMapper);
        lenient().when(redisTemplate.opsForValue()).thenReturn(valueOperations);
    }

    private Order buildOrder(int status) {
        Order order = new Order();
        order.setId(ORDER_ID);
        order.setOrderNo(ORDER_NO);
        order.setBuyerId(BUYER_ID);
        order.setSellerId(SELLER_ID);
        order.setSkillShelfId(SHELF_ID);
        order.setSkillTagId(TAG_ID);
        order.setTitle("测试服务");
        order.setPointAmount(POINT_AMOUNT);
        order.setStatus(status);
        order.setCreateTime(LocalDateTime.now());
        order.setUpdateTime(LocalDateTime.now());
        return order;
    }

    @Nested
    @DisplayName("创建订单")
    class CreateOrderTests {

        @Test
        @DisplayName("从货架创建订单成功")
        void createOrderFromShelf_success() {
            when(shelfApi.isOnShelf(SHELF_ID)).thenReturn(true);
            when(shelfApi.getUserId(SHELF_ID)).thenReturn(SELLER_ID);
            when(shelfApi.getPointPrice(SHELF_ID)).thenReturn(POINT_AMOUNT);
            when(shelfApi.getSkillTagId(SHELF_ID)).thenReturn(TAG_ID);
            when(shelfApi.getTitle(SHELF_ID)).thenReturn("Java辅导");
            doAnswer(invocation -> {
                Order order = invocation.getArgument(0);
                order.setId(99L);
                return 1;
            }).when(orderMapper).insert((Order) any());

            CreateOrderDto dto = new CreateOrderDto();
            dto.setSkillShelfId(SHELF_ID);

            Long orderId = orderService.createOrderFromShelf(BUYER_ID, dto);

            assertNotNull(orderId);
            verify(pointApi, never()).freeze(anyLong(), anyLong(), anyInt(), anyString());
        }

        @Test
        @DisplayName("货架不存在时抛404")
        void createOrderFromShelf_notOnShelf() {
            when(shelfApi.isOnShelf(SHELF_ID)).thenReturn(false);
            CreateOrderDto dto = new CreateOrderDto();
            dto.setSkillShelfId(SHELF_ID);

            BusinessException ex = assertThrows(BusinessException.class,
                    () -> orderService.createOrderFromShelf(BUYER_ID, dto));
            assertEquals(ErrorCode.NOT_FOUND.getCode(), ex.getCode());
        }

        @Test
        @DisplayName("购买自己服务抛409")
        void createOrderFromShelf_selfBuy() {
            when(shelfApi.isOnShelf(SHELF_ID)).thenReturn(true);
            when(shelfApi.getUserId(SHELF_ID)).thenReturn(BUYER_ID);
            CreateOrderDto dto = new CreateOrderDto();
            dto.setSkillShelfId(SHELF_ID);

            BusinessException ex = assertThrows(BusinessException.class,
                    () -> orderService.createOrderFromShelf(BUYER_ID, dto));
            assertEquals(ErrorCode.CONFLICT.getCode(), ex.getCode());
        }

        @Test
        @DisplayName("从需求创建订单成功")
        void createOrderFromDemand_success() {
            when(demandApi.getPointReward(DEMAND_ID)).thenReturn(POINT_AMOUNT);
            when(demandApi.getSkillTagId(DEMAND_ID)).thenReturn(TAG_ID);
            when(demandApi.getTitle(DEMAND_ID)).thenReturn("需要辅导");
            when(demandApi.getUserId(DEMAND_ID)).thenReturn(BUYER_ID);
            doAnswer(invocation -> {
                Order order = invocation.getArgument(0);
                order.setId(99L);
                return 1;
            }).when(orderMapper).insert((Order) any());

            CreateOrderFromDemandDto dto = new CreateOrderFromDemandDto();
            dto.setDemandId(DEMAND_ID);

            Long orderId = orderService.createOrderFromDemand(SELLER_ID, dto);

            assertNotNull(orderId);
            verify(demandApi).updateStatus(DEMAND_ID, 2);
        }

        @Test
        @DisplayName("接自己需求抛409")
        void createOrderFromDemand_selfAccept() {
            when(demandApi.getPointReward(DEMAND_ID)).thenReturn(POINT_AMOUNT);
            when(demandApi.getSkillTagId(DEMAND_ID)).thenReturn(TAG_ID);
            when(demandApi.getTitle(DEMAND_ID)).thenReturn("需要辅导");
            when(demandApi.getUserId(DEMAND_ID)).thenReturn(BUYER_ID);
            CreateOrderFromDemandDto dto = new CreateOrderFromDemandDto();
            dto.setDemandId(DEMAND_ID);

            BusinessException ex = assertThrows(BusinessException.class,
                    () -> orderService.createOrderFromDemand(BUYER_ID, dto));
            assertEquals(ErrorCode.CONFLICT.getCode(), ex.getCode());
        }
    }

    @Nested
    @DisplayName("支付订单")
    class PayOrderTests {

        @Test
        @DisplayName("支付成功返回核销码")
        void payOrder_success() {
            when(valueOperations.setIfAbsent(eq("dbs:idem:token123"), eq("1"), eq(5L), eq(TimeUnit.MINUTES)))
                    .thenReturn(true);
            when(orderMapper.selectById(ORDER_ID)).thenReturn(buildOrder(OrderStatus.PENDING_PAYMENT.getCode()));
            when(orderMapper.updateById((Order) any())).thenReturn(1);

            PayResultVo result = orderService.payOrder(BUYER_ID, ORDER_ID, "token123");

            assertNotNull(result.getVerifyCode());
            assertEquals(6, result.getVerifyCode().length());
            verify(pointApi).freeze(BUYER_ID, ORDER_ID, POINT_AMOUNT, "订单支付冻结: " + ORDER_NO);
            verify(orderMapper).updateById((Order) any());
        }

        @Test
        @DisplayName("重复支付抛409")
        void payOrder_idempotentDuplicate() {
            when(valueOperations.setIfAbsent(anyString(), anyString(), anyLong(), any(TimeUnit.class)))
                    .thenReturn(false);

            BusinessException ex = assertThrows(BusinessException.class,
                    () -> orderService.payOrder(BUYER_ID, ORDER_ID, "token123"));
            assertEquals(ErrorCode.CONFLICT.getCode(), ex.getCode());
        }

        @Test
        @DisplayName("非买家支付抛403")
        void payOrder_notBuyer() {
            when(valueOperations.setIfAbsent(anyString(), anyString(), anyLong(), any(TimeUnit.class)))
                    .thenReturn(true);
            when(orderMapper.selectById(ORDER_ID)).thenReturn(buildOrder(OrderStatus.PENDING_PAYMENT.getCode()));

            BusinessException ex = assertThrows(BusinessException.class,
                    () -> orderService.payOrder(SELLER_ID, ORDER_ID, "token123"));
            assertEquals(ErrorCode.FORBIDDEN.getCode(), ex.getCode());
        }

        @Test
        @DisplayName("已支付订单重复支付抛409")
        void payOrder_alreadyPaid() {
            when(valueOperations.setIfAbsent(anyString(), anyString(), anyLong(), any(TimeUnit.class)))
                    .thenReturn(true);
            when(orderMapper.selectById(ORDER_ID)).thenReturn(buildOrder(OrderStatus.PAID.getCode()));

            BusinessException ex = assertThrows(BusinessException.class,
                    () -> orderService.payOrder(BUYER_ID, ORDER_ID, "token123"));
            assertEquals(ErrorCode.CONFLICT.getCode(), ex.getCode());
        }
    }

    @Nested
    @DisplayName("状态机流转")
    class StateTransitionTests {

        @Test
        @DisplayName("开始服务成功(2→3)")
        void startService_success() {
            when(orderMapper.selectById(ORDER_ID)).thenReturn(buildOrder(OrderStatus.PAID.getCode()));
            when(orderMapper.updateById((Order) any())).thenReturn(1);

            orderService.startService(SELLER_ID, ORDER_ID);

            verify(orderMapper).updateById((Order) any());
        }

        @Test
        @DisplayName("非卖家开始服务抛403")
        void startService_notSeller() {
            when(orderMapper.selectById(ORDER_ID)).thenReturn(buildOrder(OrderStatus.PAID.getCode()));

            assertThrows(BusinessException.class,
                    () -> orderService.startService(BUYER_ID, ORDER_ID));
        }

        @Test
        @DisplayName("待支付开始服务抛409")
        void startService_invalidTransition() {
            when(orderMapper.selectById(ORDER_ID)).thenReturn(buildOrder(OrderStatus.PENDING_PAYMENT.getCode()));

            BusinessException ex = assertThrows(BusinessException.class,
                    () -> orderService.startService(SELLER_ID, ORDER_ID));
            assertEquals(ErrorCode.CONFLICT.getCode(), ex.getCode());
        }

        @Test
        @DisplayName("核销成功(3→4)")
        void verifyOrder_success() {
            when(orderMapper.selectById(ORDER_ID)).thenReturn(buildOrder(OrderStatus.IN_SERVICE.getCode()));
            when(valueOperations.get("dbs:verify:" + ORDER_ID)).thenReturn("123456");
            when(orderMapper.updateById((Order) any())).thenReturn(1);

            VerifyDto dto = new VerifyDto();
            dto.setCode("123456");

            orderService.verifyOrder(SELLER_ID, ORDER_ID, dto);

            verify(redisTemplate).delete("dbs:verify:" + ORDER_ID);
            verify(orderMapper).updateById((Order) any());
        }

        @Test
        @DisplayName("核销码错误抛400")
        void verifyOrder_wrongCode() {
            when(orderMapper.selectById(ORDER_ID)).thenReturn(buildOrder(OrderStatus.IN_SERVICE.getCode()));
            when(valueOperations.get("dbs:verify:" + ORDER_ID)).thenReturn("123456");

            VerifyDto dto = new VerifyDto();
            dto.setCode("000000");

            BusinessException ex = assertThrows(BusinessException.class,
                    () -> orderService.verifyOrder(SELLER_ID, ORDER_ID, dto));
            assertEquals(ErrorCode.BAD_REQUEST.getCode(), ex.getCode());
        }

        @Test
        @DisplayName("核销码过期抛409")
        void verifyOrder_codeExpired() {
            when(orderMapper.selectById(ORDER_ID)).thenReturn(buildOrder(OrderStatus.IN_SERVICE.getCode()));
            when(valueOperations.get("dbs:verify:" + ORDER_ID)).thenReturn(null);

            VerifyDto dto = new VerifyDto();
            dto.setCode("123456");

            BusinessException ex = assertThrows(BusinessException.class,
                    () -> orderService.verifyOrder(SELLER_ID, ORDER_ID, dto));
            assertEquals(ErrorCode.CONFLICT.getCode(), ex.getCode());
        }

        @Test
        @DisplayName("买家确认完成(4→5)")
        void confirmOrder_success() {
            when(orderMapper.selectById(ORDER_ID)).thenReturn(buildOrder(OrderStatus.PENDING_CONFIRM.getCode()));
            when(orderMapper.updateById((Order) any())).thenReturn(1);

            orderService.confirmOrder(BUYER_ID, ORDER_ID);

            verify(pointApi).unfreezeAndTransfer(BUYER_ID, SELLER_ID, ORDER_ID, POINT_AMOUNT, "订单结算: " + ORDER_NO);
            verify(orderMapper).updateById((Order) any());
        }

        @Test
        @DisplayName("发起争议(4→7)")
        void disputeOrder_success() {
            when(orderMapper.selectById(ORDER_ID)).thenReturn(buildOrder(OrderStatus.PENDING_CONFIRM.getCode()));
            when(orderMapper.updateById((Order) any())).thenReturn(1);

            DisputeDto dto = new DisputeDto();
            dto.setReason("服务不满意");

            orderService.disputeOrder(BUYER_ID, ORDER_ID, dto);

            verify(orderMapper).updateById((Order) any());
        }

        @Test
        @DisplayName("已完成订单不能争议")
        void disputeOrder_completedOrder() {
            when(orderMapper.selectById(ORDER_ID)).thenReturn(buildOrder(OrderStatus.COMPLETED.getCode()));

            DisputeDto dto = new DisputeDto();
            dto.setReason("太晚了");

            assertThrows(BusinessException.class,
                    () -> orderService.disputeOrder(BUYER_ID, ORDER_ID, dto));
        }
    }

    @Nested
    @DisplayName("取消订单")
    class CancelOrderTests {

        @Test
        @DisplayName("待支付取消成功(1→0)")
        void cancelOrder_pendingPayment() {
            when(orderMapper.selectById(ORDER_ID)).thenReturn(buildOrder(OrderStatus.PENDING_PAYMENT.getCode()));
            when(orderMapper.updateById((Order) any())).thenReturn(1);

            CancelDto dto = new CancelDto();
            dto.setReason("不想买了");

            orderService.cancelOrder(BUYER_ID, ORDER_ID, dto);

            verify(pointApi, never()).refundFrozen(anyLong(), anyLong(), anyInt(), anyString());
            verify(orderMapper).updateById((Order) any());
        }

        @Test
        @DisplayName("已支付取消退款解冻(2→0)")
        void cancelOrder_paidRefund() {
            when(orderMapper.selectById(ORDER_ID)).thenReturn(buildOrder(OrderStatus.PAID.getCode()));
            when(orderMapper.updateById((Order) any())).thenReturn(1);

            CancelDto dto = new CancelDto();
            dto.setReason("有事去不了");

            orderService.cancelOrder(BUYER_ID, ORDER_ID, dto);

            verify(pointApi).refundFrozen(BUYER_ID, ORDER_ID, POINT_AMOUNT, "订单取消退款: " + ORDER_NO);
        }
    }

    @Nested
    @DisplayName("仲裁")
    class ArbitrateTests {

        @Test
        @DisplayName("仲裁退款(7→6)")
        void arbitrateOrder_refund() {
            when(orderMapper.selectById(ORDER_ID)).thenReturn(buildOrder(OrderStatus.DISPUTING.getCode()));
            when(orderMapper.updateById((Order) any())).thenReturn(1);

            ArbitrateDto dto = new ArbitrateDto();
            dto.setRefundAmount(300);

            orderService.arbitrateOrder(ORDER_ID, dto);

            verify(pointApi).refundFrozen(BUYER_ID, ORDER_ID, 300, "仲裁退款: " + ORDER_NO);
            verify(pointApi).reward(SELLER_ID, 200, "仲裁结算: " + ORDER_NO);
            verify(orderMapper).updateById((Order) any());
        }

        @Test
        @DisplayName("仲裁全额结算(7→5)")
        void arbitrateOrder_fullSettlement() {
            when(orderMapper.selectById(ORDER_ID)).thenReturn(buildOrder(OrderStatus.DISPUTING.getCode()));
            when(orderMapper.updateById((Order) any())).thenReturn(1);

            ArbitrateDto dto = new ArbitrateDto();

            orderService.arbitrateOrder(ORDER_ID, dto);

            verify(pointApi).unfreezeAndTransfer(BUYER_ID, SELLER_ID, ORDER_ID, POINT_AMOUNT, "仲裁结算: " + ORDER_NO);
            verify(orderMapper).updateById((Order) any());
        }

        @Test
        @DisplayName("非争议状态不能仲裁")
        void arbitrateOrder_notDisputing() {
            when(orderMapper.selectById(ORDER_ID)).thenReturn(buildOrder(OrderStatus.PAID.getCode()));

            ArbitrateDto dto = new ArbitrateDto();

            assertThrows(BusinessException.class,
                    () -> orderService.arbitrateOrder(ORDER_ID, dto));
        }
    }

    @Nested
    @DisplayName("退款")
    class RefundTests {

        @Test
        @DisplayName("退款成功(2→6)")
        void refundOrder_success() {
            when(orderMapper.selectById(ORDER_ID)).thenReturn(buildOrder(OrderStatus.PAID.getCode()));
            when(orderMapper.updateById((Order) any())).thenReturn(1);

            RefundDto dto = new RefundDto();
            dto.setReason("不想要了");

            orderService.refundOrder(BUYER_ID, ORDER_ID, dto);

            verify(pointApi).refundFrozen(BUYER_ID, ORDER_ID, POINT_AMOUNT, "订单退款: " + ORDER_NO);
        }

        @Test
        @DisplayName("非买家退款抛403")
        void refundOrder_notBuyer() {
            when(orderMapper.selectById(ORDER_ID)).thenReturn(buildOrder(OrderStatus.PAID.getCode()));

            RefundDto dto = new RefundDto();
            dto.setReason("测试");

            assertThrows(BusinessException.class,
                    () -> orderService.refundOrder(SELLER_ID, ORDER_ID, dto));
        }

        @Test
        @DisplayName("已完成订单不能退款")
        void refundOrder_completed() {
            when(orderMapper.selectById(ORDER_ID)).thenReturn(buildOrder(OrderStatus.COMPLETED.getCode()));

            RefundDto dto = new RefundDto();
            dto.setReason("太晚了");

            assertThrows(BusinessException.class,
                    () -> orderService.refundOrder(BUYER_ID, ORDER_ID, dto));
        }
    }

    @Nested
    @DisplayName("查询")
    class QueryTests {

        @Test
        @DisplayName("订单不存在抛404")
        void getOrderDetail_notFound() {
            when(orderMapper.selectById(999L)).thenReturn(null);

            assertThrows(BusinessException.class,
                    () -> orderService.getOrderDetail(BUYER_ID, 999L));
        }

        @Test
        @DisplayName("非参与者查看抛403")
        void getOrderDetail_notParticipant() {
            when(orderMapper.selectById(ORDER_ID)).thenReturn(buildOrder(OrderStatus.PAID.getCode()));

            assertThrows(BusinessException.class,
                    () -> orderService.getOrderDetail(999L, ORDER_ID));
        }

        @Test
        @DisplayName("获取核销码成功")
        void getVerifyCode_success() {
            Order order = buildOrder(OrderStatus.PAID.getCode());
            order.setVerifyCode("123456");
            order.setVerifyCodeExpire(LocalDateTime.now().plusMinutes(30));
            when(orderMapper.selectById(ORDER_ID)).thenReturn(order);

            VerifyCodeVo vo = orderService.getVerifyCode(BUYER_ID, ORDER_ID);

            assertEquals("123456", vo.getVerifyCode());
        }
    }

    @Nested
    @DisplayName("状态机校验")
    class StatusTransitionTests {

        @Test
        @DisplayName("所有合法流转路径")
        void validTransitions() {
            assertTrue(OrderStatus.canTransitTo(1, 2));
            assertTrue(OrderStatus.canTransitTo(1, 0));
            assertTrue(OrderStatus.canTransitTo(2, 3));
            assertTrue(OrderStatus.canTransitTo(2, 6));
            assertTrue(OrderStatus.canTransitTo(3, 4));
            assertTrue(OrderStatus.canTransitTo(4, 5));
            assertTrue(OrderStatus.canTransitTo(4, 7));
            assertTrue(OrderStatus.canTransitTo(7, 5));
            assertTrue(OrderStatus.canTransitTo(7, 6));
            assertTrue(OrderStatus.canTransitTo(6, 0));
        }

        @Test
        @DisplayName("所有非法流转路径")
        void invalidTransitions() {
            assertFalse(OrderStatus.canTransitTo(0, 1));
            assertFalse(OrderStatus.canTransitTo(5, 1));
            assertFalse(OrderStatus.canTransitTo(2, 5));
            assertFalse(OrderStatus.canTransitTo(1, 5));
            assertFalse(OrderStatus.canTransitTo(3, 2));
            assertFalse(OrderStatus.canTransitTo(7, 1));
        }
    }
}
