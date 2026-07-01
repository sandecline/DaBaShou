/**
 * 订单状态中文映射（运行时常量）
 * 从 types/order.d.ts 拆出：.d.ts 声明文件不会编译生成 .js，
 * 运行时 require 'types/order' 会失败，故将常量移至此处。
 */
import type { OrderStatus } from '../types/order';

export const ORDER_STATUS_MAP: Record<OrderStatus, string> = {
  0: '已取消',
  1: '待支付',
  2: '已支付',
  3: '服务中',
  4: '待确认',
  5: '已完成',
  6: '已退款',
  7: '争议中',
};
