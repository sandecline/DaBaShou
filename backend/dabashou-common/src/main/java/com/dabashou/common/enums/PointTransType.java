package com.dabashou.common.enums;

/**
 * 积分流水类型枚举
 */
public enum PointTransType {

    INCOME(1, "收入"),
    EXPENSE(2, "支出"),
    FREEZE(3, "冻结"),
    UNFREEZE(4, "解冻"),
    SYSTEM_REWARD(5, "系统奖励"),
    SYSTEM_DEDUCT(6, "系统扣除");

    private final int code;
    private final String desc;

    PointTransType(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public int getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }
}
