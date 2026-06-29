<template>
  <div class="demand-card card-hover" @click="goDetail">
    <!-- 状态标签 -->
    <div class="card-badges">
      <span v-if="demand.isUrgent" class="badge badge-urgent">
        <span class="badge-dot"></span>急单
      </span>
      <span class="badge badge-reward">
        {{ demand.pointReward }} 积分
      </span>
    </div>

    <!-- 标题 -->
    <h3 class="card-title text-ellipsis-2">{{ demand.title }}</h3>

    <!-- 描述 -->
    <p class="card-desc text-ellipsis-2">{{ demand.description || '急需帮助，点击查看详情' }}</p>

    <!-- 标签 + 用户 -->
    <div class="card-footer">
      <div class="tags-row">
        <span class="footer-tag">{{ demand.tagName || '求助' }}</span>
        <span v-if="demand.campus" class="footer-tag campus-tag">{{ demand.campus }}</span>
      </div>

      <div class="user-row">
        <el-avatar :size="18" :src="demand.userAvatar" class="user-avatar">
          {{ (demand.userName || '?').charAt(0) }}
        </el-avatar>
        <span class="user-name text-ellipsis">{{ demand.userName || '匿名' }}</span>
      </div>
    </div>

    <!-- 截止时间 -->
    <div class="card-bottom" v-if="demand.deadline">
      <svg viewBox="0 0 24 24" width="12" height="12" fill="currentColor" class="clock-icon"><path d="M11.99 2C6.47 2 2 6.48 2 12s4.47 10 9.99 10C17.52 22 22 17.52 22 12S17.52 2 11.99 2zM12 20c-4.42 0-8-3.58-8-8s3.58-8 8-8 8 3.58 8 8-3.58 8-8 8zm.5-13H11v6l5.25 3.15.75-1.23-4.5-2.67z"/></svg>
      <span class="deadline-text">{{ formatDateTime(demand.deadline, 'MM-DD HH:mm') }} 截止</span>
      <span v-if="demand.distance != null" class="distance-text">{{ formatDistance(demand.distance) }}</span>
    </div>
  </div>
</template>

<script setup lang="ts">
import { useRouter } from 'vue-router'
import type { Demand } from '@/types'
import { formatDateTime, formatDistance } from '@/utils/format'

const props = defineProps<{
  demand: Demand
}>()

const router = useRouter()

function goDetail() {
  router.push(`/demand/${props.demand.id}`)
}
</script>

<style scoped lang="scss">
.demand-card {
  background: #FFFFFF;
  border-radius: $radius-md;
  padding: 12px;
  cursor: pointer;
  box-shadow: $shadow-sm;
  display: flex;
  flex-direction: column;
  gap: 8px;
  position: relative;
  border-left: 3px solid transparent;

  &:has(.badge-urgent) {
    border-left-color: $color-danger;
    background: linear-gradient(135deg, #FFFBFB 0%, #FFFFFF 100%);
  }
}

// ========== 顶部标签 ==========
.card-badges {
  display: flex;
  align-items: center;
  gap: 6px;
}

.badge {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  padding: 2px 8px;
  border-radius: 4px;
  font-size: 10px;
  font-weight: 600;

  &-urgent {
    background: #FFEBEE;
    color: #FF5252;

    .badge-dot {
      width: 5px;
      height: 5px;
      border-radius: 50%;
      background: #FF5252;
      animation: pulse 1.5s infinite;
    }
  }

  &-reward {
    background: #FFF3E0;
    color: #FF6B00;
  }
}

@keyframes pulse {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.4; }
}

// ========== 标题 ==========
.card-title {
  font-size: 14px;
  font-weight: 600;
  color: $color-text-primary;
  line-height: 1.4;
  margin: 0;
}

// ========== 描述 ==========
.card-desc {
  font-size: 12px;
  color: $color-text-secondary;
  line-height: 1.5;
  margin: 0;
}

// ========== 底部信息 ==========
.card-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.tags-row {
  display: flex;
  gap: 4px;

  .footer-tag {
    padding: 1px 6px;
    border-radius: 4px;
    font-size: 10px;
    background: $color-bg;
    color: $color-text-secondary;

    &.campus-tag {
      background: #E8F5E9;
      color: #388E3C;
    }
  }
}

.user-row {
  display: flex;
  align-items: center;
  gap: 4px;

  .user-avatar {
    flex-shrink: 0;
  }

  .user-name {
    font-size: 10px;
    color: $color-text-secondary;
    max-width: 50px;
  }
}

// ========== 截止时间 ==========
.card-bottom {
  display: flex;
  align-items: center;
  gap: 3px;
  padding-top: 6px;
  border-top: 1px solid $color-border-light;

  .clock-icon {
    color: $color-warning;
  }

  .deadline-text {
    font-size: 11px;
    color: $color-warning;
    font-weight: 500;
  }

  .distance-text {
    font-size: 11px;
    color: $color-text-placeholder;
    margin-left: auto;
  }
}
</style>
