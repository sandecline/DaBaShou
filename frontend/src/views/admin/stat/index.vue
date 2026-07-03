<template>
  <AdminLayout
    title="数据统计"
    subtitle="观察供需、订单、活跃和信用结构"
  >
    <template #actions>
      <el-button :icon="Refresh" :loading="loading" @click="loadData">刷新</el-button>
    </template>

    <LoadingSpinner v-if="loading" text="加载统计..." />

    <template v-else>
      <div class="stat-cards">
        <div class="stat-card" v-for="item in statCards" :key="item.label">
          <span>{{ item.label }}</span>
          <strong>{{ item.value }}</strong>
          <small>{{ item.hint }}</small>
        </div>
      </div>

      <div class="chart-grid">
        <section class="chart-panel wide">
          <h2>近30日平台趋势</h2>
          <div ref="lineChartRef" class="chart-box" />
        </section>
        <section class="chart-panel">
          <h2>技能热度 Top 10</h2>
          <div ref="barChartRef" class="chart-box" />
        </section>
        <section class="chart-panel">
          <h2>信任分分布</h2>
          <div ref="trustChartRef" class="chart-box" />
        </section>
        <section class="chart-panel">
          <h2>用户活跃</h2>
          <div ref="activeChartRef" class="chart-box" />
        </section>
      </div>
    </template>
  </AdminLayout>
</template>

<script setup lang="ts">
import { computed, nextTick, onBeforeUnmount, onMounted, ref } from 'vue'
import { Refresh } from '@element-plus/icons-vue'
import { getAdminDailyTrend, getAdminOverview, getAdminSkillHeat, getAdminTrustDistribution, getAdminUserActive } from '@/api/admin'
import AdminLayout from '@/components/layout/AdminLayout.vue'
import LoadingSpinner from '@/components/common/LoadingSpinner.vue'
import * as echarts from 'echarts'
import type { AdminOverviewVo, DailyTrendItem, SkillHeatItem, TrustDistributionItem, UserActiveItem } from '@/types/api'

const loading = ref(false)
const overview = ref<AdminOverviewVo | null>(null)
const dailyData = ref<DailyTrendItem[]>([])
const heatData = ref<SkillHeatItem[]>([])
const activeData = ref<UserActiveItem[]>([])
const trustData = ref<TrustDistributionItem[]>([])
const lineChartRef = ref<HTMLElement>()
const barChartRef = ref<HTMLElement>()
const trustChartRef = ref<HTMLElement>()
const activeChartRef = ref<HTMLElement>()
const chartInstances: echarts.ECharts[] = []

function disposeCharts() {
  chartInstances.forEach((c) => c.dispose())
  chartInstances.length = 0
}

const statCards = computed(() => [
  { label: '注册用户', value: overview.value?.totalUsers ?? 0, hint: `今日新增 ${overview.value?.todayNewUsers ?? 0}` },
  { label: '技能服务', value: overview.value?.totalSkills ?? overview.value?.totalShelves ?? 0, hint: '累计服务供给' },
  { label: '求助需求', value: overview.value?.totalDemands ?? 0, hint: '待接单需求' },
  { label: '完成订单', value: overview.value?.completedOrders ?? 0, hint: `完成率 ${Math.round((overview.value?.orderCompletionRate ?? 0) * 100)}%` },
  { label: '争议订单', value: overview.value?.disputingOrders ?? 0, hint: '需要仲裁' },
  { label: '积分流通', value: overview.value?.totalPointsInCirculation ?? 0, hint: '用户积分余额' },
])

async function loadData() {
  loading.value = true
  try {
    const [overviewResult, dailyResult, heatResult, activeResult, trustResult] = await Promise.all([
      getAdminOverview().catch(() => null),
      getAdminDailyTrend(30).catch(() => []),
      getAdminSkillHeat(10).catch(() => []),
      getAdminUserActive(30).catch(() => []),
      getAdminTrustDistribution().catch(() => []),
    ])
    overview.value = overviewResult
    dailyData.value = fillDaily(dailyResult)
    heatData.value = heatResult
    activeData.value = fillActive(activeResult)
    trustData.value = trustResult.length > 0 ? trustResult : [
      { level: '新人', count: 0, percentage: 0 },
      { level: '靠谱', count: 0, percentage: 0 },
      { level: '金牌', count: 0, percentage: 0 },
    ]
    await nextTick()
    renderCharts()
  } finally {
    loading.value = false
  }
}

function fillDaily(data: DailyTrendItem[]) {
  if (data.length > 0) return data
  const today = new Date()
  return Array.from({ length: 30 }, (_, index) => {
    const date = new Date(today)
    date.setDate(today.getDate() - 29 + index)
    return {
      date: date.toISOString().slice(0, 10),
      newUserCount: 0,
      activeUserCount: 0,
      newOrderCount: 0,
      completedOrderCount: 0,
      pointInflow: 0,
      pointOutflow: 0,
    }
  })
}

function fillActive(data: UserActiveItem[]) {
  if (data.length > 0) return data
  return dailyData.value.map((item) => ({
    date: item.date,
    value: item.activeUserCount,
  }))
}

function renderCharts() {
  disposeCharts()

  if (lineChartRef.value) {
    const chart = echarts.init(lineChartRef.value)
    chartInstances.push(chart)
    chart.setOption({
      tooltip: { trigger: 'axis' },
      legend: { data: ['新增用户', '新增订单', '完成订单'] },
      xAxis: { type: 'category', data: dailyData.value.map((d) => d.date.slice(5)) },
      yAxis: { type: 'value' },
      series: [
        { name: '新增用户', type: 'line', data: dailyData.value.map((d) => d.newUserCount), smooth: true, itemStyle: { color: '#0f766e' } },
        { name: '新增订单', type: 'line', data: dailyData.value.map((d) => d.newOrderCount), smooth: true, itemStyle: { color: '#b45309' } },
        { name: '完成订单', type: 'line', data: dailyData.value.map((d) => d.completedOrderCount), smooth: true, itemStyle: { color: '#2563eb' } },
      ],
    })
  }

  if (barChartRef.value) {
    const chartData = heatData.value.length > 0
      ? heatData.value
      : [{ skillTagId: 0, skillTagName: '暂无数据', shelfCount: 0, demandCount: 0, orderCount: 0, heatScore: 0 }]
    const chart = echarts.init(barChartRef.value)
    chartInstances.push(chart)
    chart.setOption({
      tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
      xAxis: { type: 'category', data: chartData.map((d) => d.tagName ?? d.skillTagName) },
      yAxis: { type: 'value' },
      series: [{ name: '热度', type: 'bar', data: chartData.map((d) => d.heatScore), itemStyle: { color: '#0f766e', borderRadius: [4, 4, 0, 0] } }],
    })
  }

  if (trustChartRef.value) {
    const chart = echarts.init(trustChartRef.value)
    chartInstances.push(chart)
    chart.setOption({
      tooltip: { trigger: 'item' },
      series: [{
        type: 'pie',
        radius: ['42%', '72%'],
        data: trustData.value.map((item) => ({ name: item.level, value: item.count })),
        color: ['#94a3b8', '#0f766e', '#b45309'],
      }],
    })
  }

  if (activeChartRef.value) {
    const chart = echarts.init(activeChartRef.value)
    chartInstances.push(chart)
    chart.setOption({
      tooltip: { trigger: 'axis' },
      xAxis: { type: 'category', data: activeData.value.map((d) => d.date.slice(5)) },
      yAxis: { type: 'value' },
      series: [
        { name: '活跃用户', type: 'line', areaStyle: {}, data: activeData.value.map((d) => d.value), smooth: true, itemStyle: { color: '#2563eb' } },
      ],
    })
  }
}

onMounted(loadData)
onBeforeUnmount(disposeCharts)
</script>

<style scoped lang="scss">
.stat-cards {
  display: grid;
  grid-template-columns: repeat(6, 1fr);
  gap: 12px;
  margin-bottom: 20px;
}

.stat-card,
.chart-panel {
  border: 1px solid #e2e8f0;
  border-radius: 10px;
  background: #ffffff;
}

.stat-card {
  padding: 16px;

  span,
  strong,
  small {
    display: block;
  }

  span {
    color: #94a3b8;
    font-size: 12px;
    font-weight: 600;
    text-transform: uppercase;
    letter-spacing: 0.3px;
  }

  small {
    color: #94a3b8;
    font-size: 12px;
    margin-top: 4px;
  }

  strong {
    margin-top: 8px;
    color: #0f172a;
    font-size: 24px;
    font-weight: 800;
  }
}

.chart-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 16px;
}

.chart-panel {
  padding: 20px;

  &.wide {
    grid-column: 1 / -1;
  }

  h2 {
    margin: 0 0 14px;
    color: #0f172a;
    font-size: 15px;
    font-weight: 700;
  }
}

.chart-box {
  height: 300px;
}

@media (max-width: 1100px) {
  .stat-cards,
  .chart-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}
</style>
