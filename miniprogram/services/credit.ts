import { api } from '../utils/request';
import type { PageResult } from '../types/api-response';
import type { ReviewVo, ViolationVo, AppealVo } from '../types/credit';

export const creditService = {
  getSentReviews(params: { pageNum: number; pageSize: number }) {
    return api.get<PageResult<ReviewVo>>('/v1/reviews/mine', params as unknown as Record<string, unknown>);
  },

  getReceivedReviews(params: { pageNum: number; pageSize: number }) {
    return api.get<PageResult<ReviewVo>>('/v1/reviews/received', params as unknown as Record<string, unknown>);
  },

  getUserReviews(userId: number, params: { pageNum: number; pageSize: number }) {
    return api.get<PageResult<ReviewVo>>('/v1/reviews/received', { ...params, userId } as unknown as Record<string, unknown>);
  },

  submitReview(params: {
    orderId: number;
    rating: number;
    content?: string;
    images?: string[];
    isAnonymous?: boolean;
  }) {
    return api.post<{ id: number }>('/v1/reviews', params as unknown as Record<string, unknown>);
  },

  getPendingReviewOrders() {
    return api.get<Array<{ orderId: number; orderTitle: string; targetUser: { id: number; nickname: string } }>>('/v1/reviews/pending');
  },

  reportViolation(params: {
    targetUserId: number;
    orderId?: number;
    type: string;
    reason: string;
    evidence?: string[];
  }) {
    return api.post<{ id: number }>('/v1/violations', params as unknown as Record<string, unknown>);
  },

  getMyViolations(params: { pageNum: number; pageSize: number }) {
    return api.get<PageResult<ViolationVo>>('/v1/violations/mine', params as unknown as Record<string, unknown>);
  },

  submitAppeal(params: { violationId: number; reason: string; evidence?: string[] }) {
    return api.post<{ id: number }>('/v1/appeals', params as unknown as Record<string, unknown>);
  },

  getMyAppeals(params: { pageNum: number; pageSize: number }) {
    return api.get<PageResult<AppealVo>>('/v1/appeals/mine', params as unknown as Record<string, unknown>);
  },

  /**
   * 订单申诉 — 复用 POST /v1/appeals 接口
   * 只传 orderId，后端通过 orderId 识别为订单申诉
   */
  submitOrderAppeal(params: { orderId: number; reason: string; evidence?: string[] }) {
    return api.post<{ id: number }>('/v1/appeals', {
      orderId: params.orderId,
      reason: params.reason,
      evidence: params.evidence,
    } as unknown as Record<string, unknown>);
  },
};
