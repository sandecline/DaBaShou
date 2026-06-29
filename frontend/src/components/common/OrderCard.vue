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

    <h3 class="order-title text-ellipsis">{{ order.title }}</h3>

    <div class="order-users">
      <div class="user-side">
        <el-avatar :size="24" :src="order.buyerAvatar">
          {{ (order.buyerName || '买').charAt(0) }}
        </el-avatar>
        <span>{{ order.buyerName || '买家' }}</span>
      </div>
      <el-icon><Right /></el-icon>
      <div class="user-side">
        <el-avatar :size="24" :src="order.sellerAvatar">
          {{ (order.sellerName || '卖').charAt(0) }}
        </el-avatar>
        <span>{{ order.sellerName || '卖家' }}</span>
      </div>
    </div>

    <div class="order-footer">
      <span class="order-amount">{{ order.pointAmount }} 积分</span>
      <span class="order-time">{{ formatDateTime(order.createTime) }}</span>
    </div>
  </div>
</template>

<script setup lang="ts">
import { useRouter } from 'vue-router'
import type { Order } from '@/types'
import { getOrderStatusText, getOrderStatusColor, formatDateTime } from '@/utils/format'

const props = defineProps<{
  order: Order
}>()

const router = useRouter()

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
