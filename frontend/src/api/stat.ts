import request from '@/utils/request'
import type { OverviewStat, UserStat, DailySummary, SkillHeat } from '@/types'

export function getOverview(): Promise<OverviewStat> {
  return request.get('/stat/overview')
}

export function getUserStat(userId?: number): Promise<UserStat> {
  return request.get('/stat/user', { userId })
}

export function getDailySummary(days?: number): Promise<DailySummary[]> {
  return request.get('/stat/daily', { days })
}

export function getSkillHeat(): Promise<SkillHeat[]> {
  return request.get('/stat/skill-heat')
}

export function getDemandStat(): Promise<any> {
  return request.get('/stat/demand')
}

export function getSkillStat(): Promise<any> {
  return request.get('/stat/skill')
}

export function getAdminStats(): Promise<any> {
  return request.get('/admin/stats')
}
