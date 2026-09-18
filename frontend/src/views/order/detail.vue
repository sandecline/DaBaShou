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
              <el-step title="待核销" :status="stepStatus(1)" />
              <el-step title="服务中" :status="stepStatus(3)" />
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
              <div v-if="order.status === 7 && order.disputeReason" class="dispute-reason-box">
                <p><strong>争议原因：</strong>{{ order.disputeReason }}</p>
                <p v-if="order.disputeExplain"><strong>补充说明：</strong>{{ order.disputeExplain }}</p>
              </div>
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
              <span class="info-value">{{ order.shelfTitle }}</span>
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
              <span class="info-label">服务开始</span>
              <span>{{ formatDateTime(order.serviceStartTime) }}</span>
            </div>
            <div v-if="order.completeTime" class="info-row">
              <span class="info-label">完成时间</span>
              <span>{{ formatDateTime(order.completeTime) }}</span>
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

          <!-- 核销码区域 -->
          <div v-if="order.status === 1 || order.status === 3" class="verify-section">
            <el-divider />

            <!-- 阶段1: 开始核销 (1→3) -->
            <div v-if="order.status === 1" class="verify-phase">
              <h4>开始核销</h4>
              <p class="verify-hint">双方输入对方的核销码后，服务正式开始，积分将被冻结</p>

              <div class="verify-codes">
                <div class="code-card">
                  <div class="code-label">我的核销码（交给对方）</div>
                  <div class="code-value">{{ myStartCode }}</div>
                  <el-tag :type="myStartVerified ? 'success' : 'info'" size="small">
                    {{ myStartVerified ? '已核销' : '等待对方输入' }}
                  </el-tag>
                </div>
                <div class="code-card">
                  <div class="code-label">对方的核销码</div>
                  <div class="code-input-area">
                    <el-input
                      v-model="verifyCodeInput"
                      placeholder="输入对方的6位核销码"
                      maxlength="6"
                      :disabled="otherStartVerified"
                    />
                    <el-button
                      type="primary"
                      :disabled="!verifyCodeInput || verifyCodeInput.length < 6 || otherStartVerified"
                      :loading="verifyLoading"
                      @click="handleVerifyStart"
                    >
                      {{ otherStartVerified ? '已核销' : '确认核销' }}
                    </el-button>
                  </div>
                  <el-tag :type="otherStartVerified ? 'success' : 'info'" size="small">
                    {{ otherStartVerified ? '已核销' : '等待输入' }}
                  </el-tag>
                </div>
              </div>
            </div>

            <!-- 阶段2: 完成确认 (3→5) -->
            <div v-if="order.status === 3" class="verify-phase">
              <h4>完成确认</h4>
              <p class="verify-hint">双方输入对方的确认码后，订单完成，积分将结算给卖家</p>

              <div class="verify-codes">
                <div class="code-card">
                  <div class="code-label">我的确认码（交给对方）</div>
                  <div class="code-value">{{ myConfirmCode }}</div>
                  <el-tag :type="myConfirmed ? 'success' : 'info'" size="small">
                    {{ myConfirmed ? '已确认' : '等待对方输入' }}
                  </el-tag>
                </div>
                <div class="code-card">
                  <div class="code-label">对方的确认码</div>
                  <div class="code-input-area">
                    <el-input
                      v-model="confirmCodeInput"
                      placeholder="输入对方的6位确认码"
                      maxlength="6"
                      :disabled="otherConfirmed"
                    />
                    <el-button
                      type="success"
                      :disabled="!confirmCodeInput || confirmCodeInput.length < 6 || otherConfirmed"
                      :loading="verifyLoading"
                      @click="handleVerifyComplete"
                    >
                      {{ otherConfirmed ? '已确认' : '确认完成' }}
                    </el-button>
                  </div>
                  <el-tag :type="otherConfirmed ? 'success' : 'info'" size="small">
                    {{ otherConfirmed ? '已确认' : '等待输入' }}
                  </el-tag>
                </div>
              </div>
            </div>
          </div>

          <!-- 退款状态（仅服务中状态才显示退款提示） -->
          <div v-if="order.status === 3 && order.refundRequester && !order.refundAgreed" class="refund-section">
            <el-divider />
            <el-alert
              :title="refundStatusText"
              type="warning"
              :closable="false"
              show-icon
            />
          </div>

          <!-- 操作按钮 -->
          <div class="actions-section">
            <template v-if="order.status === 1">
              <el-button size="large" @click="handleCancel">取消订单</el-button>
            </template>

            <template v-if="order.status === 3">
              <el-button type="warning" size="large" @click="handleRefund">
                {{ order.refundRequester ? (order.refundRequester === myRole ? '等待对方同意' : '同意退款') : '申请退款' }}
              </el-button>
              <el-button type="danger" size="large" @click="handleDispute">发起争议</el-button>
            </template>

            <template v-if="order.status === 5">
              <el-button v-if="!reviewed" type="success" size="large" @click="openReviewDialog">
                评价{{ otherPartyLabel }}
              </el-button>
              <el-button v-else size="large" disabled>已评价</el-button>
              <el-button type="danger" size="large" @click="handleDispute">发起争议</el-button>
            </template>

            <template v-if="order.status === 7">
              <el-button type="primary" size="large" @click="$router.push({ path: '/credit/appeal', query: { orderId: order.id } })">
                发起申诉
              </el-button>
            </template>
          </div>
        </div>

        <el-dialog v-model="reviewDialogVisible" :title="'评价' + otherPartyLabel" width="420px">
          <el-form :model="reviewForm" label-position="top">
            <el-form-item label="评分" required>
              <el-rate
                v-model="reviewForm.rating"
                :max="5"
                :low-threshold="2"
                :high-threshold="4"
                show-text
                :texts="['极差', '较差', '一般', '满意', '非常满意']"
              />
            </el-form-item>
            <el-form-item label="评价内容">
              <el-input
                v-model="reviewForm.content"
                type="textarea"
                :rows="3"
                placeholder="分享你的体验..."
                maxlength="300"
                show-word-limit
              />
            </el-form-item>
            <el-form-item>
              <el-checkbox v-model="reviewForm.isAnonymous">匿名评价</el-checkbox>
            </el-form-item>
          </el-form>
          <template #footer>
            <el-button @click="reviewDialogVisible = false">取消</el-button>
            <el-button type="primary" :loading="reviewSubmitting" @click="submitReviewForm">
              {{ reviewSubmitting ? '提交中...' : '提交评价' }}
            </el-button>
          </template>
        </el-dialog>
      </template>

      <EmptyState v-else icon="🔍" title="订单不存在" />
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getOrderDetail, verifyOrder, cancelOrder, disputeOrder, refundOrder, getOrderReview } from '@/api/order'
import { submitReview } from '@/api/credit'
import { formatDateTime, getOrderStatusText } from '@/utils/format'
import { useUserStore } from '@/stores/user'
import EmptyState from '@/components/common/EmptyState.vue'
import LoadingSpinner from '@/components/common/LoadingSpinner.vue'
import type { OrderDetailVo, OrderStatus } from '@/types/api'

const props = defineProps<{ id: string }>()
const router = useRouter()

const loading = ref(true)
const order = ref<OrderDetailVo | null>(null)
const verifyCodeInput = ref('')
const confirmCodeInput = ref('')
const verifyLoading = ref(false)
const myRole = ref<'buyer' | 'seller'>('buyer')
const reviewed = ref(false)
const reviewDialogVisible = ref(false)
const reviewSubmitting = ref(false)
const reviewForm = reactive({
  rating: 0,
  content: '',
  isAnonymous: false,
})
const otherPartyLabel = computed(() => myRole.value === 'buyer' ? '卖家' : '买家')

const myStartCode = computed(() => {
  if (!order.value) return ''
  return myRole.value === 'buyer' ? order.value.buyerVerifyCode : order.value.sellerVerifyCode
})

const myStartVerified = computed(() => {
  if (!order.value) return false
  return myRole.value === 'buyer' ? order.value.buyerVerified : order.value.sellerVerified
})

const otherStartVerified = computed(() => {
  if (!order.value) return false
  return myRole.value === 'buyer' ? order.value.sellerVerified : order.value.buyerVerified
})

const myConfirmCode = computed(() => {
  if (!order.value) return ''
  return myRole.value === 'buyer' ? order.value.buyerConfirmCode : order.value.sellerConfirmCode
})

const myConfirmed = computed(() => {
  if (!order.value) return false
  return myRole.value === 'buyer' ? order.value.buyerConfirmed : order.value.sellerConfirmed
})

const otherConfirmed = computed(() => {
  if (!order.value) return false
  return myRole.value === 'buyer' ? order.value.sellerConfirmed : order.value.buyerConfirmed
})

const refundStatusText = computed(() => {
  if (!order.value?.refundRequester) return ''
  const requester = order.value.refundRequester === 'buyer' ? '买家' : '卖家'
  return `${requester}发起退款申请，等待对方同意`
})

const activeStep = computed(() => {
  if (!order.value) return 0
  const status = order.value.status
  if (status >= 5) return 3
  if (status >= 3) return 2
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
  if (status === 6) return step <= 1 ? 'error' : 'wait'
  if (status === 7) return step >= 2 ? 'error' : 'finish'
  return ''
}

async function fetchDetail() {
  loading.value = true
  try {
    order.value = await getOrderDetail(Number(props.id))
    const userStore = useUserStore()
    if (userStore.user?.id === order.value.buyerId) {
      myRole.value = 'buyer'
    } else {
      myRole.value = 'seller'
    }
    if (order.value.status === 5) {
      loadReviewStatus()
    }
  } catch {
    order.value = null
  } finally {
    loading.value = false
  }
}

async function handleVerifyStart() {
  if (!order.value || verifyCodeInput.value.length < 6) return
  verifyLoading.value = true
  try {
    await verifyOrder(order.value.id, verifyCodeInput.value, 'start')
    ElMessage.success('核销成功！')
    verifyCodeInput.value = ''
    fetchDetail()
  } catch (err: any) {
    // error already shown by interceptor
  } finally {
    verifyLoading.value = false
  }
}

async function handleVerifyComplete() {
  if (!order.value || confirmCodeInput.value.length < 6) return
  verifyLoading.value = true
  try {
    await verifyOrder(order.value.id, confirmCodeInput.value, 'complete')
    ElMessage.success('确认完成！')
    confirmCodeInput.value = ''
    fetchDetail()
  } catch (err: any) {
    // error already shown by interceptor
  } finally {
    verifyLoading.value = false
  }
}

async function handleCancel() {
  if (!order.value) return
  try {
    const { value: reason } = await ElMessageBox.prompt('请输入取消原因', '取消订单', {
      confirmButtonText: '确认取消',
      cancelButtonText: '再看看',
      inputPlaceholder: '例如：时间不合适，暂时不需要了',
      inputPattern: /\S+/,
      inputErrorMessage: '请输入取消原因',
    })
    await cancelOrder(order.value.id, reason.trim())
    ElMessage.success('订单已取消')
    fetchDetail()
  } catch {
    // cancelled
  }
}

async function handleRefund() {
  if (!order.value) return
  const isRequester = order.value.refundRequester === myRole.value
  if (isRequester) {
    ElMessage.warning('您已发起退款申请，请等待对方同意')
    return
  }
  try {
    await ElMessageBox.confirm('确认同意退款？积分将退还给买家', '退款确认', {
      confirmButtonText: '同意退款',
      type: 'warning',
    })
    await refundOrder(order.value.id, '同意退款')
    ElMessage.success('退款已处理')
    fetchDetail()
  } catch {
    // cancelled
  }
}

async function handleDispute() {
  if (!order.value) return
  try {
    const { value: reason } = await ElMessageBox.prompt('请输入争议原因', '发起争议', {
      confirmButtonText: '提交争议',
      cancelButtonText: '取消',
      inputPlaceholder: '描述争议原因...',
      inputPattern: /\S+/,
      inputErrorMessage: '请输入争议原因',
    })
    let explain: string | undefined
    try {
      const { value: explainVal } = await ElMessageBox.prompt('补充说明（可选）', '争议补充说明', {
        confirmButtonText: '提交',
        cancelButtonText: '跳过',
        inputPlaceholder: '补充详细说明或证据描述...',
      })
      explain = explainVal?.trim()
    } catch {
      // user skipped
    }
    await disputeOrder(order.value.id, reason.trim(), explain)
    ElMessage.success('争议已提交，等待管理员仲裁')
    fetchDetail()
  } catch {
    // cancelled
  }
}

async function loadReviewStatus() {
  if (!order.value) return
  try {
    await getOrderReview(order.value.id)
    reviewed.value = true
  } catch {
    reviewed.value = false
  }
}

function openReviewDialog() {
  reviewForm.rating = 0
  reviewForm.content = ''
  reviewForm.isAnonymous = false
  reviewDialogVisible.value = true
}

async function submitReviewForm() {
  if (!order.value || reviewForm.rating === 0) {
    ElMessage.warning('请选择评分')
    return
  }
  reviewSubmitting.value = true
  try {
    await submitReview({
      orderId: order.value.id,
      rating: reviewForm.rating,
      content: reviewForm.content || undefined,
      isAnonymous: reviewForm.isAnonymous,
    })
    ElMessage.success('评价已提交')
    reviewDialogVisible.value = false
    reviewed.value = true
  } catch {
    // error already shown by interceptor
  } finally {
    reviewSubmitting.value = false
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

    .dispute-reason-box {
      margin-top: 8px;
      padding: 12px 16px;
      background: #fef0f0;
      border-radius: 4px;
      font-size: 13px;
      color: #c45656;

      p {
        margin: 4px 0;
      }
    }
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

.verify-phase {
  h4 {
    font-size: 16px;
    margin-bottom: 8px;
  }

  .verify-hint {
    color: $color-text-secondary;
    font-size: $font-size-sm;
    margin-bottom: 16px;
  }
}

.verify-codes {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;

  .code-card {
    background: $color-bg;
    border-radius: $radius-md;
    padding: 16px;
    text-align: center;

    .code-label {
      font-size: $font-size-xs;
      color: $color-text-secondary;
      margin-bottom: 8px;
    }

    .code-value {
      font-size: 28px;
      font-weight: 700;
      letter-spacing: 4px;
      color: $color-primary;
      margin-bottom: 8px;
      font-family: 'Courier New', monospace;
    }

    .code-input-area {
      display: flex;
      gap: 8px;
      margin-bottom: 8px;
    }
  }
}

.refund-section {
  margin-top: 8px;
}
</style>
