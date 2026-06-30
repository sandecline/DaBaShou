/** 统一API响应格式 */
export interface ApiResponse<T = any> {
  code: number
  msg: string
  data: T
}

/** 分页请求参数 */
export interface PageParams {
  pageNum?: number
  pageSize?: number
  [key: string]: any
}

/** 分页结果 */
export interface PageResult<T> {
  total: number
  list: T[]
  pageNum: number
  pageSize: number
}

// ---- 登录 ----

export interface LoginParams {
  username: string
  password: string
}

export interface LoginVo {
  accessToken: string
  refreshToken: string
  expiresIn: number
  userId: number
  nickname: string
  avatar: string
}

export interface RegisterParams {
  username: string
  password: string
  nickname?: string
  phone?: string
}

// ---- 用户 ----

export interface UserProfileVo {
  id: number
  username: string
  nickname: string
  avatar: string
  phone: string
  email: string
  pointBalance: number
  trustScore: number
  trustLevel: string
  longitude: number
  latitude: number
  campus: string
  building: string
  bio: string
  status: number
  createTime: string
}

export interface TrustScoreVo {
  score: number
  level: string
  recentLogs: TrustLogItem[]
}

export interface TrustLogItem {
  type: string
  scoreChange: number
  scoreBefore: number
  scoreAfter: number
  reason: string
  createTime: string
}

export interface CampusAuthVo {
  id: number
  authType: string
  studentNo: string
  realName: string
  campus: string
  college: string
  status: number
  statusDesc: string
  reviewRemark: string | null
  reviewTime: string | null
  createTime: string
}

// ---- 技能 ----

export interface CategoryTreeNode {
  id: number
  name: string
  icon: string
  sortOrder: number
  children?: CategoryTreeNode[]
}

export interface SkillTagVo {
  id: number
  categoryId: number
  name: string
  status: number
}

export interface UserSkillVo {
  id: number
  skillTagId: number
  skillTagName: string
  categoryName: string
  proficiency: number
  proficiencyDesc: string
  description: string
  createTime: string
}

// ---- 货架 ----

export interface ShelfItemVo {
  id: number
  userId: number
  nickname: string
  avatar: string
  trustScore: number
  skillTagName: string
  title: string
  pointPrice: number
  durationMinutes: number
  locationType: number
  status: number
}

export interface ShelfDetailVo extends ShelfItemVo {
  description: string
  locationTypeDesc: string
  statusDesc: string
  createTime: string
}

export interface TimeSlotVo {
  id: number
  dayOfWeek: number
  startTime: string
  endTime: string
  available: boolean
}

// ---- 需求 ----

export interface DemandItemVo {
  id: number
  userId: number
  nickname: string
  avatar: string
  skillTagName: string
  title: string
  pointReward: number
  deadline: string
  locationType: number
  demandType: number
  demandTypeDesc: string
  status: number
  statusDesc: string
  createTime: string
}

export interface DemandDetailVo extends DemandItemVo {
  description: string
}

export interface DemandMatchVo {
  shelfId: number
  userId: number
  nickname: string
  avatar: string
  title: string
  pointPrice: number
  trustScore: number
  matchScore: number
}

// ---- 订单 ----

export type OrderStatus = 0 | 1 | 2 | 3 | 4 | 5 | 6 | 7

export const OrderStatusMap: Record<OrderStatus, string> = {
  0: '已取消',
  1: '待支付',
  2: '已支付（担保中）',
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

export interface OrderItemVo {
  id: number
  orderNo: string
  title: string
  skillTagName: string
  pointAmount: number
  status: OrderStatus
  statusDesc: string
  counterpartNickname: string
  counterpartAvatar: string
  createTime: string
}

export interface OrderDetailVo {
  id: number
  orderNo: string
  buyerId: number
  buyerNickname: string
  sellerId: number
  sellerNickname: string
  title: string
  skillTagName: string
  pointAmount: number
  status: OrderStatus
  statusDesc: string
  verifyCode: string | null
  verifyCodeExpire: string | null
  serviceStartTime: string | null
  serviceEndTime: string | null
  completeTime: string | null
  cancelTime: string | null
  cancelReason: string | null
  remark: string
  createTime: string
}

export interface VerifyCodeVo {
  verifyCode: string
  expireTime: string
}

export interface PayResultVo {
  status: number
  verifyCode: string
  verifyCodeExpire: string
}

// ---- 积分 ----

export interface PointBalanceVo {
  available: number
  frozen: number
  total: number
}

export interface PointTransVo {
  id: number
  type: number
  typeDesc: string
  amount: number
  balanceAfter: number
  orderId: number | null
  orderNo: string
  description: string
  createTime: string
}

export const PointTransactionTypeMap: Record<number, string> = {
  1: '收入',
  2: '支出',
  3: '冻结',
  4: '解冻',
  5: '签到奖励',
  6: '系统调整',
}

export interface PointStatsVo {
  totalIncome: number
  totalExpense: number
  monthIncome: number
  monthExpense: number
}

export interface SignInResultVo {
  reward: number
  consecutiveDays: number
}

export interface SignInStatusVo {
  todaySigned: boolean
  consecutiveDays: number
  reward: number
}

export interface GuaranteePoolVo {
  totalPool: number
  frozenAmount: number
  availableAmount: number
}

// ---- 信用 ----

export interface ReviewVo {
  id: number
  orderId: number
  rating: number
  content: string
  images: string[]
  isAnonymous: boolean
  reviewerId: number
  reviewerNickname: string
  reviewerAvatar: string
  createTime: string
}

export interface ViolationVo {
  id: number
  targetUserId: number
  targetNickname: string
  orderId: number | null
  type: number
  typeDesc: string
  reason: string
  evidence: string[]
  status: number
  statusDesc: string
  handleResult: string | null
  createTime: string
}

export interface AppealVo {
  id: number
  violationId: number
  reason: string
  evidence: string[]
  status: number
  statusDesc: string
  reviewRemark: string | null
  createTime: string
}

// ---- 消息 ----

export interface NotificationVo {
  id: number
  type: string
  title: string
  content: string
  relatedType: string
  relatedId: number
  isRead: number
  readTime: string | null
  createTime: string
}

export interface ChatSessionVo {
  id: number
  otherUserId: number
  otherNickname: string
  otherAvatar: string
  lastMessage: string
  lastTime: string
  unreadCount: number
}

export interface ChatMessageVo {
  id: number
  senderId: number
  senderNickname: string
  senderAvatar: string
  content: string
  msgType: number
  isRead: number
  createTime: string
  isMine?: boolean
}

// ---- 统计 ----

export interface PersonalOverviewVo {
  totalOrders: number
  completedOrders: number
  totalIncome: number
  totalExpense: number
  trustScore: number
  skillCount: number
  reviewCount: number
}

export interface TrendItem {
  date: string
  value: number
}

export interface SkillHeatItem {
  skillTagId: number
  skillTagName: string
  shelfCount: number
  demandCount: number
  orderCount: number
  heatScore: number
}

export interface CategoryStatItem {
  categoryName: string
  count: number
  percentage: number
}

// ---- 管理 ----

export interface UserAdminVo {
  id: number
  username: string
  nickname: string
  avatar: string
  phone: string
  pointBalance: number
  trustScore: number
  campus: string
  status: number
  createTime: string
  lastLoginTime: string
}

export interface OrderAdminVo {
  id: number
  orderNo: string
  buyerId: number
  buyerNickname: string
  sellerId: number
  sellerNickname: string
  title: string
  pointAmount: number
  status: number
  statusDesc: string
  createTime: string
}

export interface AdminOverviewVo {
  totalUsers: number
  totalOrders: number
  totalShelves: number
  totalDemands: number
  todayNewUsers: number
  todayNewOrders: number
}

export interface DailyTrendItem {
  date: string
  newUserCount: number
  activeUserCount: number
  newOrderCount: number
  completedOrderCount: number
  pointInflow: number
  pointOutflow: number
}

export interface UserActiveItem {
  date: string
  activeUsers: number
  newUsers: number
}

export interface TrustDistributionItem {
  level: string
  count: number
  percentage: number
}

export interface CampusAuthAdminVo {
  id: number
  userId: number
  nickname: string
  authType: string
  studentNo: string
  realName: string
  campus: string
  college: string
  status: number
  statusDesc: string
  reviewRemark: string | null
  createTime: string
}