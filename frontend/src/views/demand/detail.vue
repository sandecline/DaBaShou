<template>
  <div class="demand-detail-page">
    <div class="page-container">
      <LoadingSpinner v-if="loading" text="加载中..." fullscreen />

      <template v-else-if="demand">
        <el-button text @click="$router.back()" class="back-btn">
          <el-icon><ArrowLeft /></el-icon>
          返回
        </el-button>

        <div class="detail-layout">
          <div class="detail-main">
            <el-card class="detail-card">
              <div class="detail-header">
                <div class="header-tags">
                  <el-tag v-if="demand.isUrgent" type="danger" size="small" effect="dark">急单</el-tag>
                  <el-tag type="info" size="small" effect="plain">{{ demand.tagName || '求助' }}</el-tag>
                  <span :class="['status-badge', statusClass]">{{ statusText }}</span>
                </div>
                <div class="reward-box">
                  <span class="reward-num">{{ demand.pointReward }}</span>
                  <span class="reward-txt">积分悬赏</span>
                </div>
              </div>

              <h1 class="detail-title">{{ demand.title }}</h1>

              <div class="detail-meta">
                <div class="meta-item">
                  <span class="meta-label">服务方式</span>
                  <span>{{ ['', '线上', '线下', '均可'][demand.locationType] }}</span>
                </div>
                <div class="meta-item">
                  <span class="meta-label">截止时间</span>
                  <span :class="{ 'text-danger': demand.isUrgent }">{{ demand.deadline ? formatDateTime(demand.deadline) : '不限' }}</span>
                </div>
                <div class="meta-item">
                  <span class="meta-label">发布时间</span>
                  <span>{{ formatDateTime(demand.createTime) }}</span>
                </div>
                <div class="meta-item">
                  <span class="meta-label">校区/楼栋</span>
                  <span>{{ demand.campus || '未设置' }} {{ demand.building || '' }}</span>
                </div>
              </div>

              <div class="detail-desc">
                <h3>需求描述</h3>
                <p>{{ demand.description || '暂无详细描述' }}</p>
              </div>
            </el-card>
          </div>

          <div class="detail-sidebar">
            <el-card class="user-card">
              <div class="user-info">
                <el-avatar :size="56" :src="demand.userAvatar">
                  {{ (demand.userName || '?').charAt(0) }}
                </el-avatar>
                <div class="user-detail">
                  <span class="user-name">{{ demand.userName || '匿名用户' }}</span>
                  <span class="user-campus">{{ demand.campus || '' }}</span>
                </div>
              </div>

              <el-divider />

              <el-button
                v-if="demand.status === 1"
                type="primary"
                size="large"
                class="take-btn"
                @click="handleTakeOrder"
              >
                接单帮助TA
              </el-button>
              <el-tag v-else size="large" style="width: 100%; text-align: center; display: block;">
                {{ statusMap[demand.status] }}
              </el-tag>
            </el-card>
          </div>
        </div>
      </template>

      <EmptyState v-else icon="🔍" title="需求不存在或已关闭" />
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getDemandDetail } from '@/api/demand'
import { createOrderFromDemand } from '@/api/order'
import { formatDateTime } from '@/utils/format'
import EmptyState from '@/components/common/EmptyState.vue'
import LoadingSpinner from '@/components/common/LoadingSpinner.vue'
import type { Demand } from '@/types/api'

const props = defineProps<{ id: string }>()
const router = useRouter()

const loading = ref(true)
const demand = ref<Demand | null>(null)

const statusMap: Record<number, string> = { 0: '已关闭', 1: '待接单', 2: '进行中', 3: '已完成' }
const statusClassMap: Record<number, string> = { 0: 'status-closed', 1: 'status-open', 2: 'status-active', 3: 'status-done' }
const statusText = computed(() => demand.value ? statusMap[demand.value.status] : '')
const statusClass = computed(() => demand.value ? statusClassMap[demand.value.status] : '')

async function fetchDetail() {
  loading.value = true
  try {
    demand.value = await getDemandDetail(Number(props.id))
    if (demand.value.deadline) {
      demand.value.isUrgent = new Date(demand.value.deadline).getTime() - Date.now() < 12 * 3600 * 1000
    }
  } catch {
    demand.value = null
  } finally {
    loading.value = false
  }
}

async function handleTakeOrder() {
  if (!demand.value) return
  try {
    await ElMessageBox.confirm(
      `确认接单帮助TA？这将消耗你的一次接单机会。`,
      '确认接单',
      { confirmButtonText: '确认接单', cancelButtonText: '再看看', type: 'warning' },
    )
    const orderId = await createOrderFromDemand({ demandId: demand.value.id })
    demand.value.status = 2
    ElMessage.success('接单成功！请前往订单页查看')
    router.replace('/order/' + orderId)
  } catch (e: any) {
    if (e !== 'cancel' && e !== 'close') {
      // Error already shown by interceptor, or user cancelled dialog
    }
  }
}

onMounted(fetchDetail)
</script>

<style scoped lang="scss">
.back-btn {
  margin-bottom: $spacing-md;
}

.detail-layout {
  display: grid;
  grid-template-columns: 1fr 340px;
  gap: $spacing-lg;
  align-items: start;
}

.detail-card {
  padding: $spacing-md;
}

.detail-header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;

  .header-tags {
    display: flex;
    align-items: center;
    gap: 6px;

    .status-badge {
      font-size: $font-size-xs;
      padding: 2px 8px;
      border-radius: 4px;

      &.status-open { background: #ecfdf5; color: $color-success; }
      &.status-active { background: #FFF8E1; color: $color-primary-dark; }
      &.status-closed { background: $color-bg; color: $color-text-secondary; }
      &.status-done { background: #f1f5f9; color: #64748b; }
    }
  }

  .reward-box {
    text-align: center;
    padding: 8px 16px;
    background: #fef2f2;
    border-radius: 8px;

    .reward-num {
      display: block;
      font-size: $font-size-xl;
      font-weight: 700;
      color: $color-danger;
    }

    .reward-txt {
      font-size: $font-size-xs;
      color: #dc2626;
      opacity: 0.7;
    }
  }
}

.detail-title {
  font-size: $font-size-2xl;
  font-weight: 700;
  margin: $spacing-md 0;
}

.detail-meta {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: $spacing-md;
  background: $color-bg;
  border-radius: $radius-md;
  padding: $spacing-md;

  .meta-item {
    display: flex;
    flex-direction: column;
    gap: 4px;

    .meta-label {
      font-size: $font-size-xs;
      color: $color-text-secondary;
    }

    span:last-child {
      font-weight: 600;
    }
  }
}

.text-danger {
  color: $color-danger;
}

.detail-desc {
  margin-top: $spacing-lg;
  padding-top: $spacing-lg;
  border-top: 1px solid $color-border-light;

  h3 {
    font-size: $font-size-base;
    font-weight: 600;
    margin: 0 0 8px;
  }

  p {
    color: $color-text-regular;
    line-height: 1.8;
    white-space: pre-wrap;
  }
}

// Sidebar
.user-card {
  text-align: center;

  .user-info {
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 10px;
  }

  .user-detail {
    .user-name {
      font-weight: 600;
      display: block;
    }

    .user-campus {
      font-size: $font-size-xs;
      color: $color-text-secondary;
    }
  }

  .take-btn {
    width: 100%;
  }
}

@media (max-width: 768px) {
  .detail-layout {
    grid-template-columns: 1fr;
  }
}
</style>
