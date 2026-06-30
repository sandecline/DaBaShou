<template>
  <AppLayout />
</template>

<script setup lang="ts">
import AppLayout from '@/components/layout/AppLayout.vue'
import { useMessageStore } from '@/stores/message'
import { isLoggedIn } from '@/utils/auth'
import { onMounted } from 'vue'

const messageStore = useMessageStore()

onMounted(() => {
  // 只有登录后才初始化未读消息，避免公开页面触发未授权提示。
  if (isLoggedIn()) {
    messageStore.fetchUnreadCount()
  }
})
</script>

<style>
#app {
  min-height: 100vh;
}
</style>
