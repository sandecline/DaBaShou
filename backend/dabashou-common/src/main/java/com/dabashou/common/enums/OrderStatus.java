package com.dabashou.common.enums;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 订单状态枚举（6状态，核销码驱动）
 *
 * 状态机:
 * 0-已取消(终态)
 * 1-待核销 → 3, 0
 * 3-服务中 → 5, 6, 7, 0
 * 5-已完成(终态) → 7
 * 6-已退款 → 0
 * 7-争议中 → 5, 6
 *
 * 核销流程:
 * 1→3: 双方输入对方的核销码（开始核销码），冻结买方积分
 * 3→5: 双方输入对方的确认码（完成确认码），结算积分
 * 3/5→7: 任一方发起争议
 * 3→6: 双方同意退款（需对方同意）
 */
public enum OrderStatus {

    CANCELLED(0, "已取消"),
    PENDING_PAYMENT(1, "待核销"),
    PAID(2, "已支付(担保中)"),      // 保留兼容，新流程不再使用
    IN_SERVICE(3, "服务中"),
    PENDING_CONFIRM(4, "待确认"),    // 保留兼容，新流程不再使用
    COMPLETED(5, "已完成"),
    REFUNDED(6, "已退款"),
    DISPUTING(7, "争议中");

    private static final Map<Integer, List<Integer>> TRANSITIONS = Map.of(
        1, Arrays.asList(3, 0),
        3, Arrays.asList(5, 6, 7, 0),
        5, Arrays.asList(7),
        6, Arrays.asList(0),
        7, Arrays.asList(5, 6)
    );

    private static final Map<Integer, OrderStatus> CODE_MAP =
        Arrays.stream(values()).collect(Collectors.toMap(OrderStatus::getCode, s -> s));

    private final int code;
    private final String desc;

    OrderStatus(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public int getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }

    /**
     * 校验状态流转是否合法
     *
     * @param from 当前状态码
     * @param to   目标状态码
     * @return true-合法，false-非法
     */
    public static boolean canTransitTo(int from, int to) {
        List<Integer> allowed = TRANSITIONS.getOrDefault(from, Collections.emptyList());
        return allowed.contains(to);
    }

    /**
     * 根据code获取枚举
     */
    public static OrderStatus ofCode(int code) {
        OrderStatus status = CODE_MAP.get(code);
        if (status == null) {
            throw new IllegalArgumentException("未知的订单状态码: " + code);
        }
        return status;
    }
}
