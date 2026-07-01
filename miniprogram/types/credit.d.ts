/**
 * 信用评价相关类型定义
 * 对应后端 dabashou-credit 模块
 * 与 frontend/src/types/credit.ts + api.ts 保持一致
 */

/** 评价（与前端 ReviewVo 一致） */
export interface Review {
  id: number;
  orderId: number;
  orderTitle?: string;
  rating: number; // 1-5
  content: string;
  images: string[];
  isAnonymous: number | boolean;
  reviewerId: number;
  reviewerNickname?: string;
  reviewerAvatar?: string;
  revieweeId?: number;
  revieweeName?: string;
  createTime: string;
}

/** 用户信任分记录（与前端 TrustLogItem 一致） */
export interface TrustScoreLog {
  id: number;
  userId?: number;
  type?: string;
  scoreBefore: number;
  scoreAfter: number;
  scoreChange?: number;
  delta?: number;
  reason: string;
  createTime: string;
}

/** 违规记录（与前端 ViolationVo 一致 — 数字类型键） */
export interface CreditViolation {
  id: number;
  targetUserId?: number;
  targetNickname?: string;
  userId?: number;
  /** 违规类型（数字键，与前端 api.ts 一致：1=虚假服务 2=态度恶劣 3=违规交易 4=其他） */
  type: number;
  typeDesc?: string;
  /** 扣分 */
  penaltyScore?: number;
  /** 描述 */
  description?: string;
  reason?: string;
  evidence?: string[];
  /** 举报人 */
  reporterId?: number;
  /** 状态 */
  status: number;
  statusDesc?: string;
  handleResult?: string | null;
  createTime: string;
}

/** 申诉记录 */
export interface CreditAppeal {
  id: number;
  violationId: number;
  appellantId?: number;
  reason: string;
  evidence?: string[];
  status: number;
  statusDesc?: string;
  reviewRemark?: string | null;
  result?: string;
  createTime: string;
}

export type AppealStatus = 0 | 1 | 2;
// 0-待审核  1-已通过  2-已驳回

/** 提交评价参数 */
export interface SubmitReviewParams {
  orderId: number;
  rating: number;
  content: string;
  images?: string[];
  isAnonymous?: boolean | number;
}
