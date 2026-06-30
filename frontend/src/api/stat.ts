import request from '@/utils/request'
import type { PersonalOverviewVo, TrendItem, SkillHeatItem, CategoryStatItem } from '@/types/api'

export function getPersonalOverview(): Promise<PersonalOverviewVo> {
  return request.get('/v1/stats/overview')
}

export function getOrderTrend(days?: number): Promise<TrendItem[]> {
  return request.get('/v1/stats/orders/trend', { days })
}

export function getPointTrend(days?: number): Promise<TrendItem[]> {
  return request.get('/v1/stats/points/trend', { days })
}

export function getSkillHeat(limit?: number): Promise<SkillHeatItem[]> {
  return request.get('/v1/stats/skills/heat', { limit })
}

export function getCategoryStats(): Promise<CategoryStatItem[]> {
  return request.get('/v1/stats/categories')
}