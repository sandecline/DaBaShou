import request from '@/utils/request'
import type { ApiResponse, PersonalOverviewVo, TrendDataVo, SkillHeatVo, CategoryStatVo, PlatformOverviewVo, DailyStatVo, UserActiveStatVo, TrustDistributionVo } from '@/types/api'

export function getPersonalOverview(): Promise<ApiResponse<PersonalOverviewVo>> {
  return request({ url: '/v1/stats/overview', method: 'get' })
}

export function getOrderTrend(days = 30): Promise<ApiResponse<TrendDataVo[]>> {
  return request({ url: '/v1/stats/orders/trend', method: 'get', params: { days } })
}

export function getPointTrend(days = 30): Promise<ApiResponse<TrendDataVo[]>> {
  return request({ url: '/v1/stats/points/trend', method: 'get', params: { days } })
}

export function getSkillHeat(limit = 10): Promise<ApiResponse<SkillHeatVo[]>> {
  return request({ url: '/v1/stats/skills/heat', method: 'get', params: { limit } })
}

export function getCategoryStat(): Promise<ApiResponse<CategoryStatVo[]>> {
  return request({ url: '/v1/stats/categories', method: 'get' })
}

export function getPlatformOverview(): Promise<ApiResponse<PlatformOverviewVo>> {
  return request({ url: '/admin/v1/stats/overview', method: 'get' })
}

export function getDailyTrend(days = 30): Promise<ApiResponse<DailyStatVo[]>> {
  return request({ url: '/admin/v1/stats/daily-trend', method: 'get', params: { days } })
}

export function getUserActiveStat(days = 30): Promise<ApiResponse<UserActiveStatVo[]>> {
  return request({ url: '/admin/v1/stats/user-active', method: 'get', params: { days } })
}

export function getTrustDistribution(): Promise<ApiResponse<TrustDistributionVo[]>> {
  return request({ url: '/admin/v1/stats/trust-distribution', method: 'get' })
}

export function exportData(type: string): Promise<Blob> {
  return request({ url: '/admin/v1/stats/export', method: 'get', params: { type }, responseType: 'blob' })
}

export function getAdminStats(): Promise<ApiResponse<any>> {
  return request({ url: '/admin/v1/stats/overview', method: 'get' })
}

export function getDailySummary(days?: number): Promise<ApiResponse<any>> {
  return request({ url: '/admin/v1/stats/daily-trend', method: 'get', params: { days } })
}

// Shim: getUserStat used in stat/overview.vue
export function getUserStat(params?: any): any {
  return getPersonalOverview(params)
}
