export type OrderStatus = 0 | 1 | 2 | 3 | 4 | 5 | 6 | 7;

export interface OrderItemVo {
  id: number;
  orderNo: string;
  buyerId: number;
  buyerNickname: string;
  sellerId: number;
  sellerNickname: string;
  shelfTitle: string;
  tagName: string;
  pointAmount: number;
  status: OrderStatus;
  statusName: string;
  createTime: string;
}

export interface OrderDetailVo {
  id: number;
  orderNo: string;
  buyerId: number;
  buyerNickname: string;
  buyerAvatar: string;
  sellerId: number;
  sellerNickname: string;
  sellerAvatar: string;
  shelfId: number;
  shelfTitle: string;
  demandId: number | null;
  tagName: string;
  pointAmount: number;
  status: OrderStatus;
  statusName: string;
  buyerVerifyCode: string | null;
  sellerVerifyCode: string | null;
  buyerConfirmCode: string | null;
  sellerConfirmCode: string | null;
  buyerVerified: boolean;
  sellerVerified: boolean;
  buyerConfirmed: boolean;
  sellerConfirmed: boolean;
  refundRequester: string | null;
  refundAgreed: boolean;
  timeSlotId: number | null;
  serviceStartTime: string | null;
  serviceEndTime: string | null;
  completeTime: string | null;
  cancelTime: string | null;
  cancelReason: string | null;
  remark: string;
  createTime: string;
}

export interface CreateFromShelfParams {
  shelfId: number;
  timeSlotId?: number;
  remark?: string;
}

export interface CreateFromDemandParams {
  demandId: number;
  sellerId?: number;
  remark?: string;
}

export type { OrderItemVo as Order };
export type { OrderDetailVo as OrderDetail };
