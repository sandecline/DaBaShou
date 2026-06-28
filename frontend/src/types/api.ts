export interface ApiResponse<T = unknown> {
  code: number
  msg: string
  data: T
}

export interface PageQuery {
  pageNum?: number
  pageSize?: number
}

export interface PageResult<T> {
  total: number
  list: T[]
  pageNum: number
  pageSize: number
}

// ============ Auth & User ============

export interface LoginVo {
  accessToken: string
  refreshToken: string
  expiresIn: number
  userId: number
  nickname: string
  avatar: string
}

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

// ============ Skill ============

export interface SkillCategoryVo {
  id: number
  name: string
  icon: string
  sortOrder: number
  children?: SkillCategoryVo[]
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

export interface UserSkillDto {
  skillTagId: number
  description: string
  proficiency: number
}

// ============ Shelf ============

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

export interface SkillShelfDto {
  skillTagId: number
  title: string
  description: string
  pointPrice: number
  durationMinutes: number
  locationType: number
}

export interface TimeSlotVo {
  id: number
  dayOfWeek: number
  startTime: string
  endTime: string
  available: boolean
}

export interface TimeSlotDto {
  dayOfWeek: number
  startTime: string
  endTime: string
}

export interface ShelfSearchParams {
  keyword?: string
  categoryId?: number
  skillTagId?: number
  locationType?: number
  sortBy?: 'heat' | 'distance' | 'trust' | 'price'
  longitude?: number
  latitude?: number
  pageNum?: number
  pageSize?: number
}

// ============ Demand ============

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

export interface DemandDto {
  skillTagId: number
  title: string
  description: string
  demandType: number
  pointReward?: number
  images?: string[]
  deadline?: string
  locationType: number
  longitude?: number
  latitude?: number
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

// ============ Order ============

export interface OrderItemVo {
  id: number
  orderNo: string
  title: string
  skillTagName: string
  pointAmount: number
  status: number
  statusDesc: string
  counterpartNickname: string
  counterpartAvatar: string
  createTime: string
}

export interface OrderDetailVo extends OrderItemVo {
  buyerId: number
  buyerNickname: string
  sellerId: number
  sellerNickname: string
  verifyCode: string | null
  verifyCodeExpire: string | null
  serviceStartTime: string | null
  serviceEndTime: string | null
  completeTime: string | null
  cancelTime: string | null
  cancelReason: string | null
  remark: string
}

export interface CreateOrderFromShelfDto {
  skillShelfId: number
  timeSlotId?: number
  remark?: string
}

export interface CreateOrderFromDemandDto {
  demandId: number
  sellerId: number
  remark?: string
}

export interface PayOrderDto {
  idempotentToken: string
}

export interface PayResultVo {
  status: number
  verifyCode: string
  verifyCodeExpire: string
}

// ============ Point ============

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
  orderId: number
  orderNo: string
  description: string
  createTime: string
}

export interface PointStatsVo {
  totalIncome: number
  totalExpense: number
  monthIncome: number
  monthExpense: number
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

// ============ Message ============

export interface NotificationVo {
  id: number
  type: string
  title: string
  content: string
  relatedType: string
  relatedId: number
  isRead: number
  readTime: string
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
}

// ============ Credit ============

export interface ReviewVo {
  id: number
  orderId: number
  orderNo: string
  reviewerId: number
  reviewerNickname: string
  revieweeId: number
  revieweeNickname: string
  rating: number
  content: string
  createTime: string
}

export interface ReviewDto {
  orderId: number
  rating: number
  content: string
  images?: string[]
  isAnonymous?: boolean
}

export interface ViolationVo {
  id: number
  orderId: number
  type: string
  description: string
  penaltyScore: number
  status: number
  createTime: string
}

export interface ViolationDto {
  targetUserId: number
  orderId?: number
  type: number
  reason: string
  evidence?: string[]
}

export interface AppealVo {
  id: number
  violationId: number
  reason: string
  status: number
  statusDesc: string
  reviewRemark: string
  reviewTime: string
  createTime: string
}

export interface AppealDto {
  violationId: number
  reason: string
  evidence?: string[]
}

// ============ Stat ============

export interface DailyStatVo {
  date: string
  newUserCount: number
  activeUserCount: number
  newOrderCount: number
  completedOrderCount: number
  pointInflow: number
  pointOutflow: number
}

export interface SkillHeatVo {
  skillTagId: number
  skillTagName: string
  shelfCount: number
  demandCount: number
  orderCount: number
  heatScore: number
}

export interface PersonalOverviewVo {
  totalOrders: number
  completedOrders: number
  totalIncome: number
  totalExpense: number
  trustScore: number
  skillCount: number
  reviewCount: number
}

export interface TrendDataVo {
  date: string
  value: number
}

export interface CategoryStatVo {
  categoryName: string
  count: number
  percentage: number
}

export interface PlatformOverviewVo {
  totalUsers: number
  totalOrders: number
  totalShelves: number
  totalDemands: number
  todayNewUsers: number
  todayNewOrders: number
}

export interface UserActiveStatVo {
  date: string
  activeUsers: number
  newUsers: number
}

export interface TrustDistributionVo {
  level: string
  count: number
  percentage: number
}

// ============ System ============

export interface FileVo {
  id: number
  fileName: string
  fileUrl: string
  fileSize: number
  fileType: string
}

export interface RoleVo {
  id: number
  roleCode: string
  roleName: string
  description: string
  status: number
  createTime: string
}

export interface PermissionVo {
  id: number
  parentId: number
  permissionCode: string
  permissionName: string
  type: number
  path: string
  component: string
  icon: string
  sortOrder: number
  children?: PermissionVo[]
}

export interface LogVo {
  id: number
  userId: number
  username: string
  module: string
  action: string
  method: string
  params: string
  ip: string
  duration: number
  createTime: string
}

export interface ConfigVo {
  id: number
  configKey: string
  configValue: string
  description: string
  createTime: string
}

// ============ Admin ============

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
  buyerNickname: string
  sellerNickname: string
  title: string
  pointAmount: number
  status: number
  statusDesc: string
  createTime: string
}

export interface ViolationAdminVo {
  id: number
  reporterNickname: string
  targetUserNickname: string
  type: number
  typeName: string
  reason: string
  evidence: string[]
  status: number
  handleResult: string
  createTime: string
}

export interface AppealAdminVo {
  id: number
  userNickname: string
  violationId: number
  reason: string
  evidence: string[]
  status: number
  handleResult: string
  createTime: string
}

export interface CampusAuthVo {
  id: number
  userId: number
  userNickname: string
  schoolName: string
  studentId: string
  realName: string
  cardImageUrl: string
  status: number
  rejectReason: string
  createTime: string
}
