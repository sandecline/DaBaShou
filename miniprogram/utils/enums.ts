/**
 * 全局枚举/常量映射（运行时可 import 的 .ts 文件）
 * 与 frontend/src/ 中的枚举定义保持一致
 * 
 * 注意：此文件不受 types/d.ts 运行时不可用的限制，可被所有页面/组件直接 import
 */

// ===== 订单状态 =====
// 与 frontend/src/types/order.ts 一致
export const ORDER_STATUS_MAP: Record<number, string> = {
  0: '已取消',
  1: '待支付',
  2: '已支付（担保中）',  // 与前端对齐
  3: '服务中',
  4: '待确认',
  5: '已完成',
  6: '已退款',
  7: '争议中',
};

// ===== 需求状态 =====
// 与 frontend views/demand/detail.vue 一致
export const DEMAND_STATUS_MAP: Record<number, string> = {
  0: '已关闭',
  1: '待接单',
  2: '进行中',
  3: '已完成',
};

// ===== 货架状态 =====
// 与 frontend views/skill/detail.vue 一致
export const SHELF_STATUS_MAP: Record<number, string> = {
  0: '已下架',
  1: '在售',
  2: '审核中',
};

// ===== 服务方式 =====
export const LOCATION_TYPE_MAP: Record<number, string> = {
  1: '线上',
  2: '线下',
  3: '均可',
};

/** 服务方式选项（供 picker/radio 使用） */
export const LOCATION_TYPE_OPTIONS = [
  { label: '线上', value: 1 },
  { label: '线下', value: 2 },
  { label: '均可', value: 3 },
];

// ===== 需求类型 =====
export const DEMAND_TYPE_MAP: Record<number, string> = {
  1: '求助悬赏',
  2: '技能置换',
};

/** 需求类型选项（供 picker/radio 使用） */
export const DEMAND_TYPE_OPTIONS = [
  { label: '求助悬赏', value: 1, desc: '付出积分，寻找帮手' },
  { label: '技能置换', value: 2, desc: '用自己的技能交换帮助' },
];

// ===== 熟练度 =====
// 与 frontend/src/types/skill.ts 一致
export const PROFICIENCY_MAP: Record<number, string> = {
  1: '了解',
  2: '熟悉',
  3: '精通',
  4: '专家',
};

// ===== 积分流水类型 =====
// 与 frontend/src/types/api.ts 一致
export const POINT_TRANSACTION_TYPE_MAP: Record<number, string> = {
  1: '收入',
  2: '支出',
  3: '冻结',
  4: '解冻',
  5: '签到奖励',
  6: '系统调整',
};

// ===== 通知类型 =====
// 与 frontend/src/types/message.ts 一致
export const NOTIFICATION_TYPE_MAP: Record<string, string> = {
  order: '订单通知',
  review: '评价通知',
  system: '系统消息',
  warning: '超时预警',
};

// ===== 违规类型 =====
// 与 frontend/src/types/api.ts 一致（数字键版本）
export const VIOLATION_TYPE_MAP: Record<number, string> = {
  1: '虚假服务',
  2: '态度恶劣',
  3: '违规交易',
  4: '其他',
};

// ===== 申诉状态 =====
export const APPEAL_STATUS_MAP: Record<number, string> = {
  0: '待审核',
  1: '已通过',
  2: '已驳回',
};

// ===== 校园认证状态 =====
export const CAMPUS_AUTH_STATUS_MAP: Record<number, string> = {
  0: '未认证',
  1: '审核中',
  2: '已认证',
};

// ===== 信任等级 =====
// 与 frontend/src/utils/format.ts 一致
export function getTrustLevel(score: number) {
  if (score >= 4.0) return { level: '金牌' as const, label: '金牌', color: '#f59e0b' };
  if (score >= 3.0) return { level: '靠谱' as const, label: '靠谱', color: '#FF8F00' };
  return { level: '新人' as const, label: '新人', color: '#10b981' };
}

// ===== 消息类型 =====
export const MSG_TYPE_MAP: Record<number, string> = {
  1: '文字',
  2: '图片',
};

// ===== 订单角色 =====
export const ORDER_ROLE_MAP: Record<string, string> = {
  buyer: '我是买家',
  seller: '我是卖家',
};

// ===== 需求排序 =====
export const DEMAND_SORT_OPTIONS = [
  { label: '默认', value: 'default' },
  { label: '悬赏最高', value: 'reward-desc' },
  { label: '截止最早', value: 'deadline' },
  { label: '最新发布', value: 'newest' },
];

// ===== 货架排序 =====
export const SHELF_SORT_OPTIONS = [
  { label: '默认', value: 'default' },
  { label: '价格最低', value: 'price-asc' },
  { label: '价格最高', value: 'price-desc' },
  { label: '最新发布', value: 'newest' },
  { label: '热门推荐', value: 'heat' },
];
