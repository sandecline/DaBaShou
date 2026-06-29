<template>
  <div class="user-avatar" :class="sizeClass">
    <el-avatar :size="avatarSize" :src="src">
      <span class="avatar-fallback">{{ fallback }}</span>
    </el-avatar>
    <div v-if="showTrustBadge" class="trust-dot" :class="trustLevelClass" />
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { getTrustLevel } from '@/utils/format'
import type { TrustLevel } from '@/types'

const props = withDefaults(defineProps<{
  src?: string
  name?: string
  trustScore?: number
  size?: 'small' | 'default' | 'large'
  showTrustBadge?: boolean
}>(), {
  src: '',
  name: '',
  trustScore: 0,
  size: 'default',
  showTrustBadge: false,
})

const sizeMap: Record<string, number> = { small: 32, default: 40, large: 64 }
const avatarSize = computed(() => sizeMap[props.size])
const sizeClass = computed(() => `size-${props.size}`)
const fallback = computed(() => (props.name || '?').charAt(0))

const trustLevelClass = computed(() => {
  if (!props.trustScore) return ''
  const { level } = getTrustLevel(props.trustScore)
  return `trust-${level}`
})
</script>

<style scoped lang="scss">
.user-avatar {
  position: relative;
  display: inline-flex;

  .trust-dot {
    position: absolute;
    bottom: 0;
    right: 0;
    width: 12px;
    height: 12px;
    border-radius: 50%;
    border: 2px solid #ffffff;

    &.trust-gold { background: #d97706; }
    &.trust-reliable { background: $color-primary; }
    &.trust-newcomer { background: $color-success; }
  }
}

.avatar-fallback {
  font-weight: 600;
}
</style>
