<template>
  <div class="admin-page">
    <div class="admin-layout">
      <Sidebar :menu-items="adminMenu" />
      <div class="admin-content">
        <div class="page-container">
          <h2>平台数据统计</h2>

          <div class="stat-cards">
            <div class="stat-card" v-for="item in statCards" :key="item.label">
              <div class="stat-value">{{ item.value }}</div>
              <div class="stat-label">{{ item.label }}</div>
            </div>
          </div>

          <el-row :gutter="16">
            <el-col :span="12">
              <div class="chart-card">
                <h3>近30日趋势</h3>
                <div ref="lineChartRef" class="chart-box" />
              </div>
            </el-col>
            <el-col :span="12">
              <div class="chart-card">
                <h3>技能热度排行 Top 10</h3>
                <div ref="barChartRef" class="chart-box" />
              </div>
            </el-col>
          </el-row>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, nextTick } from 'vue'
import { getAdminOverview, getAdminDailyTrend, getAdminUserActive, getAdminTrustDistribution } from '@/api/admin'
import { getSkillHeat } from '@/api/stat'
import Sidebar from '@/components/layout/Sidebar.vue'
import * as echarts from 'echarts'
import type { AdminOverviewVo, DailyTrendItem, SkillHeatItem } from '@/types/api'

const adminMenu = [
  { path: '/admin/users', title: '用户管理', icon: 'User' },
  { path: '/admin/orders', title: '订单管理', icon: 'Document' },
  { path: '/admin/credit', title: '信用管理', icon: 'Warning' },
  { path: '/admin/system', title: '系统配置', icon: 'Setting' },
  { path: '/admin/stat', title: '数据统计', icon: 'DataAnalysis' },
]

const lineChartRef = ref()
const barChartRef = ref()

const statCards = ref([
  { label: '注册用户', value: '0' },
  { label: '技能服务', value: '0' },
  { label: '求助需求', value: '0' },
  { label: '完成订单', value: '0' },
  { label: '订单完成率', value: '0%' },
  { label: '积分流通量', value: '0' },
])

onMounted(async () => {
  const [overview, dailyData, heatData] = await Promise.all([
    getAdminOverview().catch(() => null as AdminOverviewVo | null),
    getAdminDailyTrend(30).catch(() => [] as DailyTrendItem[]),
    getSkillHeat().catch(() => [] as SkillHeatItem[]),
  ])

  if (overview) {
    statCards.value = [
      { label: '注册用户', value: String(overview.totalUsers || 0) },
      { label: '技能服务', value: String(overview.totalSkills || 0) },
      { label: '求助需求', value: String(overview.totalDemands || 0) },
      { label: '完成订单', value: String(overview.completedOrders || 0) },
      { label: '订单完成率', value: `${Math.round((overview.orderCompletionRate || 0) * 100)}%` },
      { label: '积分流通量', value: String(overview.totalPointsInCirculation || 0) },
    ]
  }

  await nextTick()
  if (dailyData.length > 0 && lineChartRef.value) {
    const chart = echarts.init(lineChartRef.value)
    chart.setOption({
      tooltip: { trigger: 'axis' },
      legend: { data: ['新增用户', '新增订单', '完成订单'] },
      xAxis: { type: 'category', data: dailyData.map((d) => d.date.slice(5)) },
      yAxis: { type: 'value' },
      series: [
        { name: '新增用户', type: 'line', data: dailyData.map((d) => d.newUsers), smooth: true, itemStyle: { color: '#FFC300' } },
        { name: '新增订单', type: 'line', data: dailyData.map((d) => d.newOrders), smooth: true, itemStyle: { color: '#f59e0b' } },
        { name: '完成订单', type: 'line', data: dailyData.map((d) => d.completedOrders), smooth: true, itemStyle: { color: '#10b981' } },
      ],
    })
  }

  if (heatData.length > 0 && barChartRef.value) {
    const chart = echarts.init(barChartRef.value)
    chart.setOption({
      tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
      xAxis: { type: 'category', data: heatData.slice(0, 10).map((d) => d.tagName) },
      yAxis: { type: 'value' },
      series: [{
        name: '热度',
        type: 'bar',
        data: heatData.slice(0, 10).map((d) => d.heatScore),
        itemStyle: { color: '#FFC300', borderRadius: [4, 4, 0, 0] },
      }],
    })
  }
})
</script>

<style scoped lang="scss">
.admin-layout {
  display: flex;
  min-height: calc(100vh - #{$header-offset});
}

.admin-content {
  flex: 1;
  overflow-x: auto;
}

h2 {
  margin: 0 0 $spacing-md;
  font-size: $font-size-xl;
  font-weight: 700;
}

.stat-cards {
  display: grid;
  grid-template-columns: repeat(6, 1fr);
  gap: $spacing-md;
  margin-bottom: $spacing-xl;
}

.stat-card {
  background: #ffffff;
  border-radius: $radius-md;
  padding: $spacing-lg;
  text-align: center;
  border: 1px solid $color-border;

  .stat-value {
    font-size: 24px;
    font-weight: 700;
    color: $color-primary;
  }

  .stat-label {
    font-size: $font-size-xs;
    color: $color-text-secondary;
    margin-top: 4px;
  }
}

.chart-card {
  background: #ffffff;
  border-radius: $radius-md;
  padding: $spacing-md;
  border: 1px solid $color-border;

  h3 {
    margin: 0 0 12px;
    font-size: $font-size-base;
    font-weight: 600;
  }

  .chart-box {
    height: 300px;
  }
}

@media (max-width: 768px) {
  .stat-cards {
    grid-template-columns: repeat(2, 1fr);
  }
}
</style>
