<template>
  <div class="skill-detail-page">
    <div class="page-container">
      <LoadingSpinner v-if="loading" text="加载技能详情..." fullscreen />

      <template v-else-if="skill">
        <!-- 返回按钮 -->
        <el-button text @click="$router.back()" class="back-btn">
          <el-icon><ArrowLeft /></el-icon>
          返回
        </el-button>

        <div class="detail-layout">
          <!-- 主要内容 -->
          <div class="detail-main">
            <el-card class="detail-card">
              <div class="detail-header">
                <el-tag size="small" effect="plain">{{ skill.tagName || '技能' }}</el-tag>
                <span class="status-tag" :class="statusClass">{{ statusText }}</span>
              </div>

              <h1 class="detail-title">{{ skill.title }}</h1>

              <div class="detail-meta">
                <div class="meta-item">
                  <span class="meta-label">积分价格</span>
                  <span class="meta-value price">{{ skill.pointPrice }} 积分</span>
                </div>
                <div class="meta-item">
                  <span class="meta-label">预计时长</span>
                  <span class="meta-value">{{ skill.durationMinutes }} 分钟</span>
                </div>
                <div class="meta-item">
                  <span class="meta-label">服务方式</span>
                  <span class="meta-value">{{ ['', '线上', '线下', '均可'][skill.locationType] }}</span>
                </div>
                <div class="meta-item">
                  <span class="meta-label">发布时间</span>
                  <span class="meta-value">{{ formatDateTime(skill.createTime) }}</span>
                </div>
              </div>

              <div class="detail-desc">
                <h3>服务描述</h3>
                <p>{{ skill.description || '暂无详细描述' }}</p>
              </div>
            </el-card>
          </div>

          <!-- 侧边栏 -->
          <div class="detail-sidebar">
            <!-- 发布者信息 -->
            <el-card class="user-card">
              <div class="user-info">
                <router-link :to="`/user/shop/${skill.userId}`">
                  <el-avatar :size="56" :src="skill.userAvatar">
                    {{ (skill.userName || '?').charAt(0) }}
                  </el-avatar>
                </router-link>
                <div class="user-detail">
                  <router-link :to="`/user/shop/${skill.userId}`" class="user-name">
                    {{ skill.userName || '匿名用户' }}
                  </router-link>
                  <TrustBadge :score="skill.trustScore || 0" />
                </div>
              </div>

              <el-divider />

              <el-button type="primary" size="large" class="order-btn" @click="handleOrder">
                预约服务
              </el-button>
            </el-card>

            <!-- 闲时格子 -->
            <el-card v-if="skill.status === 1" class="time-card">
              <template #header>
                <span>可预约时段</span>
              </template>
              <p class="time-hint">选择服务后将显示可预约时段</p>
            </el-card>
          </div>
        </div>
      </template>

      <EmptyState v-else icon="🔍" title="技能不存在或已下架" />
    </div>

    <!-- 下单对话框 -->
    <el-dialog v-model="showOrderDialog" title="确认预约" width="440px" :close-on-click-modal="false">
      <div class="order-dialog-content">
        <div class="order-summary">
          <div class="order-item">
            <span>服务</span>
            <span>{{ skill?.title }}</span>
          </div>
          <div class="order-item">
            <span>积分</span>
            <span class="price">{{ skill?.pointPrice }} 积分</span>
          </div>
        </div>

        <el-divider />

        <el-form label-position="top">
          <el-form-item label="选择时间段">
            <el-select v-model="selectedSlotId" placeholder="请选择时间段" style="width: 100%">
              <el-option
                v-for="slot in availableSlots"
                :key="slot.id"
                :label="`${slot.date} ${slot.startTime}-${slot.endTime}`"
                :value="slot.id"
              />
            </el-select>
          </el-form-item>
        </el-form>
      </div>

      <template #footer>
        <el-button @click="showOrderDialog = false">取消</el-button>
        <el-button type="primary" :loading="ordering" @click="confirmOrder">
          确认预约（{{ skill?.pointPrice }} 积分）
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getShelfDetail } from '@/api/shelf'
import { createOrder } from '@/api/order'
import { formatDateTime } from '@/utils/format'
import TrustBadge from '@/components/common/TrustBadge.vue'
import EmptyState from '@/components/common/EmptyState.vue'
import LoadingSpinner from '@/components/common/LoadingSpinner.vue'
import type { SkillShelf, TimeSlot } from '@/types'

const props = defineProps<{ id: string }>()
const router = useRouter()

const loading = ref(true)
const skill = ref<SkillShelf | null>(null)
const showOrderDialog = ref(false)
const selectedSlotId = ref<number | null>(null)
const availableSlots = ref<TimeSlot[]>([])
const ordering = ref(false)

const statusMap: Record<number, string> = { 0: '已下架', 1: '在售', 2: '审核中' }
const statusClassMap: Record<number, string> = { 0: 'status-down', 1: 'status-on', 2: 'status-review' }
const statusText = ref('')
const statusClass = ref('')

async function fetchDetail() {
  loading.value = true
  try {
    skill.value = await getShelfDetail(Number(props.id))
    statusText.value = statusMap[skill.value.status]
    statusClass.value = statusClassMap[skill.value.status]
  } catch {
    skill.value = null
  } finally {
    loading.value = false
  }
}

function handleOrder() {
  if (!skill.value) return
  showOrderDialog.value = true
}

async function confirmOrder() {
  if (!skill.value) return
  ordering.value = true
  try {
    await createOrder({
      skillShelfId: skill.value.id,
      timeSlotId: selectedSlotId.value ?? undefined,
    })
    ElMessage.success('预约成功！请前往订单页支付')
    router.push('/order')
  } catch {
    // handled
  } finally {
    ordering.value = false
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
  align-items: center;
  gap: 8px;
  margin-bottom: 12px;

  .status-tag {
    font-size: $font-size-xs;
    padding: 2px 8px;
    border-radius: 4px;

    &.status-on { background: #ecfdf5; color: $color-success; }
    &.status-down { background: $color-bg; color: $color-text-secondary; }
    &.status-review { background: #fffbeb; color: $color-warning; }
  }
}

.detail-title {
  font-size: $font-size-2xl;
  font-weight: 700;
  margin: 0 0 $spacing-lg;
  color: $color-text-primary;
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

    .meta-value {
      font-weight: 600;
      color: $color-text-primary;

      &.price {
        color: $color-warning;
        font-size: $font-size-lg;
      }
    }
  }
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
      color: $color-text-primary;
      text-decoration: none;
      display: block;
      margin-bottom: 4px;

      &:hover {
        color: $color-primary;
      }
    }
  }

  .order-btn {
    width: 100%;
  }
}

.time-card {
  margin-top: $spacing-md;

  .time-hint {
    color: $color-text-secondary;
    font-size: $font-size-sm;
    text-align: center;
  }
}

.order-dialog-content {
  .order-summary {
    .order-item {
      display: flex;
      justify-content: space-between;
      padding: 6px 0;
      font-size: $font-size-base;

      .price {
        color: $color-warning;
        font-weight: 600;
      }
    }
  }
}

@media (max-width: 768px) {
  .detail-layout {
    grid-template-columns: 1fr;
  }
}
</style>
