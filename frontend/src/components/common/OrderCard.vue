<template>
  <div class="order-card" @click="goDetail">
    <div class="order-header">
      <div class="order-no">
        <span class="label">订单号：</span>
        <span>{{ order.orderNo }}</span>
      </div>
      <el-tag
        :color="getOrderStatusColor(order.status)"
        size="small"
        effect="dark"
        style="border: none; color: #fff;"
      >
        {{ getOrderStatusText(order.status) }}
      </el-tag>
    </div>

    <h3 class="order-title text-ellipsis">{{ orderTitle }}</h3>

    <div class="order-content">
      <el-tag v-if="orderTagName" size="small" type="info">{{ orderTagName }}</el-tag>
      <span v-if="order.remark" class="order-remark text-ellipsis">{{ order.remark }}</span>
    </div>

    <div class="order-users">
      <div class="user-side">
        <el-avatar :size="24" :src="order.buyerAvatar">
          {{ (buyerName || '买').charAt(0) }}
        </el-avatar>
        <span>{{ buyerName || '买家' }}</span>
      </div>
      <el-icon><Right /></el-icon>
      <div class="user-side">
        <el-avatar :size="24" :src="order.sellerAvatar">
          {{ (sellerName || '卖').charAt(0) }}
        </el-avatar>
        <span>{{ sellerName || '卖家' }}</span>
      </div>
    </div>

    <div class="order-footer">
      <span class="order-amount">{{ order.pointAmount }} 积分</span>
      <span class="order-time">{{ formatDateTime(order.createTime) }}</span>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import type { Order } from '@/types'
import { getOrderStatusText, getOrderStatusColor, formatDateTime } from '@/utils/format'

const props = defineProps<{
  order: Order
}>()

const router = useRouter()
const orderTitle = computed(() => props.order.title || props.order.shelfTitle || '未命名交易')
const orderTagName = computed(() => props.order.skillTagName || props.order.tagName)
const buyerName = computed(() => props.order.buyerName || props.order.buyerNickname)
const sellerName = computed(() => props.order.sellerName || props.order.sellerNickname)

function goDetail() {
  router.push(`/order/${props.order.id}`)
}
</script>

<style scoped lang="scss">
.order-card {
  background: #ffffff;
  border-radius: $radius-md;
  border: 1px solid $color-border;
  padding: $spacing-md;
  cursor: pointer;
  display: flex;
  flex-direction: column;
  gap: 10px;
  transition: box-shadow 0.2s;

  &:hover {
    box-shadow: $shadow-md;
  }
}

.order-header {
  display: flex;
  align-items: center;
  justify-content: space-between;

  .order-no {
    font-size: $font-size-xs;
    color: $color-text-secondary;

    .label {
      color: $color-text-placeholder;
    }
  }
}

.order-title {
  font-size: $font-size-md;
  font-weight: 600;
  color: $color-text-primary;
  margin: 0;
}

.order-content {
  display: flex;
  align-items: center;
  gap: 8px;
  min-height: 24px;

  .order-remark {
    min-width: 0;
    color: $color-text-secondary;
    font-size: $font-size-xs;
  }
}

.order-users {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12px;

  .user-side {
    display: flex;
    align-items: center;
    gap: 6px;
    font-size: $font-size-sm;
    color: $color-text-regular;
  }
}

.order-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding-top: 8px;
  border-top: 1px solid $color-border-light;

  .order-amount {
    font-weight: 600;
    color: $color-primary;
  }

  .order-time {
    font-size: $font-size-xs;
    color: $color-text-placeholder;
  }
}
</style>
