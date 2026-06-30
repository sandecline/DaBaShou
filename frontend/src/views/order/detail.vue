<template>
  <div class="order-detail-page">
    <div class="page-container">
      <LoadingSpinner v-if="loading" text="加载中..." fullscreen />

      <template v-else-if="order">
        <el-button text @click="$router.back()" class="back-btn">
          <el-icon><ArrowLeft /></el-icon>
          返回
        </el-button>

        <div class="detail-card">
          <!-- 状态步骤条 -->
          <div class="status-section">
            <el-steps :active="activeStep" align-center finish-status="success">
              <el-step title="待支付" :status="stepStatus(1)" />
              <el-step title="已支付" :status="stepStatus(2)" />
              <el-step title="服务中" :status="stepStatus(3)" />
              <el-step title="待确认" :status="stepStatus(4)" />
              <el-step title="已完成" :status="stepStatus(5)" />
            </el-steps>

            <!-- 异常状态 -->
            <div v-if="isAbnormal" class="abnormal-status">
              <el-alert
                :title="abnormalTitle"
                :type="order.status === 7 ? 'error' : 'warning'"
                :closable="false"
                show-icon
              />
            </div>
          </div>

          <!-- 订单基本信息 -->
          <div class="info-section">
            <div class="info-row">
              <span class="info-label">订单号</span>
              <span class="info-value">{{ order.orderNo }}</span>
            </div>
            <div class="info-row">
              <span class="info-label">服务</span>
              <span class="info-value">{{ order.title }}</span>
            </div>
            <div class="info-row">
              <span class="info-label">积分金额</span>
              <span class="info-value price">{{ order.pointAmount }} 积分</span>
            </div>
            <div class="info-row">
              <span class="info-label">创建时间</span>
              <span>{{ formatDateTime(order.createTime) }}</span>
            </div>
            <div v-if="order.serviceStartTime" class="info-row">
              <span class="info-label">服务时间</span>
              <span>{{ formatDateTime(order.serviceStartTime) }}</span>
            </div>
            <div v-if="order.cancelReason" class="info-row">
              <span class="info-label">取消原因</span>
              <span class="text-danger">{{ order.cancelReason }}</span>
            </div>
          </div>

          <!-- 双方信息 -->
          <div class="users-section">
            <div class="user-box">
              <el-avatar :size="40" :src="''">{{ (order.buyerNickname || '买').charAt(0) }}</el-avatar>
              <div>
                <div class="user-role">买家</div>
                <div class="user-name">{{ order.buyerNickname }}</div>
              </div>
            </div>
            <el-icon :size="20"><Right /></el-icon>
            <div class="user-box">
              <el-avatar :size="40" :src="''">{{ (order.sellerNickname || '卖').charAt(0) }}</el-avatar>
              <div>
                <div class="user-role">卖家</div>
                <div class="user-name">{{ order.sellerNickname }}</div>
              </div>
            </div>
          </div>

          <!-- 核销码 -->
          <div v-if="order.status === 2 || order.status === 3" class="verify-section">
            <el-divider />
            <VerifyCode
              v-if="order.verifyCode"
              mode="display"
              :code="order.verifyCode"
              :expire-at="order.verifyCodeExpire"
            />
          </div>

          <!-- 操作按钮 -->
          <div class="actions-section">
            <template v-if="order.status === 1">
              <el-button type="primary" size="large" @click="handlePay">支付（{{ order.pointAmount }} 积分）</el-button>
              <el-button size="large" @click="handleCancel">取消订单</el-button>
            </template>

            <template v-if="order.status === 2">
              <el-button type="primary" size="large" @click="handleStartService">开始服务</el-button>
            </template>

            <template v-if="order.status === 3 || order.status === 4">
              <el-button type="success" size="large" @click="$router.push(`/order/${order.id}/verify`)">
                {{ order.status === 4 ? '确认完成' : '核销确认' }}
              </el-button>
            </template>

            <template v-if="order.status === 7">
              <el-button type="primary" size="large" @click="$router.push('/credit/appeal')">
                发起申诉
              </el-button>
            </template>
          </div>
        </div>
      </template>

      <EmptyState v-else icon="🔍" title="订单不存在" />
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getOrderDetail, payOrder, startService, cancelOrder } from '@/api/order'
import { formatDateTime, getOrderStatusText } from '@/utils/format'
import VerifyCode from '@/components/common/VerifyCode.vue'
import EmptyState from '@/components/common/EmptyState.vue'
import LoadingSpinner from '@/components/common/LoadingSpinner.vue'
import type { OrderDetailVo, OrderStatus } from '@/types/api'

const props = defineProps<{ id: string }>()
const router = useRouter()

const loading = ref(true)
const order = ref<OrderDetailVo | null>(null)

const activeStep = computed(() => {
  if (!order.value) return 0
  const status = order.value.status
  if (status >= 5) return 5
  if (status >= 4) return 4
  if (status >= 3) return 3
  if (status >= 2) return 2
  if (status >= 1) return 1
  return 0
})

const isAbnormal = computed(() => [0, 6, 7].includes(order.value?.status ?? -1))
const abnormalTitle = computed(() => {
  if (!order.value) return ''
  return `订单状态：${getOrderStatusText(order.value.status as OrderStatus)}`
})

function stepStatus(step: number) {
  if (!order.value) return ''
  const status = order.value.status
  if (status === 0) return step <= 1 ? 'error' : 'wait'
  if (status === 6) return step <= 2 ? 'error' : 'wait'
  if (status === 7) return step >= 4 ? 'error' : 'finish'
  return ''
}

async function fetchDetail() {
  loading.value = true
  try {
    order.value = await getOrderDetail(Number(props.id))
  } catch {
    order.value = null
  } finally {
    loading.value = false
  }
}

async function handlePay() {
  if (!order.value) return
  try {
    await ElMessageBox.confirm(`确认支付 ${order.value.pointAmount} 积分？`, '确认支付', {
      confirmButtonText: '确认支付',
      type: 'warning',
    })
    await payOrder(order.value.id)
    ElMessage.success('支付成功！积分已冻结至担保池')
    fetchDetail()
  } catch {
    // cancelled
  }
}

async function handleStartService() {
  if (!order.value) return
  try {
    await startService(order.value.id)
    ElMessage.success('服务已开始！')
    fetchDetail()
  } catch {
    // handled
  }
}

async function handleCancel() {
  if (!order.value) return
  try {
    const { value: reason } = await ElMessageBox.prompt('请输入取消原因', '取消订单', {
      confirmButtonText: '确认取消',
      cancelButtonText: '再看看',
    })
    await cancelOrder(order.value.id, reason || undefined)
    ElMessage.success('订单已取消')
    fetchDetail()
  } catch {
    // cancelled
  }
}

onMounted(fetchDetail)
</script>

<style scoped lang="scss">
.back-btn {
  margin-bottom: $spacing-md;
}

.detail-card {
  max-width: 780px;
  margin: 0 auto;
  background: #ffffff;
  border-radius: $radius-lg;
  padding: $spacing-xl;
  box-shadow: $shadow-sm;
  border: 1px solid $color-border;
}

.status-section {
  margin-bottom: $spacing-xl;

  .abnormal-status {
    margin-top: $spacing-md;
  }
}

.info-section {
  .info-row {
    display: flex;
    justify-content: space-between;
    padding: 10px 0;
    border-bottom: 1px solid $color-border-light;

    .info-label {
      color: $color-text-secondary;
      font-size: $font-size-sm;
    }

    .info-value {
      font-weight: 500;

      &.price {
        color: $color-warning;
        font-weight: 700;
      }
    }
  }
}

.users-section {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 16px;
  padding: $spacing-lg 0;

  .user-box {
    display: flex;
    align-items: center;
    gap: 10px;

    .user-role {
      font-size: $font-size-xs;
      color: $color-text-secondary;
    }

    .user-name {
      font-weight: 600;
    }
  }
}

.actions-section {
  display: flex;
  gap: 12px;
  justify-content: center;
  padding-top: $spacing-lg;
  border-top: 1px solid $color-border-light;
}

.text-danger {
  color: $color-danger;
}
</style>
