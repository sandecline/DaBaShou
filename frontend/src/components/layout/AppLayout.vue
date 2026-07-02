<template>
  <div class="app-layout">
    <AppHeader v-if="!$route.meta.noLayout" />

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

    <nav
      v-if="!$route.meta.noLayout && !$route.meta.noBottomNav"
      class="bottom-nav"
      aria-label="底部导航"
    >
      <router-link to="/" class="bottom-nav-item" exact-active-class="bottom-nav-active">
        <el-icon><HomeFilled /></el-icon>
        <span class="nav-label">首页</span>
      </router-link>

      <router-link to="/skill" class="bottom-nav-item" active-class="bottom-nav-active">
        <el-icon><Briefcase /></el-icon>
        <span class="nav-label">技能</span>
      </router-link>

      <button class="bottom-nav-item bottom-nav-publish" type="button" @click="handlePublish">
        <span class="publish-btn"><el-icon><Plus /></el-icon></span>
        <span class="nav-label">发布</span>
      </button>

      <router-link to="/message" class="bottom-nav-item" active-class="bottom-nav-active">
        <el-badge :value="messageStore.unreadCount" :hidden="messageStore.unreadCount === 0" :max="99">
          <el-icon><ChatDotRound /></el-icon>
        </el-badge>
        <span class="nav-label">消息</span>
      </router-link>

      <router-link to="/user/shop" class="bottom-nav-item" active-class="bottom-nav-active">
        <el-icon><User /></el-icon>
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
}

.app-main {
  flex: 1;
  margin-top: $header-offset;
  min-height: calc(100vh - #{$header-offset});

  &.no-header {
    margin-top: 0;
    min-height: 100vh;
  }

  &.has-bottom-nav {
    padding-bottom: calc(#{$bottom-nav-height} + 18px);
  }
}

.bottom-nav {
  position: fixed;
  bottom: 14px;
  left: 50%;
  z-index: 120;
  display: flex;
  align-items: center;
  justify-content: space-between;
  width: min(480px, calc(100% - 28px));
  height: $bottom-nav-height;
  padding: 8px;
  border: 1px solid rgba(203, 213, 225, 0.8);
  border-radius: 18px;
  background: rgba(255, 255, 255, 0.92);
  box-shadow: $shadow-lg;
  backdrop-filter: blur(18px);
  transform: translateX(-50%);
}

.bottom-nav-item {
  appearance: none;
  border: 0;
  background: transparent;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-direction: column;
  gap: 4px;
  width: 64px;
  height: 48px;
  border-radius: $radius-md;
  color: $color-text-secondary;
  cursor: pointer;
  transition: color 0.16s ease, background 0.16s ease;

  .el-icon {
    font-size: 21px;
  }

  .nav-label {
    font-size: 11px;
    font-weight: 650;
    line-height: 1;
  }

  &:hover,
  &.bottom-nav-active {
    background: $color-primary-light;
    color: $color-primary;
  }
}

.bottom-nav-publish {
  .publish-btn {
    display: grid;
    place-items: center;
    width: 40px;
    height: 40px;
    margin-top: -18px;
    border-radius: 14px;
    color: #ffffff;
    background: linear-gradient(135deg, $color-primary, $color-primary-dark);
    box-shadow: 0 12px 24px rgba(35, 100, 232, 0.28);
  }

  &:hover {
    background: transparent;
    color: $color-primary;
  }
}

.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.16s ease, transform 0.16s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
  transform: translateY(4px);
}

@media (max-width: 768px) {
  .app-main {
    margin-top: $header-height;
    min-height: calc(100vh - #{$header-height});
  }
}
</style>
