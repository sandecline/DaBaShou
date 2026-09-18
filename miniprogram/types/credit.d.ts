export interface ReviewVo {
  id: number;
  orderId: number;
  reviewerId: number;
  reviewerNickname: string;
  reviewerAvatar: string;
  revieweeId: number;
  revieweeNickname: string;
  revieweeAvatar: string;
  rating: number;
  content: string;
  images: string;
  isAnonymous: number;
  createTime: string;
}

export interface ViolationVo {
  id: number;
  userId: number;
  orderId: number | null;
  type: string;
  description: string;
  penaltyScore: number;
  reporterId: number;
  reporterNickname: string;
  status: number;
  createTime: string;
}

export interface AppealVo {
  id: number;
  violationId: number;
  violationType: string;
  reason: string;
  evidenceFileId: string | null;
  status: number;
  statusDesc: string;
  reviewRemark: string | null;
  reviewTime: string | null;
  createTime: string;
}

export interface SubmitReviewParams {
  orderId: number;
  rating: number;
  content?: string;
  images?: string[];
  isAnonymous?: boolean;
}

export type { ReviewVo as Review };
export type { ViolationVo as Violation };
export type { AppealVo as Appeal };
