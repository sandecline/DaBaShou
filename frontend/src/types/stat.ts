/** 平台统计概览 */
export interface OverviewStat {
  totalUsers: number
  activeUsers: number
  totalSkills: number
  totalDemands: number
  totalOrders: number
  completedOrders: number
  orderCompletionRate: number
  totalPointsInCirculation: number
}

/** 每日统计汇总 */
export interface DailySummary {
  id: number
  date: string
  newUsers: number
  newSkills: number
  newDemands: number
  newOrders: number
  completedOrders: number
  cancelledOrders: number
  pointsCreated: number
  pointsConsumed: number
}

/** 技能热度统计 */
export interface SkillHeat {
  id: number
  skillTagId: number
  tagName: string
  shelfCount: number
  orderCount: number
  heatScore: number
  updateTime: string
}

/** 个人统计 */
export interface UserStat {
  publishedSkills: number
  publishedDemands: number
  takenOrders: number
  completedOrders: number
  averageRating: number
  totalPointsEarned: number
  totalPointsSpent: number
}
