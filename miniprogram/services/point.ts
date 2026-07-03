import { api } from '../utils/request';
import type { PageResult } from '../types/api-response';
import type { PointBalanceVo, PointTransVo, PointStatsVo, SignInVo, GuaranteePoolVo } from '../types/point';

export const pointService = {
  getBalance() {
    return api.get<PointBalanceVo>('/v1/points/balance');
  },

  getTransactions(params: {
    pageNum: number;
    pageSize: number;
    type?: string;
    orderId?: number;
    startDate?: string;
    endDate?: string;
  }) {
    return api.get<PageResult<PointTransVo>>(
      '/v1/points/transactions',
      params as unknown as Record<string, unknown>
    );
  },

  getTransactionDetail(id: number) {
    return api.get<PointTransVo>(`/v1/points/transactions/${id}`);
  },

  getPointStats() {
    return api.get<PointStatsVo>('/v1/points/stats');
  },

  signIn() {
    return api.post<SignInVo>('/v1/points/sign-in');
  },

  getSignInStatus() {
    return api.get<SignInVo>('/v1/points/sign-in/status');
  },

  getGuaranteePool() {
    return api.get<GuaranteePoolVo>('/v1/points/guarantee-pool');
  },
};
