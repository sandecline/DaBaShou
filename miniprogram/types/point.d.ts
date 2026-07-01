/**
 * 积分相关类型定义
 * 对应后端 dabashou-point 模块
 * 与 frontend/src/types/api.ts 保持一致
 */

/** 积分账户/余额（与前端 PointBalanceVo 一致） */
export interface PointAccount {
  userId?: number;
  /** 可用积分 */
  available: number;
  /** 冻结积分 */
  frozen: number;
  /** 总积分 */
  total?: number;
  /** 余额（前端兼容字段） */
  balance?: number;
  /** 累计收入 */
  totalEarned?: number;
  /** 累计支出 */
  totalSpent?: number;
}

/** 积分交易类型 */
export type PointTransactionType = 1 | 2 | 3 | 4 | 5 | 6;

/** 积分交易类型中文（与前端一致：5=签到奖励 6=系统调整） */
export const POINT_TRANSACTION_TYPE_MAP: Record<PointTransactionType, string> = {
  1: '收入',
  2: '支出',
  3: '冻结',
  4: '解冻',
  5: '签到奖励',
  6: '系统调整',
};

/** 积分流水记录（与前端 PointTransVo 一致） */
export interface PointTransaction {
  id: number;
  type: PointTransactionType;
  /** 类型描述 */
  typeDesc?: string;
  /** 变动金额 */
  amount: number;
  /** 变动后余额 */
  balanceAfter: number;
  /** 关联订单 */
  orderId?: number | null;
  /** 订单号 */
  orderNo?: string;
  /** 说明 */
  description: string;
  createTime: string;
}

/** 积分冻结记录 */
export interface PointFreeze {
  orderId: number;
  amount: number;
  status: FreezeStatus;
  createTime: string;
}

export type FreezeStatus = 1 | 2 | 3;
// 1-冻结中  2-已解冻  3-已结算

/** 担保池记录 */
export interface GuaranteePool {
  orderId: number;
  amount: number;
  status: GuaranteeStatus;
  createTime: string;
}

export type GuaranteeStatus = 1 | 2 | 3;
// 1-担保中  2-已结算  3-已退还
