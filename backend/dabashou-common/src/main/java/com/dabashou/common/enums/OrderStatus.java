package com.dabashou.common.enums;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 订单状态枚举（8状态，不可跳变）
 *
 * 状态机:
 * 0-已取消(终态)
 * 1-待支付 → 2, 0
 * 2-已支付(担保中) → 3, 6, 0
 * 3-服务中 → 4, 6, 0
 * 4-待确认 → 5, 7, 6
 * 5-已完成(终态)
 * 6-已退款 → 0
 * 7-争议中 → 5, 6
 */
public enum OrderStatus {

    CANCELLED(0, "已取消"),
    PENDING_PAYMENT(1, "待支付"),
    PAID(2, "已支付(担保中)"),
    IN_SERVICE(3, "服务中"),
    PENDING_CONFIRM(4, "待确认"),
    COMPLETED(5, "已完成"),
    REFUNDED(6, "已退款"),
    DISPUTING(7, "争议中");

    private static final Map<Integer, List<Integer>> TRANSITIONS = Map.of(
        1, Arrays.asList(2, 0),
        2, Arrays.asList(3, 6, 0),
        3, Arrays.asList(4, 6, 0),
        4, Arrays.asList(5, 7, 6),
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
