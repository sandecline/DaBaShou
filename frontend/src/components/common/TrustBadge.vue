<template>
  <span class="trust-badge" :class="levelClass" :style="{ color: level.color }">
    <el-icon :size="size === 'small' ? 14 : 16">
      <Medal />
    </el-icon>
    <span>{{ level.label }}</span>
    <span class="trust-score">{{ score.toFixed(1) }}</span>
  </span>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { getTrustLevel } from '@/utils/format'

const props = withDefaults(defineProps<{
  score: number
  size?: 'small' | 'default'
}>(), {
  size: 'default',
})

const level = computed(() => getTrustLevel(props.score))
const levelClass = computed(() => `trust-${level.value.level}`)
</script>

<style scoped lang="scss">
.trust-badge {
  display: inline-flex;
  align-items: center;
  gap: 3px;
  font-weight: 600;
  font-size: $font-size-sm;

  .trust-score {
    opacity: 0.7;
    font-weight: 400;
  }
}

.trust-gold {
  color: #d97706;
}

.trust-reliable {
  color: $color-primary;
}

.trust-newcomer {
  color: $color-success;
}
</style>
