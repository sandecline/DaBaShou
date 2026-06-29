<template>
  <span class="trust-badge" :class="[levelClass, `trust-size-${size}`]">
    <el-icon :size="size === 'small' ? 14 : 16">
      <Medal />
    </el-icon>
    <span>{{ level.label }}</span>
    <span class="trust-score">{{ (score ?? 0).toFixed(1) }}</span>
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

const level = computed(() => getTrustLevel(props.score ?? 0))
const levelClass = computed(() => `trust-${level.value.level}`)
</script>

<style scoped lang="scss">
.trust-badge {
  display: inline-flex;
  align-items: center;
  gap: 3px;
  font-weight: 600;
  font-size: $font-size-sm;
  padding: 2px 10px;
  border-radius: 12px;
  background: rgba(255, 255, 255, 0.85);
  backdrop-filter: blur(4px);

  .trust-score {
    opacity: 0.7;
    font-weight: 400;
  }
}

.trust-size-small {
  font-size: $font-size-xs;
  padding: 1px 6px;
}

.trust-gold {
  color: #d97706;
}

.trust-reliable {
  color: $color-primary-dark;
}

.trust-newcomer {
  color: $color-success;
}
</style>
