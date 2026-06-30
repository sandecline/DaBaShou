<template>
  <u-config-provider>
    <view class="app">
      <u-toast ref="uToast"></u-toast>
      <router-view />
    </view>
  </u-config-provider>
</template>

<script setup>
import { onLaunch, onShow, onHide } from '@dcloudio/uni-app'

onLaunch(() => {
  console.log('搭把手小程序启动')
})

onShow(() => {
  // 小程序从后台切回前台时触发
})

onHide(() => {
  // 小程序切入后台时触发
})
</script>

<style lang="scss">
/* 全局样式 */
page {
  background-color: $dabashou-bg;
  font-family: -apple-system, BlinkMacSystemFont, 'Helvetica Neue', sans-serif;
}
</style>
