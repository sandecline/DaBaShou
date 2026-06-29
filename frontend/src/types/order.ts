/** 订单状态 */
export type OrderStatus = 0 | 1 | 2 | 3 | 4 | 5 | 6 | 7

export const OrderStatusMap: Record<OrderStatus, string> = {
  0: '已取消',
  1: '待支付',
  2: '已支付',
  3: '服务中',
  4: '待确认',
  5: '已完成',
  6: '已退款',
  7: '争议中',
}

export const OrderStatusColor: Record<OrderStatus, string> = {
  0: '#909399',
  1: '#e6a23c',
  2: '#409eff',
  3: '#FF8F00',
  4: '#e6a23c',
  5: '#67c23a',
  6: '#f56c6c',
  7: '#f56c6c',
}

/** 订单 */
export interface Order {
  id: number
  orderNo: string
  buyerId: number
  sellerId: number
  demandId: number | null
  skillShelfId: number | null
  skillTagId: number
  title: string
  pointAmount: number
  status: OrderStatus
  verifyCode: string | null
  verifyCodeExpire: string | null
  timeSlotId: number | null
  serviceStartTime: string | null
  serviceEndTime: string | null
  completeTime: string | null
  cancelTime: string | null
  cancelReason: string | null
  createTime: string
  updateTime: string
  // 前端附加字段
  buyerName?: string
  buyerAvatar?: string
  sellerName?: string
  sellerAvatar?: string
  tagName?: string
}

/** 积分流水 */
export interface PointTransaction {
  id: number
  userId: number
  orderId: number | null
  type: PointTransactionType
  amount: number
  balanceAfter: number
  description: string
  createTime: string
}

export type PointTransactionType = 1 | 2 | 3 | 4 | 5 | 6

export const PointTransactionTypeMap: Record<PointTransactionType, string> = {
  1: '收入',
  2: '支出',
  3: '冻结',
  4: '解冻',
  5: '系统奖励',
  6: '系统扣除',
}

/** 核销确认 */
export interface VerifyOrderParams {
  orderId: number
  verifyCode: string
}
