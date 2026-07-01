/**
 * 信用评价服务
 * 对应后端 dabashou-credit 模块
 * 与 frontend/src/api/credit.ts 对齐
 */

import { api } from '../utils/request';
import type { PageResult } from '../types/api-response';

export interface ReviewResult {
  id: number;
  orderId: number;
  orderTitle?: string;
  rating: number;
  content: string;
  images: string[];
  isAnonymous: number;
  reviewerId: number;
  reviewerNickname: string;
  reviewerAvatar: string;
  createTime: string;
}

export interface Violation {
  id: number;
  targetUserId: number;
  targetNickname: string;
  orderId: number | null;
  type: number;
  typeDesc: string;
  reason: string;
  evidence: string[];
  status: number;
  statusDesc: string;
  handleResult: string | null;
  createTime: string;
}

export interface Appeal {
  id: number;
  violationId: number;
  reason: string;
  evidence: string[];
  status: number;
  statusDesc: string;
  reviewRemark: string | null;
  createTime: string;
}

export const creditService = {
  // ===== 评价 =====

  /** 获取我发出的评价 */
  getSentReviews(params: { pageNum: number; pageSize: number }) {
    return api.get<PageResult<ReviewResult>>('/v1/reviews/mine', params as unknown as Record<string, unknown>);
  },

  /** 获取我收到的评价 */
  getReceivedReviews(params: { pageNum: number; pageSize: number }) {
    return api.get<PageResult<ReviewResult>>('/v1/reviews/received', params as unknown as Record<string, unknown>);
  },

  /** 提交评价 */
  submitReview(params: {
    orderId: number;
    rating: number;
    content: string;
    images?: string[];
    isAnonymous?: boolean;
  }) {
    return api.post<{ id: number }>('/v1/reviews', params as unknown as Record<string, unknown>);
  },

  /** 获取需要评价的订单列表 */
  getPendingReviewOrders() {
    return api.get<Array<{ orderId: number; orderTitle: string; targetUser: { id: number; nickname: string } }>>('/v1/reviews/pending');
  },

  // ===== 违规举报 =====

  /** 举报违规 */
  reportViolation(params: {
    targetUserId: number;
    orderId?: number;
    type: number;
    reason: string;
    evidence?: string[];
  }) {
    return api.post<{ id: number }>('/v1/violations', params as unknown as Record<string, unknown>);
  },

  /** 获取我的违规记录 */
  getMyViolations(params: { pageNum: number; pageSize: number }) {
    return api.get<PageResult<Violation>>('/v1/violations/mine', params as unknown as Record<string, unknown>);
  },

  // ===== 申诉 =====

  /** 提交申诉 */
  submitAppeal(params: { violationId: number; reason: string; evidence?: string[] }) {
    return api.post<{ id: number }>('/v1/appeals', params as unknown as Record<string, unknown>);
  },

  /** 获取我的申诉记录 */
  getMyAppeals(params: { pageNum: number; pageSize: number }) {
    return api.get<PageResult<Appeal>>('/v1/appeals/mine', params as unknown as Record<string, unknown>);
  },
};
