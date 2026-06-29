<template>
  <AppLayout />
</template>

<script setup lang="ts">
import AppLayout from '@/components/layout/AppLayout.vue'
import { useMessageStore } from '@/stores/message'
import { onMounted } from 'vue'

const messageStore = useMessageStore()

onMounted(() => {
  // 初始化未读消息数
  messageStore.fetchUnreadCount()
})
</script>

<style>
#app {
  min-height: 100vh;
}
</style>
