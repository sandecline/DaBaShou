<template>
  <div class="loading-spinner" :class="{ 'is-fullscreen': fullscreen }">
    <el-icon class="spinner-icon" :size="size">
      <Loading />
    </el-icon>
    <p v-if="text" class="spinner-text">{{ text }}</p>
  </div>
</template>

<script setup lang="ts">
withDefaults(defineProps<{
  text?: string
  size?: number
  fullscreen?: boolean
}>(), {
  text: '加载中...',
  size: 32,
  fullscreen: false,
})
</script>

<style scoped lang="scss">
.loading-spinner {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: $spacing-2xl;
  color: $color-text-secondary;

  &.is-fullscreen {
    position: fixed;
    inset: 0;
    background: rgba(255, 255, 255, 0.8);
    z-index: 999;
  }
}

.spinner-icon {
  animation: spin 1s linear infinite;
  color: $color-primary;
}

.spinner-text {
  margin-top: 12px;
  font-size: $font-size-sm;
}

@keyframes spin {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}
</style>
