<template>
  <AdminLayout
    title="运营总览"
    eyebrow="TODAY'S WORK"
    subtitle="集中查看待处理事项和平台运行状态。"
  >
    <template #actions>
      <el-button :icon="Refresh" :loading="loading" @click="loadData">刷新</el-button>
    </template>

    <LoadingSpinner v-if="loading" text="加载中..." />

    <template v-else>
      <div class="ops-strip">
        <router-link v-for="item in pendingItems" :key="item.label" :to="item.path" class="ops-item">
          <span class="ops-value">{{ item.value }}</span>
          <span class="ops-label">{{ item.label }}</span>
        </router-link>
      </div>

      <div class="metric-grid">
        <div v-for="item in metricItems" :key="item.label" class="metric-card">
          <span class="metric-label">{{ item.label }}</span>
          <strong>{{ item.value }}</strong>
          <small>{{ item.hint }}</small>
        </div>
      </div>

      <section class="workbench">
        <div class="workbench-main">
          <h2>今日巡检</h2>
          <div class="checklist">
            <div v-for="item in checklist" :key="item.label" class="check-row">
              <el-icon><component :is="item.icon" /></el-icon>
              <span>{{ item.label }}</span>
              <el-tag :type="item.done ? 'success' : 'warning'" size="small">
                {{ item.done ? '正常' : '需处理' }}
              </el-tag>
            </div>
          </div>
        </div>

        <div class="workbench-side">
          <h2>快捷入口</h2>
          <router-link to="/admin/orders" class="quick-link">处理争议订单</router-link>
          <router-link to="/admin/campus-auths" class="quick-link">审核校园认证</router-link>
          <router-link to="/admin/credit" class="quick-link">查看信用申诉</router-link>
          <router-link to="/admin/system" class="quick-link">调整运营规则</router-link>
        </div>
      </section>
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
  { label: '争议订单', value: overview.value?.disputingOrders ?? 0, path: '/admin/orders' },
  { label: '待审认证', value: overview.value?.pendingCampusAuths ?? 0, path: '/admin/campus-auths' },
  { label: '待处理违规', value: overview.value?.pendingViolations ?? 0, path: '/admin/credit' },
  { label: '待审申诉', value: overview.value?.pendingAppeals ?? 0, path: '/admin/credit' },
])

const metricItems = computed(() => [
  { label: '注册用户', value: overview.value?.totalUsers ?? 0, hint: `今日新增 ${overview.value?.todayNewUsers ?? 0}` },
  { label: '订单总数', value: overview.value?.totalOrders ?? 0, hint: `今日新增 ${overview.value?.todayNewOrders ?? 0}` },
  { label: '技能服务', value: overview.value?.totalSkills ?? overview.value?.totalShelves ?? 0, hint: '当前服务供给' },
  { label: '求助需求', value: overview.value?.totalDemands ?? 0, hint: '待接单需求' },
  { label: '完成订单', value: overview.value?.completedOrders ?? 0, hint: `完成率 ${Math.round((overview.value?.orderCompletionRate ?? 0) * 100)}%` },
  { label: '积分流通', value: overview.value?.totalPointsInCirculation ?? 0, hint: '用户积分余额' },
])

const checklist = computed(() => [
  { label: '争议订单积压', icon: 'Tickets', done: (overview.value?.disputingOrders ?? 0) === 0 },
  { label: '校园认证审核', icon: 'Checked', done: (overview.value?.pendingCampusAuths ?? 0) === 0 },
  { label: '违规记录处理', icon: 'Warning', done: (overview.value?.pendingViolations ?? 0) === 0 },
  { label: '申诉审核处理', icon: 'ChatDotRound', done: (overview.value?.pendingAppeals ?? 0) === 0 },
])

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
.ops-strip,
.metric-grid {
  display: grid;
  gap: 12px;
}

.ops-strip {
  grid-template-columns: repeat(4, 1fr);
  margin-bottom: 18px;
}

.ops-item,
.metric-card,
.workbench-main,
.workbench-side {
  border: 1px solid #dfe7e3;
  border-radius: 8px;
  background: #ffffff;
}

.ops-item {
  display: flex;
  align-items: baseline;
  justify-content: space-between;
  padding: 15px 16px;
  color: #17211f;

  &:hover {
    border-color: #0f766e;
  }
}

.ops-value {
  color: #b45309;
  font-size: 28px;
  font-weight: 850;
}

.ops-label,
.metric-label {
  color: #667a74;
  font-size: 13px;
}

.metric-grid {
  grid-template-columns: repeat(6, 1fr);
}

.metric-card {
  padding: 15px;

  strong,
  small {
    display: block;
  }

  strong {
    margin-top: 8px;
    color: #17211f;
    font-size: 24px;
  }

  small {
    margin-top: 4px;
    color: #7b8b86;
  }
}

.workbench {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 300px;
  gap: 16px;
  margin-top: 18px;
}

.workbench-main,
.workbench-side {
  padding: 18px;

  h2 {
    margin: 0 0 14px;
    color: #17211f;
    font-size: 17px;
  }
}

.checklist {
  display: grid;
  gap: 10px;
}

.check-row {
  display: grid;
  grid-template-columns: 24px minmax(0, 1fr) auto;
  align-items: center;
  gap: 10px;
  padding: 12px;
  border-radius: 8px;
  background: #f6f8f7;
}

.quick-link {
  display: block;
  padding: 12px 0;
  border-top: 1px solid #edf2ef;
  color: #0f766e;
  font-weight: 700;
}

@media (max-width: 1100px) {
  .ops-strip,
  .metric-grid {
    grid-template-columns: repeat(2, 1fr);
  }

  .workbench {
    grid-template-columns: 1fr;
  }
}
</style>
