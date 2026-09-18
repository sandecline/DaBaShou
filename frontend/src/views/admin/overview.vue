<template>
  <AdminLayout
    title="运营总览"
    subtitle="平台运行状态与待处理事项"
  >
    <template #actions>
      <el-button :icon="Refresh" :loading="loading" @click="loadData">刷新数据</el-button>
    </template>

    <LoadingSpinner v-if="loading" text="加载中..." />

    <template v-else>
      <!-- 待处理事项 -->
      <div class="pending-bar">
        <router-link v-for="item in pendingItems" :key="item.label" :to="item.path" class="pending-item" :style="{ '--accent': item.color }">
          <div class="pending-value">{{ item.value }}</div>
          <div class="pending-label">{{ item.label }}</div>
          <el-icon class="pending-arrow"><ArrowRight /></el-icon>
        </router-link>
      </div>

      <!-- 核心指标 -->
      <div class="metrics-grid">
        <div v-for="item in metricItems" :key="item.label" class="metric-card">
          <div class="metric-header">
            <span class="metric-label">{{ item.label }}</span>
            <el-icon class="metric-icon" :style="{ color: item.color }"><component :is="item.icon" /></el-icon>
          </div>
          <div class="metric-value">{{ item.value }}</div>
          <div class="metric-hint">{{ item.hint }}</div>
        </div>
      </div>

      <!-- 底部区域 -->
      <div class="bottom-grid">
        <!-- 待办事项 -->
        <section class="panel">
          <div class="panel-header">
            <h2>待办事项</h2>
            <el-tag v-if="todoCount > 0" type="danger" size="small" effect="dark">{{ todoCount }} 项</el-tag>
            <el-tag v-else type="success" size="small" effect="dark">全部完成</el-tag>
          </div>
          <div class="todo-list">
            <div v-for="item in todoItems" :key="item.label" class="todo-row" :class="{ 'is-done': item.done }">
              <div class="todo-status">
                <el-icon v-if="item.done"><CircleCheck /></el-icon>
                <el-icon v-else><Warning /></el-icon>
              </div>
              <span class="todo-label">{{ item.label }}</span>
              <el-tag v-if="item.done" type="success" size="small">正常</el-tag>
              <el-tag v-else type="warning" size="small">需处理</el-tag>
            </div>
          </div>
        </section>

        <!-- 快捷操作 -->
        <section class="panel">
          <div class="panel-header">
            <h2>快捷操作</h2>
          </div>
          <div class="quick-actions">
            <router-link to="/admin/orders" class="quick-btn">
              <el-icon><Tickets /></el-icon>
              <span>处理争议订单</span>
            </router-link>
            <router-link to="/admin/campus-auths" class="quick-btn">
              <el-icon><Checked /></el-icon>
              <span>审核校园认证</span>
            </router-link>
            <router-link to="/admin/credit" class="quick-btn">
              <el-icon><Warning /></el-icon>
              <span>查看信用申诉</span>
            </router-link>
            <router-link to="/admin/system" class="quick-btn">
              <el-icon><Setting /></el-icon>
              <span>调整运营规则</span>
            </router-link>
            <router-link to="/admin/users" class="quick-btn">
              <el-icon><User /></el-icon>
              <span>用户管理</span>
            </router-link>
            <router-link to="/admin/stat" class="quick-btn">
              <el-icon><DataAnalysis /></el-icon>
              <span>数据统计</span>
            </router-link>
          </div>
        </section>
      </div>
    </template>
  </AdminLayout>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { Refresh } from '@element-plus/icons-vue'
import { getAdminOverview } from '@/api/admin'
import AdminLayout from '@/components/layout/AdminLayout.vue'
import LoadingSpinner from '@/components/common/LoadingSpinner.vue'
import type { AdminOverviewVo } from '@/types/api'

const loading = ref(false)
const overview = ref<AdminOverviewVo | null>(null)

const pendingItems = computed(() => [
  { label: '争议订单', value: overview.value?.disputingOrders ?? 0, path: '/admin/orders', color: '#f59e0b' },
  { label: '待审认证', value: overview.value?.pendingCampusAuths ?? 0, path: '/admin/campus-auths', color: '#3b82f6' },
  { label: '待处理违规', value: overview.value?.pendingViolations ?? 0, path: '/admin/credit', color: '#ef4444' },
  { label: '待审申诉', value: overview.value?.pendingAppeals ?? 0, path: '/admin/credit', color: '#8b5cf6' },
])

const metricItems = computed(() => [
  { label: '注册用户', value: overview.value?.totalUsers ?? 0, hint: `今日新增 ${overview.value?.todayNewUsers ?? 0}`, icon: 'User', color: '#3b82f6' },
  { label: '订单总数', value: overview.value?.totalOrders ?? 0, hint: `今日新增 ${overview.value?.todayNewOrders ?? 0}`, icon: 'Tickets', color: '#f59e0b' },
  { label: '技能服务', value: overview.value?.totalSkills ?? overview.value?.totalShelves ?? 0, hint: '当前服务供给', icon: 'Briefcase', color: '#10b981' },
  { label: '求助需求', value: overview.value?.totalDemands ?? 0, hint: '待接单需求', icon: 'Collection', color: '#8b5cf6' },
  { label: '完成订单', value: overview.value?.completedOrders ?? 0, hint: `完成率 ${Math.round((overview.value?.orderCompletionRate ?? 0) * 100)}%`, icon: 'CircleCheck', color: '#06b6d4' },
  { label: '积分流通', value: overview.value?.totalPointsInCirculation ?? 0, hint: '用户积分余额', icon: 'Coin', color: '#ec4899' },
])

const todoItems = computed(() => [
  { label: '争议订单积压', done: (overview.value?.disputingOrders ?? 0) === 0 },
  { label: '校园认证审核', done: (overview.value?.pendingCampusAuths ?? 0) === 0 },
  { label: '违规记录处理', done: (overview.value?.pendingViolations ?? 0) === 0 },
  { label: '申诉审核处理', done: (overview.value?.pendingAppeals ?? 0) === 0 },
])

const todoCount = computed(() => todoItems.value.filter(i => !i.done).length)

async function loadData() {
  loading.value = true
  try {
    overview.value = await getAdminOverview()
  } catch {
    // handled
  } finally {
    loading.value = false
  }
}

onMounted(loadData)
</script>

<style scoped lang="scss">
/* ===== 待处理事项栏 ===== */
.pending-bar {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 12px;
  margin-bottom: 20px;
}

.pending-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 16px 18px;
  background: #ffffff;
  border-radius: 10px;
  border: 1px solid #e2e8f0;
  border-left: 4px solid var(--accent, #3b82f6);
  text-decoration: none;
  transition: all 0.15s ease;

  &:hover {
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.06);
    transform: translateY(-1px);
  }
}

.pending-value {
  font-size: 28px;
  font-weight: 800;
  color: #0f172a;
  line-height: 1;
  min-width: 36px;
}

.pending-label {
  flex: 1;
  font-size: 13px;
  color: #64748b;
  font-weight: 500;
}

.pending-arrow {
  color: #cbd5e1;
  font-size: 16px;
}

/* ===== 核心指标 ===== */
.metrics-grid {
  display: grid;
  grid-template-columns: repeat(6, 1fr);
  gap: 12px;
  margin-bottom: 20px;
}

.metric-card {
  background: #ffffff;
  border-radius: 10px;
  border: 1px solid #e2e8f0;
  padding: 16px;
}

.metric-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 10px;
}

.metric-label {
  font-size: 12px;
  color: #94a3b8;
  font-weight: 600;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.metric-icon {
  font-size: 20px;
  opacity: 0.7;
}

.metric-value {
  font-size: 26px;
  font-weight: 800;
  color: #0f172a;
  line-height: 1;
}

.metric-hint {
  margin-top: 6px;
  font-size: 12px;
  color: #94a3b8;
}

/* ===== 底部区域 ===== */
.bottom-grid {
  display: grid;
  grid-template-columns: 1fr 320px;
  gap: 16px;
}

.panel {
  background: #ffffff;
  border-radius: 10px;
  border: 1px solid #e2e8f0;
  padding: 20px;
}

.panel-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 16px;

  h2 {
    margin: 0;
    font-size: 15px;
    font-weight: 700;
    color: #0f172a;
  }
}

/* ===== 待办列表 ===== */
.todo-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.todo-row {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 12px 14px;
  border-radius: 8px;
  background: #fffbeb;
  border: 1px solid #fef3c7;

  &.is-done {
    background: #f0fdf4;
    border-color: #bbf7d0;
  }
}

.todo-status {
  width: 24px;
  height: 24px;
  display: grid;
  place-items: center;
  border-radius: 50%;
  font-size: 16px;

  .todo-row:not(.is-done) & {
    color: #f59e0b;
  }

  .todo-row.is-done & {
    color: #22c55e;
  }
}

.todo-label {
  flex: 1;
  font-size: 14px;
  color: #334155;
  font-weight: 500;
}

/* ===== 快捷操作 ===== */
.quick-actions {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 8px;
}

.quick-btn {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 12px 14px;
  border-radius: 8px;
  border: 1px solid #e2e8f0;
  color: #334155;
  font-size: 13px;
  font-weight: 600;
  text-decoration: none;
  transition: all 0.15s ease;

  .el-icon {
    font-size: 16px;
    color: #3b82f6;
  }

  &:hover {
    background: #f0f9ff;
    border-color: #93c5fd;
    color: #1d4ed8;
  }
}

/* ===== 响应式 ===== */
@media (max-width: 1200px) {
  .metrics-grid {
    grid-template-columns: repeat(3, 1fr);
  }
}

@media (max-width: 900px) {
  .pending-bar {
    grid-template-columns: repeat(2, 1fr);
  }

  .metrics-grid {
    grid-template-columns: repeat(2, 1fr);
  }

  .bottom-grid {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 600px) {
  .pending-bar {
    grid-template-columns: 1fr;
  }

  .metrics-grid {
    grid-template-columns: 1fr 1fr;
  }
}
</style>
