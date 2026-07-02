<template>
  <header class="app-header">
    <div class="header-inner">
      <div class="header-left">
        <router-link to="/" class="brand">
          <span class="brand-mark">搭</span>
          <span class="brand-name">搭把手</span>
        </router-link>

        <button class="location-wrap" type="button">
          <el-icon><Location /></el-icon>
          <span>{{ currentCampus }}</span>
          <el-icon class="chevron"><ArrowDown /></el-icon>
        </button>
      </div>

      <div class="header-center">
        <div class="search-box" @click="focusSearch">
          <el-icon><Search /></el-icon>
          <input
            ref="searchInputRef"
            v-model="keyword"
            type="text"
            class="search-input"
            placeholder="搜索技能、求助或同学"
            @keyup.enter="handleSearch"
          />
          <button v-if="keyword" class="search-btn" type="button" @click.stop="handleSearch">
            搜索
          </button>
        </div>
      </div>

      <div class="header-right">
        <template v-if="!userStore.isLoggedIn">
          <router-link to="/login" class="login-link">登录</router-link>
        </template>

        <template v-else>
          <el-dropdown trigger="click" class="desktop-only">
            <button class="publish-link" type="button" aria-label="发布">
              <el-icon><Plus /></el-icon>
            </button>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item @click="$router.push('/skill/publish')">发布技能</el-dropdown-item>
                <el-dropdown-item @click="$router.push('/demand/publish')">发布求助</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>

          <el-dropdown trigger="click">
            <el-avatar :size="36" :src="userStore.user?.avatar" class="header-avatar">
              {{ userStore.user?.nickname?.charAt(0) || userStore.user?.username?.charAt(0) || 'U' }}
            </el-avatar>
            <template #dropdown>
              <el-dropdown-menu>
                <div class="dropdown-user-info">
                  <span class="user-name">{{ userStore.user?.nickname || userStore.user?.username }}</span>
                  <span class="user-trust">信任分 {{ userStore.user?.trustScore ?? 0 }}</span>
                </div>
                <el-dropdown-item divided @click="$router.push('/user/shop')">
                  <el-icon><Shop /></el-icon> 我的店铺
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
                  <span class="logout-text">退出登录</span>
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </template>
      </div>
    </div>

    <nav v-if="!$route.meta.noLayout && !isMobile" class="header-categories" aria-label="主导航">
      <div class="categories-inner">
        <router-link to="/skill" class="cat-item" :class="{ active: $route.path.startsWith('/skill') }">
          <el-icon><Briefcase /></el-icon>
          <span>技能广场</span>
        </router-link>
        <router-link to="/demand" class="cat-item" :class="{ active: $route.path.startsWith('/demand') }">
          <el-icon><Collection /></el-icon>
          <span>求助看板</span>
        </router-link>
        <router-link to="/order" class="cat-item" :class="{ active: $route.path.startsWith('/order') }">
          <el-icon><Tickets /></el-icon>
          <span>我的订单</span>
        </router-link>
        <router-link to="/credit" class="cat-item" :class="{ active: $route.path.startsWith('/credit') }">
          <el-icon><Star /></el-icon>
          <span>评价中心</span>
        </router-link>
        <router-link to="/stat" class="cat-item" :class="{ active: $route.path.startsWith('/stat') }">
          <el-icon><DataAnalysis /></el-icon>
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
  inset: 0 0 auto;
  z-index: 110;
  border-bottom: 1px solid rgba(226, 232, 240, 0.86);
  background: rgba(255, 255, 255, 0.9);
  backdrop-filter: blur(18px);
}

.header-inner {
  display: flex;
  align-items: center;
  gap: $spacing-md;
  width: min(100% - 32px, $max-width);
  height: $header-height;
  margin: 0 auto;
}

.header-left,
.header-right {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-shrink: 0;
}

.brand {
  display: inline-flex;
  align-items: center;
  gap: 9px;
  color: $color-text-primary;
  font-weight: 800;
}

.brand-mark {
  display: grid;
  place-items: center;
  width: 34px;
  height: 34px;
  border-radius: 10px;
  color: #ffffff;
  background: linear-gradient(135deg, $color-primary, $color-primary-dark);
  box-shadow: 0 10px 22px rgba(35, 100, 232, 0.22);
}

.brand-name {
  font-size: 18px;
  letter-spacing: 0;
}

.location-wrap,
.publish-link {
  appearance: none;
  border: 1px solid $color-border;
  background: #ffffff;
  color: $color-text-regular;
  cursor: pointer;
}

.location-wrap {
  display: inline-flex;
  align-items: center;
  gap: 5px;
  height: 34px;
  padding: 0 10px;
  border-radius: $radius-round;
  font-size: 13px;
  font-weight: 650;

  .chevron {
    font-size: 12px;
    color: $color-text-placeholder;
  }
}

.header-center {
  flex: 1;
  min-width: 120px;
  display: flex;
  justify-content: center;
}

.search-box {
  display: flex;
  align-items: center;
  gap: 8px;
  width: min(100%, 480px);
  height: 40px;
  padding: 0 12px;
  border: 1px solid $color-border;
  border-radius: $radius-round;
  background: $color-surface-muted;
  color: $color-text-secondary;
  cursor: text;
  transition: border-color 0.16s ease, background 0.16s ease, box-shadow 0.16s ease;

  &:focus-within {
    border-color: $color-primary;
    background: #ffffff;
    box-shadow: 0 0 0 4px rgba(35, 100, 232, 0.1);
  }
}

.search-input {
  flex: 1;
  min-width: 0;
  border: 0;
  outline: 0;
  background: transparent;
  color: $color-text-primary;
  font-size: 14px;

  &::placeholder {
    color: $color-text-placeholder;
  }
}

.search-btn {
  border: 0;
  border-radius: $radius-round;
  background: $color-primary;
  color: #ffffff;
  padding: 4px 10px;
  font-size: 12px;
  font-weight: 700;
  cursor: pointer;
}

.login-link {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  height: 36px;
  padding: 0 16px;
  border-radius: $radius-round;
  background: $color-primary;
  color: #ffffff;
  font-weight: 700;
}

.publish-link {
  display: grid;
  place-items: center;
  width: 36px;
  height: 36px;
  border-radius: 10px;
}

.header-avatar {
  cursor: pointer;
  border: 2px solid #ffffff;
  box-shadow: 0 0 0 1px $color-border;
}

.dropdown-user-info {
  display: flex;
  flex-direction: column;
  gap: 2px;
  padding: 10px 14px 8px;
  min-width: 160px;

  .user-name {
    font-weight: 750;
    color: $color-text-primary;
  }

  .user-trust {
    color: $color-text-secondary;
    font-size: 12px;
  }
}

.logout-text {
  color: $color-danger;
}

.header-categories {
  background: rgba(255, 255, 255, 0.78);
}

.categories-inner {
  display: flex;
  align-items: center;
  gap: 6px;
  width: min(100% - 32px, $max-width);
  height: $header-categories-height;
  margin: 0 auto;
  overflow-x: auto;
  scrollbar-width: none;

  &::-webkit-scrollbar {
    display: none;
  }
}

.cat-item {
  display: inline-flex;
  align-items: center;
  gap: 7px;
  height: 32px;
  padding: 0 13px;
  border-radius: $radius-round;
  color: $color-text-secondary;
  font-size: 13px;
  font-weight: 700;
  white-space: nowrap;

  &:hover,
  &.active {
    background: $color-primary-light;
    color: $color-primary;
  }
}

.desktop-only {
  display: flex;
}

@media (max-width: 768px) {
  .header-inner {
    width: min(100% - 24px, $max-width);
    gap: 10px;
  }

  .brand-name,
  .location-wrap {
    display: none;
  }

  .desktop-only,
  .header-categories {
    display: none;
  }
}
</style>
