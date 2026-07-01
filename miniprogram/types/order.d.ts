/**
 * 订单相关类型定义
 * 对应后端 dabashou-order 模块
 * 与 frontend/src/types/order.ts + api.ts 保持一致
 * 状态机见 AGENTS.md 附录 B
 */

/** 订单状态 */
export type OrderStatus = 0 | 1 | 2 | 3 | 4 | 5 | 6 | 7;

/** 订单列表项（与前端 OrderItemVo 一致） */
export interface Order {
  id: number;
  orderNo: string;
  /** 买家昵称 */
  buyerId?: number;
  buyerNickname?: string;
  buyerName?: string;
  buyerAvatar?: string;
  /** 卖家昵称 */
  sellerId?: number;
  sellerNickname?: string;
  sellerName?: string;
  sellerAvatar?: string;
  /** 对方头像/昵称（前端兼容字段） */
  counterpartNickname?: string;
  counterpartAvatar?: string;
  /** 服务标题 */
  title: string;
  /** 技能标签名 */
  skillTagName?: string;
  /** 积分金额 */
  pointAmount: number;
  /** 订单状态 */
  status: OrderStatus;
  /** 状态描述 */
  statusDesc?: string;
  createTime: string;
}

/** 订单详情（与前端 OrderDetailVo 一致） */
export interface OrderDetail extends Order {
  /** 关联的需求/技能 */
  demandId?: number;
  skillShelfId?: number;
  skillTagId?: number;
  /** 服务信息 */
  description?: string;
  durationMinutes?: number;
  locationType?: import('./skill').LocationType;
  /** 核销码 */
  verifyCode?: string | null;
  verifyCodeExpire?: string | null;
  /** 时间预约 */
  timeSlotId?: number;
  /** 备注 */
  remark?: string;
  /** 时间节点 */
  serviceStartTime?: string | null;
  serviceEndTime?: string | null;
  completeTime?: string | null;
  cancelTime?: string | null;
  cancelReason?: string | null;
  /** 操作日志 */
  statusLogs?: OrderStatusLog[];
}

/** 订单状态变更日志 */
export interface OrderStatusLog {
  fromStatus: OrderStatus;
  toStatus: OrderStatus;
  operatorId: number;
  remark: string;
  createTime: string;
}

/** 从货架创建订单参数 */
export interface CreateFromShelfParams {
  shelfId: number;
  timeSlotId?: number;
  remark?: string;
}

/** 从需求创建订单参数 */
export interface CreateFromDemandParams {
  demandId: number;
  sellerId?: number;
  remark?: string;
}

/** 核销码 */
export interface VerifyCodeVo {
  verifyCode: string;
  expireTime: string;
}
