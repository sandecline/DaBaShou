import type { PointTransactionType } from '../types/point';

/** 积分交易类型中文映射 */
export const POINT_TRANSACTION_TYPE_MAP: Record<PointTransactionType, string> = {
  1: '收入',
  2: '支出',
  3: '冻结',
  4: '解冻',
  5: '签到奖励',
  6: '系统调整',
};
