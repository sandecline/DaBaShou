<template>
  <div class="stat-page">
    <div class="page-container">
      <h2 class="page-title">数据统计</h2>

      <LoadingSpinner v-if="loading" text="加载中..." />

      <template v-else>
        <!-- 统计卡片 -->
        <div class="stat-cards">
          <div class="stat-card" v-for="item in statCards" :key="item.label">
            <div class="stat-card-value">{{ item.value }}</div>
            <div class="stat-card-label">{{ item.label }}</div>
          </div>
        </div>

        <!-- 图表区域 -->
        <el-row :gutter="16">
          <el-col :span="12">
            <div class="chart-card">
              <h3>近7日活跃趋势</h3>
              <div ref="lineChartRef" class="chart-box" />
            </div>
          </el-col>
          <el-col :span="12">
            <div class="chart-card">
              <h3>技能热度排行</h3>
              <div ref="barChartRef" class="chart-box" />
            </div>
          </el-col>
        </el-row>

        <el-row :gutter="16" style="margin-top: 16px">
          <el-col :span="12">
            <div class="chart-card">
              <h3>订单完成率</h3>
              <div ref="pieChartRef" class="chart-box" />
            </div>
          </el-col>
          <el-col :span="12">
            <div class="chart-card">
              <h3>个人数据概览</h3>
              <div class="user-stats">
                <div class="user-stat-item">
                  <span class="stat-num">{{ userStat.publishedSkills }}</span>
                  <span class="stat-text">已发布技能</span>
                </div>
                <div class="user-stat-item">
                  <span class="stat-num">{{ userStat.publishedDemands }}</span>
                  <span class="stat-text">已发布需求</span>
                </div>
                <div class="user-stat-item">
                  <span class="stat-num">{{ userStat.completedOrders }}</span>
                  <span class="stat-text">完成订单</span>
                </div>
                <div class="user-stat-item">
                  <span class="stat-num">{{ (userStat.averageRating ?? 0).toFixed(1) }}</span>
                  <span class="stat-text">平均评分</span>
                </div>
              </div>
            </div>
          </el-col>
        </el-row>
      </template>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, nextTick } from 'vue'
import { getUserStat, getDailySummary, getSkillHeat } from '@/api/stat'
import LoadingSpinner from '@/components/common/LoadingSpinner.vue'
import * as echarts from 'echarts'
import type { UserStat, DailySummary, SkillHeat } from '@/types'

const loading = ref(true)
const lineChartRef = ref()
const barChartRef = ref()
const pieChartRef = ref()

const userStat = ref<UserStat>({
  publishedSkills: 0,
  publishedDemands: 0,
  takenOrders: 0,
  completedOrders: 0,
  averageRating: 0,
  totalPointsEarned: 0,
  totalPointsSpent: 0,
})

const statCards = ref([
  { label: '已发布技能', value: '0' },
  { label: '已发布需求', value: '0' },
  { label: '完成订单', value: '0' },
  { label: '平均评分', value: '0' },
])

async function loadData() {
  loading.value = true
  try {
    const [userStatResult, dailyData, heatData] = await Promise.all([
      getUserStat().catch(() => null),
      getDailySummary(7).catch(() => []),
      getSkillHeat().catch(() => []),
    ])

    if (userStatResult) {
      userStat.value = userStatResult
      statCards.value = [
        { label: '已发布技能', value: String(userStatResult.publishedSkills) },
        { label: '已发布需求', value: String(userStatResult.publishedDemands) },
        { label: '完成订单', value: String(userStatResult.completedOrders) },
        { label: '平均评分', value: (userStatResult.averageRating ?? 0).toFixed(1) },
      ]
    }

    await nextTick()
    if (dailyData.length > 0) initLineChart(dailyData)
    if (heatData.length > 0) initBarChart(heatData)
    if (userStatResult) initPieChart(userStatResult)
  } catch {
    // handled
  } finally {
    loading.value = false
  }
}

function initLineChart(data: DailySummary[]) {
  if (!lineChartRef.value) return
  const chart = echarts.init(lineChartRef.value)
  chart.setOption({
    tooltip: { trigger: 'axis' },
    xAxis: { type: 'category', data: data.map((d) => d.date.slice(5)) },
    yAxis: { type: 'value' },
    series: [
      {
        name: '新增订单',
        type: 'line',
        data: data.map((d) => d.newOrders),
        smooth: true,
        itemStyle: { color: '#FFC300' },
      },
      {
        name: '完成订单',
        type: 'line',
        data: data.map((d) => d.completedOrders),
        smooth: true,
        itemStyle: { color: '#10b981' },
      },
    ],
  })
}

function initBarChart(data: SkillHeat[]) {
  if (!barChartRef.value) return
  const chart = echarts.init(barChartRef.value)
  chart.setOption({
    tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
    xAxis: { type: 'category', data: data.slice(0, 10).map((d) => d.tagName) },
    yAxis: { type: 'value' },
    series: [
      {
        name: '热度',
        type: 'bar',
        data: data.slice(0, 10).map((d) => d.heatScore),
        itemStyle: { color: '#FFC300', borderRadius: [4, 4, 0, 0] },
      },
    ],
  })
}

function initPieChart(stat: UserStat) {
  if (!pieChartRef.value) return
  const chart = echarts.init(pieChartRef.value)
  chart.setOption({
    tooltip: { trigger: 'item' },
    series: [
      {
        type: 'pie',
        radius: ['40%', '70%'],
        data: [
          { value: stat.completedOrders, name: '已完成', itemStyle: { color: '#10b981' } },
          { value: stat.takenOrders - stat.completedOrders, name: '进行中', itemStyle: { color: '#FFC300' } },
        ],
      },
    ],
  })
}

onMounted(loadData)
</script>

<style scoped lang="scss">
.page-title {
  font-size: $font-size-xl;
  font-weight: 700;
  margin: 0 0 $spacing-md;
}

.stat-cards {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: $spacing-md;
  margin-bottom: $spacing-xl;
}

.stat-card {
  background: #ffffff;
  border-radius: $radius-md;
  padding: $spacing-lg;
  text-align: center;
  border: 1px solid $color-border;

  .stat-card-value {
    font-size: 28px;
    font-weight: 700;
    color: $color-primary;
  }

  .stat-card-label {
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
    height: 280px;
  }
}

.user-stats {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: $spacing-md;

  .user-stat-item {
    text-align: center;
    padding: $spacing-lg;
    background: $color-bg;
    border-radius: $radius-md;

    .stat-num {
      display: block;
      font-size: 28px;
      font-weight: 700;
      color: $color-primary;
    }

    .stat-text {
      font-size: $font-size-xs;
      color: $color-text-secondary;
    }
  }
}

@media (max-width: 768px) {
  .stat-cards {
    grid-template-columns: repeat(2, 1fr);
  }
}
</style>
