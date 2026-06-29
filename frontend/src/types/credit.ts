/** 评价 */
export interface Review {
  id: number
  orderId: number
  reviewerId: number
  revieweeId: number
  rating: 1 | 2 | 3 | 4 | 5
  content: string
  createTime: string
  // 前端附加字段
  reviewerName?: string
  reviewerAvatar?: string
  revieweeName?: string
  orderTitle?: string
}

/** 违规记录 */
export interface Violation {
  id: number
  userId: number
  orderId: number
  type: ViolationType
  description: string
  status: 0 | 1 // 0-未处理 1-已处理
  penaltyPoints: number
  createTime: string
  // 前端附加字段
  userName?: string
}

export type ViolationType = 'late' | 'no_show' | 'bad_review' | 'malicious_confirm'

export const ViolationTypeMap: Record<ViolationType, string> = {
  late: '迟到',
  no_show: '放鸽子',
  bad_review: '差评',
  malicious_confirm: '恶意确认',
}

/** 申诉 */
export interface Appeal {
  id: number
  userId: number
  violationId: number
  reason: string
  evidence: string
  status: 0 | 1 | 2 // 0-待审核 1-已通过 2-已驳回
  reply: string
  createTime: string
  updateTime: string
  // 前端附加字段
  userName?: string
  violationDesc?: string
}

/** 评价表单 */
export interface ReviewForm {
  orderId: number
  rating: number
  content: string
}

/** 申诉表单 */
export interface AppealForm {
  violationId: number
  reason: string
  evidence: string
}
