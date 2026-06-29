<template>
  <div class="app-layout">
    <!-- 顶部导航（桌面端 + 移动端） -->
    <AppHeader v-if="!$route.meta.noLayout" />

    <!-- 主内容区 -->
    <main
      class="app-main"
      :class="{
        'no-header': $route.meta.noLayout,
        'has-bottom-nav': !$route.meta.noLayout && !$route.meta.noBottomNav,
      }"
    >
      <router-view v-slot="{ Component }">
        <transition name="fade" mode="out-in">
          <component :is="Component" />
        </transition>
      </router-view>
    </main>

    <!-- 底部 Tab 导航（闲鱼风格，移动端 + 桌面端常显） -->
    <nav
      v-if="!$route.meta.noLayout && !$route.meta.noBottomNav"
      class="bottom-nav"
    >
      <router-link to="/" class="bottom-nav-item" exact-active-class="bottom-nav-active">
        <span class="nav-icon">
          <svg viewBox="0 0 24 24" width="24" height="24" fill="currentColor"><path d="M10 20v-6h4v6h5v-8h3L12 3 2 12h3v8z"/></svg>
        </span>
        <span class="nav-label">首页</span>
      </router-link>

      <router-link to="/skill" class="bottom-nav-item" active-class="bottom-nav-active">
        <span class="nav-icon">
          <svg viewBox="0 0 24 24" width="24" height="24" fill="currentColor"><path d="M19 3H5c-1.1 0-2 .9-2 2v14c0 1.1.9 2 2 2h14c1.1 0 2-.9 2-2V5c0-1.1-.9-2-2-2zm-5 14H7v-2h7v2zm3-4H7v-2h10v2zm0-4H7V7h10v2z"/></svg>
        </span>
        <span class="nav-label">技能</span>
      </router-link>

      <!-- 中间发布按钮（闲鱼"卖闲置"风格） -->
      <div class="bottom-nav-item bottom-nav-publish" @click="handlePublish">
        <span class="publish-btn">
          <svg viewBox="0 0 24 24" width="28" height="28" fill="currentColor"><path d="M19 13h-6v6h-2v-6H5v-2h6V5h2v6h6v2z"/></svg>
        </span>
        <span class="nav-label">发布</span>
      </div>

      <router-link to="/message" class="bottom-nav-item" active-class="bottom-nav-active">
        <span class="nav-icon">
          <el-badge :value="messageStore.unreadCount" :hidden="messageStore.unreadCount === 0" :max="99">
            <svg viewBox="0 0 24 24" width="24" height="24" fill="currentColor"><path d="M20 2H4c-1.1 0-2 .9-2 2v18l4-4h14c1.1 0 2-.9 2-2V4c0-1.1-.9-2-2-2z"/></svg>
          </el-badge>
        </span>
        <span class="nav-label">消息</span>
      </router-link>

      <router-link to="/user/shop" class="bottom-nav-item" active-class="bottom-nav-active">
        <span class="nav-icon">
          <svg viewBox="0 0 24 24" width="24" height="24" fill="currentColor"><path d="M12 12c2.21 0 4-1.79 4-4s-1.79-4-4-4-4 1.79-4 4 1.79 4 4 4zm0 2c-2.67 0-8 1.34-8 4v2h16v-2c0-2.66-5.33-4-8-4z"/></svg>
        </span>
        <span class="nav-label">我的</span>
      </router-link>
    </nav>
  </div>
</template>

<script setup lang="ts">
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { useMessageStore } from '@/stores/message'
import AppHeader from './Header.vue'

const router = useRouter()
const userStore = useUserStore()
const messageStore = useMessageStore()

function handlePublish() {
  if (!userStore.isLoggedIn) {
    router.push('/login')
    return
  }
  router.push('/skill/publish')
}
</script>

<style scoped lang="scss">
.app-layout {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  background: $color-bg;
}

.app-main {
  flex: 1;
  // 桌面端：主栏 + 分类导航栏
  margin-top: $header-offset;
  min-height: calc(100vh - #{$header-offset});
  background: $color-bg;

  &.no-header {
    margin-top: 0;
    min-height: 100vh;
  }

  &.has-bottom-nav {
    padding-bottom: calc(#{$bottom-nav-height} + 12px);
  }
}

// 移动端分类导航隐藏，只需偏移主栏高度
@media (max-width: 768px) {
  .app-main {
    margin-top: $header-height;
    min-height: calc(100vh - #{$header-height});
  }
}

// ========== 底部 Tab 导航 ==========
.bottom-nav {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  height: $bottom-nav-height;
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  border-top: 1px solid $color-border;
  display: flex;
  align-items: center;
  justify-content: space-around;
  z-index: 100;
  padding-bottom: env(safe-area-inset-bottom, 0);
}

.bottom-nav-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 2px;
  padding: 4px 12px;
  color: $color-text-secondary;
  text-decoration: none;
  cursor: pointer;
  transition: color 0.15s;
  position: relative;
  min-width: 56px;

  .nav-icon {
    display: flex;
    align-items: center;
    justify-content: center;
    width: 28px;
    height: 28px;
    position: relative;
  }

  .nav-label {
    font-size: 10px;
    font-weight: 500;
    line-height: 1;
  }

  &:hover {
    color: $color-primary-dark;
  }

  &.bottom-nav-active {
    color: $color-primary-dark;

    .nav-label {
      font-weight: 700;
    }
  }
}

// 中间发布按钮 — 闲鱼"卖闲置"风格
.bottom-nav-publish {
  .publish-btn {
    width: 44px;
    height: 44px;
    border-radius: 50%;
    background: linear-gradient(135deg, #FFC300 0%, #FF8F00 100%);
    color: #FFFFFF;
    display: flex;
    align-items: center;
    justify-content: center;
    margin-top: -18px;
    box-shadow: 0 4px 12px rgba(255, 140, 0, 0.35);
    transition: transform 0.15s, box-shadow 0.15s;

    &:active {
      transform: scale(0.93);
      box-shadow: 0 2px 8px rgba(255, 140, 0, 0.25);
    }
  }

  .nav-label {
    margin-top: 2px;
  }

  // 发布按钮不被 active-class 覆盖颜色
  &,
  &:hover {
    color: $color-text-secondary;
  }
}

// ========== 页面过渡动画 ==========
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.15s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}

// 桌面端优化
@media (min-width: 769px) {
  .bottom-nav {
    max-width: 480px;
    left: 50%;
    transform: translateX(-50%);
    border-radius: 16px 16px 0 0;
  }
}
</style>
