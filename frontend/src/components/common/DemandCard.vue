<template>
  <article class="demand-card card-hover" :class="{ urgent: demand.isUrgent }" @click="goDetail">
    <div class="card-top">
      <div class="badges">
        <span v-if="demand.isUrgent" class="badge urgent-badge">急单</span>
        <span class="badge reward-badge">{{ demand.pointReward }} 积分</span>
      </div>
      <span v-if="demand.distance != null" class="distance">{{ formatDistance(demand.distance) }}</span>
    </div>

    <h3 class="card-title text-ellipsis-2">{{ demand.title }}</h3>
    <p class="card-desc text-ellipsis-2">{{ demand.description || '这位同学需要帮助，点击查看详情。' }}</p>

    <div class="meta-row">
      <span>{{ demand.tagName || demand.skillTagName || '求助' }}</span>
      <span v-if="demand.campus">{{ demand.campus }}</span>
      <span>{{ locationText }}</span>
    </div>

    <div class="footer-row">
      <div class="user-row">
        <el-avatar :size="24" :src="demand.userAvatar || demand.avatar">
          {{ displayName.charAt(0) }}
        </el-avatar>
        <span class="user-name text-ellipsis">{{ displayName }}</span>
      </div>

      <div v-if="demand.deadline" class="deadline">
        <el-icon><Clock /></el-icon>
        <span>{{ formatDateTime(demand.deadline, 'MM-DD HH:mm') }} 截止</span>
      </div>
    </div>
  </article>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import type { Demand } from '@/types'
import { formatDateTime, formatDistance } from '@/utils/format'

const props = defineProps<{
  demand: Demand
}>()

const router = useRouter()

const displayName = computed(() => props.demand.userName || props.demand.nickname || '匿名同学')

const locationText = computed(() => {
  if (props.demand.locationType === 2) return '线下'
  if (props.demand.locationType === 1) return '线上'
  return '不限'
})

function goDetail() {
  router.push(`/demand/${props.demand.id}`)
}
</script>

<style scoped lang="scss">
.demand-card {
  display: flex;
  flex-direction: column;
  gap: 12px;
  min-height: 220px;
  padding: 16px;
  border: 1px solid $color-border;
  border-radius: $radius-md;
  background: #ffffff;
  cursor: pointer;

  &.urgent {
    border-color: rgba(239, 68, 68, 0.28);
    background:
      linear-gradient(135deg, rgba(254, 226, 226, 0.7), rgba(255, 255, 255, 0) 55%),
      #ffffff;
  }
}

.card-top,
.footer-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.badges,
.meta-row {
  display: flex;
  align-items: center;
  gap: 6px;
  flex-wrap: wrap;
}

.badge,
.meta-row span {
  display: inline-flex;
  align-items: center;
  min-height: 24px;
  padding: 0 9px;
  border-radius: $radius-round;
  font-size: 12px;
  font-weight: 750;
}

.urgent-badge {
  background: $color-danger-light;
  color: #b91c1c;
}

.reward-badge {
  background: $color-accent-light;
  color: #c2410c;
}

.distance {
  flex-shrink: 0;
  color: $color-text-placeholder;
  font-size: 12px;
  font-weight: 650;
}

.card-title {
  margin: 0;
  color: $color-text-primary;
  font-size: 16px;
  font-weight: 780;
  line-height: 1.4;
}

.card-desc {
  min-height: 42px;
  margin: 0;
  color: $color-text-secondary;
  font-size: 13px;
  line-height: 1.55;
}

.meta-row {
  margin-top: auto;

  span {
    background: $color-surface-muted;
    color: $color-text-secondary;
  }
}

.footer-row {
  padding-top: 12px;
  border-top: 1px solid $color-border-light;
}

.user-row,
.deadline {
  display: flex;
  align-items: center;
  min-width: 0;
}

.user-row {
  gap: 7px;
}

.user-name {
  color: $color-text-secondary;
  font-size: 13px;
  font-weight: 650;
}

.deadline {
  gap: 5px;
  flex-shrink: 0;
  color: $color-warning;
  font-size: 12px;
  font-weight: 750;
}
</style>
