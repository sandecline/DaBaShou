<template>
  <header class="app-header">
    <div class="header-inner">
      <!-- 左侧：城市/校区选择（闲鱼风格） -->
      <div class="header-left">
        <router-link to="/" class="logo-link">
          <span class="logo-icon">🤝</span>
        </router-link>
        <div class="location-wrap" @click="showCampusPicker = true">
          <svg viewBox="0 0 24 24" width="16" height="16" fill="currentColor" class="location-icon"><path d="M12 2C8.13 2 5 5.13 5 9c0 5.25 7 13 7 13s7-7.75 7-13c0-3.87-3.13-7-7-7zm0 9.5c-1.38 0-2.5-1.12-2.5-2.5S10.62 6.5 12 6.5s2.5 1.12 2.5 2.5S13.38 11.5 12 11.5z"/></svg>
          <span class="location-text">{{ currentCampus || '选择校区' }}</span>
          <svg viewBox="0 0 24 24" width="12" height="12" fill="currentColor" class="arrow-icon"><path d="M7 10l5 5 5-5z"/></svg>
        </div>
      </div>

      <!-- 中间：搜索框（闲鱼风格圆角搜索） -->
      <div class="header-center">
        <div class="search-box" @click="focusSearch">
          <svg viewBox="0 0 24 24" width="16" height="16" fill="#999" class="search-icon-svg"><path d="M15.5 14h-.79l-.28-.27C15.41 12.59 16 11.11 16 9.5 16 5.91 13.09 3 9.5 3S3 5.91 3 9.5 5.91 16 9.5 16c1.61 0 3.09-.59 4.23-1.57l.27.28v.79l5 4.99L20.49 19l-4.99-5zm-6 0C7.01 14 5 11.99 5 9.5S7.01 5 9.5 5 14 7.01 14 9.5 11.99 14 9.5 14z"/></svg>
          <input
            ref="searchInputRef"
            v-model="keyword"
            type="text"
            class="search-input"
            placeholder="搜索技能或求助..."
            @keyup.enter="handleSearch"
          />
          <span v-if="keyword" class="search-btn" @click.stop="handleSearch">搜索</span>
        </div>
      </div>

      <!-- 右侧操作区 -->
      <div class="header-right">
        <!-- 未登录 -->
        <template v-if="!userStore.isLoggedIn">
          <router-link to="/login" class="login-link">登录</router-link>
        </template>

        <!-- 已登录 -->
        <template v-else>
          <!-- 发布按钮（桌面端显示，移动端由底部导航替代） -->
          <el-dropdown trigger="click" class="desktop-only">
            <span class="publish-link">
              <svg viewBox="0 0 24 24" width="20" height="20" fill="currentColor"><path d="M19 13h-6v6h-2v-6H5v-2h6V5h2v6h6v2z"/></svg>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item @click="$router.push('/skill/publish')">发布技能</el-dropdown-item>
                <el-dropdown-item @click="$router.push('/demand/publish')">发布需求</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>

          <!-- 用户头像 + 下拉菜单 -->
          <el-dropdown trigger="click">
            <el-avatar :size="32" :src="userStore.user?.avatar" class="header-avatar">
              {{ userStore.user?.nickname?.charAt(0) || userStore.user?.username?.charAt(0) || 'U' }}
            </el-avatar>
            <template #dropdown>
              <el-dropdown-menu>
                <div class="dropdown-user-info">
                  <span class="user-name">{{ userStore.user?.nickname || userStore.user?.username }}</span>
                  <span class="user-trust">信任分 {{ userStore.user?.trustScore ?? 0 }}</span>
                </div>
                <el-dropdown-item divided @click="$router.push('/user/shop')">
                  <el-icon><Shop /></el-icon> 我的小铺
                </el-dropdown-item>
                <el-dropdown-item @click="$router.push('/user/profile')">
                  <el-icon><User /></el-icon> 个人资料
                </el-dropdown-item>
                <el-dropdown-item @click="$router.push('/user/points')">
                  <el-icon><Coin /></el-icon> 积分管理
                </el-dropdown-item>
                <el-dropdown-item @click="$router.push('/user/trust')">
                  <el-icon><Medal /></el-icon> 信任分
                </el-dropdown-item>
                <el-dropdown-item @click="$router.push('/stat')">
                  <el-icon><DataAnalysis /></el-icon> 数据统计
                </el-dropdown-item>
                <el-dropdown-item divided @click="handleLogout">
                  <span style="color: #FF5252;">退出登录</span>
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </template>
      </div>
    </div>

    <!-- 分类快捷导航（桌面端横向滚动） -->
    <nav class="header-categories" v-if="!$route.meta.noLayout && !isMobile">
      <div class="categories-inner">
        <router-link to="/skill" class="cat-item" :class="{ active: $route.path.startsWith('/skill') }">
          <span class="cat-icon">💡</span>
          <span>技能广场</span>
        </router-link>
        <router-link to="/demand" class="cat-item" :class="{ active: $route.path.startsWith('/demand') }">
          <span class="cat-icon">📋</span>
          <span>求助看板</span>
        </router-link>
        <router-link to="/order" class="cat-item" :class="{ active: $route.path.startsWith('/order') }">
          <span class="cat-icon">📦</span>
          <span>我的订单</span>
        </router-link>
        <router-link to="/credit" class="cat-item" :class="{ active: $route.path.startsWith('/credit') }">
          <span class="cat-icon">⭐</span>
          <span>评价中心</span>
        </router-link>
        <router-link to="/stat" class="cat-item" :class="{ active: $route.path.startsWith('/stat') }">
          <span class="cat-icon">📊</span>
          <span>数据统计</span>
        </router-link>
      </div>
    </nav>
  </header>
</template>

<script setup lang="ts">
import { ref, onMounted, onBeforeUnmount } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const userStore = useUserStore()

const keyword = ref('')
const currentCampus = ref('仙林校区')
const showCampusPicker = ref(false)
const searchInputRef = ref<HTMLInputElement | null>(null)
const isMobile = ref(false)

function handleSearch() {
  const q = keyword.value.trim()
  if (q) {
    router.push({ path: '/skill', query: { keyword: q } })
  }
}

function focusSearch() {
  searchInputRef.value?.focus()
}

function handleLogout() {
  userStore.logout()
  router.push('/')
}

function checkMobile() {
  isMobile.value = window.innerWidth < 769
}

onMounted(() => {
  checkMobile()
  window.addEventListener('resize', checkMobile)
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', checkMobile)
})
</script>

<style scoped lang="scss">
.app-header {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  background: rgba(255, 255, 255, 0.92);
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  border-bottom: 1px solid $color-border;
  z-index: 100;
}

// ========== 顶部栏 ==========
.header-inner {
  height: $header-height;
  display: flex;
  align-items: center;
  padding: 0 $spacing-md;
  gap: $spacing-sm;
}

// 左侧
.header-left {
  display: flex;
  align-items: center;
  gap: 6px;
  flex-shrink: 0;
}

.logo-link {
  display: flex;
  align-items: center;
  text-decoration: none;

  .logo-icon {
    font-size: 24px;
  }
}

.location-wrap {
  display: flex;
  align-items: center;
  gap: 2px;
  cursor: pointer;
  color: $color-text-primary;
  padding: 2px 4px;
  border-radius: 4px;

  .location-icon {
    color: $color-primary-dark;
  }

  .location-text {
    font-size: 13px;
    font-weight: 600;
    max-width: 80px;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }

  .arrow-icon {
    color: $color-text-secondary;
  }
}

// 中间搜索
.header-center {
  flex: 1;
  display: flex;
  justify-content: center;
  min-width: 0;
}

.search-box {
  display: flex;
  align-items: center;
  width: 100%;
  max-width: 400px;
  height: 36px;
  background: $color-bg;
  border-radius: 18px;
  padding: 0 12px;
  gap: 6px;
  border: 1px solid transparent;
  transition: border-color 0.15s, background 0.15s;
  cursor: text;

  &:focus-within {
    background: #FFFFFF;
    border-color: $color-primary;
    box-shadow: 0 0 0 3px rgba(255, 195, 0, 0.12);
  }

  .search-icon-svg {
    flex-shrink: 0;
  }

  .search-input {
    flex: 1;
    border: none;
    outline: none;
    background: transparent;
    font-size: 13px;
    color: $color-text-primary;
    min-width: 0;

    &::placeholder {
      color: $color-text-placeholder;
    }
  }

  .search-btn {
    flex-shrink: 0;
    font-size: 12px;
    font-weight: 600;
    color: $color-primary-dark;
    cursor: pointer;
    padding: 2px 8px;
    border-radius: 10px;
    background: rgba(255, 195, 0, 0.15);
  }
}

// 右侧
.header-right {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-shrink: 0;

  .login-link {
    font-size: 13px;
    font-weight: 600;
    color: $color-primary-dark;
    text-decoration: none;
    padding: 4px 12px;
    border-radius: 14px;
    background: rgba(255, 195, 0, 0.12);
  }

  .publish-link {
    display: flex;
    align-items: center;
    justify-content: center;
    width: 34px;
    height: 34px;
    border-radius: 50%;
    background: rgba(255, 195, 0, 0.12);
    color: $color-primary-dark;
    cursor: pointer;
    transition: background 0.15s;

    &:hover {
      background: rgba(255, 195, 0, 0.22);
    }
  }

  .avatar-link {
    display: flex;
    align-items: center;
  }

  .header-avatar {
    border: 2px solid $color-primary-light;
  }
}

// ========== 分类导航条 ==========
.header-categories {
  background: #FFFFFF;
  border-bottom: 1px solid $color-border-light;
}

.categories-inner {
  display: flex;
  align-items: center;
  gap: 4px;
  max-width: $max-width;
  margin: 0 auto;
  padding: 6px $spacing-md;
  overflow-x: auto;
  -webkit-overflow-scrolling: touch;
  scrollbar-width: none;

  &::-webkit-scrollbar {
    display: none;
  }
}

.cat-item {
  display: flex;
  align-items: center;
  gap: 4px;
  padding: 6px 14px;
  border-radius: 16px;
  font-size: 12px;
  font-weight: 500;
  color: $color-text-regular;
  text-decoration: none;
  white-space: nowrap;
  transition: all 0.15s;

  .cat-icon {
    font-size: 14px;
  }

  &:hover {
    background: $color-bg;
    color: $color-text-primary;
  }

  &.active {
    background: #FFF8E1;
    color: $color-primary-dark;
    font-weight: 600;
  }
}

.desktop-only {
  display: flex;
}

@media (max-width: 768px) {
  .header-inner {
    padding: 0 12px;
    gap: 8px;
  }

  .header-center {
    .search-box {
      max-width: none;
    }
  }

  .desktop-only {
    display: none;
  }

  .header-categories {
    display: none;
  }

  .location-text {
    max-width: 60px;
  }
}
</style>
