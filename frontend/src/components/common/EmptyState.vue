<template>
  <div class="empty-state">
    <div class="empty-icon">{{ icon }}</div>
    <h3 class="empty-title">{{ title }}</h3>
    <p v-if="description" class="empty-desc">{{ description }}</p>
    <slot>
      <el-button v-if="actionText" type="primary" @click="$emit('action')">
        {{ actionText }}
      </el-button>
    </slot>
  </div>
</template>

<script setup lang="ts">
withDefaults(defineProps<{
  icon?: string
  title?: string
  description?: string
  actionText?: string
}>(), {
  icon: '📭',
  title: '暂无数据',
  description: '',
  actionText: '',
})

defineEmits<{
  action: []
}>()
</script>

<style scoped lang="scss">
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: $spacing-2xl $spacing-md;
  color: $color-text-secondary;

  .empty-icon {
    font-size: 48px;
    margin-bottom: $spacing-md;
  }

  .empty-title {
    font-size: $font-size-lg;
    font-weight: 600;
    color: $color-text-regular;
    margin: 0 0 8px;
  }

  .empty-desc {
    font-size: $font-size-sm;
    margin: 0 0 $spacing-md;
  }
}
</style>
