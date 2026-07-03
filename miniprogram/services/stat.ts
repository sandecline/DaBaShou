/**
 * 数据统计 API
 */
import { api } from '../utils/request';

export interface OverviewVo {
  totalOrders: number;
  completedOrders: number;
  totalIncome: number;
  totalExpense: number;
  trustScore: number;
  skillCount: number;
  reviewCount: number;
  publishedSkills: number;
  publishedDemands: number;
  takenOrders: number;
  averageRating: number;
  totalPointsEarned: number;
  totalPointsSpent: number;
}

export interface TrendItem {
  date: string;
  value: number;
}

export interface SkillHeatItem {
  skillTagId: number;
  skillTagName: string;
  shelfCount: number;
  demandCount: number;
  orderCount: number;
  heatScore: number;
}

export interface CategoryStatItem {
  categoryName: string;
  count: number;
  percentage: number;
}

export const statService = {
  getOverview() {
    return api.get<OverviewVo>('/v1/stats/overview');
  },

  getOrdersTrend(days = 30) {
    return api.get<TrendItem[]>('/v1/stats/orders/trend', { days });
  },

  getPointsTrend(days = 30) {
    return api.get<TrendItem[]>('/v1/stats/points/trend', { days });
  },

  getSkillsHeat(limit = 10) {
    return api.get<SkillHeatItem[]>('/v1/stats/skills/heat', { limit });
  },

  getCategories() {
    return api.get<CategoryStatItem[]>('/v1/stats/categories');
  },
};
