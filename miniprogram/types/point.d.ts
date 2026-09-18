export type PointTransactionType = 1 | 2 | 3 | 4 | 5 | 6 | 7;

export interface PointBalanceVo {
  available: number;
  frozen: number;
  total: number;
}

export interface PointTransVo {
  id: number;
  type: number;
  typeDesc: string;
  amount: number;
  balanceAfter: number;
  orderId: number | null;
  orderNo: string;
  description: string;
  createTime: string;
}

export interface PointStatsVo {
  totalIncome: number;
  totalExpense: number;
  monthIncome: number;
  monthExpense: number;
}

export interface SignInVo {
  todaySigned: boolean;
  reward: number;
  consecutiveDays: number;
}

export interface GuaranteePoolVo {
  totalPool: number;
  frozenAmount: number;
  availableAmount: number;
}

export type { PointBalanceVo as PointAccount };
export type { PointTransVo as PointTransaction };
