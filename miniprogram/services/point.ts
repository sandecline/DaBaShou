/**
 * 积分服务
 * 对应后端 dabashou-point 模块
 * 与 frontend/src/api/point.ts 对齐
 */

import { api } from '../utils/request';
import type { PageResult } from '../types/api-response';

export interface PointBalance {
  available: number;
  frozen: number;
  total: number;
  balance: number;
}

export interface PointTransaction {
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

export const pointService = {
  /** 获取积分余额 */
  getBalance() {
    return api.get<PointBalance>('/v1/points/balance');
  },

  /** 获取积分流水 */
  getTransactions(params: {
    pageNum: number;
    pageSize: number;
    type?: number;
    orderId?: number;
    startDate?: string;
    endDate?: string;
  }) {
    return api.get<PageResult<PointTransaction>>(
      '/v1/points/transactions',
      params as unknown as Record<string, unknown>
    );
  },

  /** 获取积分流水详情 */
  getTransactionDetail(id: number) {
    return api.get<PointTransaction>(`/v1/points/transactions/${id}`);
  },

  /** 获取积分统计 */
  getPointStats() {
    return api.get<{ totalIncome: number; totalExpense: number; monthIncome: number; monthExpense: number }>('/v1/points/stats');
  },

  /** 签到 */
  signIn() {
    return api.post<{ reward: number; consecutiveDays: number }>('/v1/points/sign-in');
  },

  /** 获取签到状态 */
  getSignInStatus() {
    return api.get<{ todaySigned: boolean; consecutiveDays: number; reward: number }>('/v1/points/sign-in/status');
  },

  /** 获取担保池信息 */
  getGuaranteePool() {
    return api.get<{ totalPool: number; frozenAmount: number; availableAmount: number }>('/v1/points/guarantee-pool');
  },
};
