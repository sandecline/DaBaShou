<template>
  <div class="admin-shell">
    <!-- 移动端遮罩 -->
    <div v-if="mobileOpen" class="admin-overlay" @click="mobileOpen = false" />

    <!-- 侧边栏 -->
    <aside class="admin-sidebar" :class="{ open: mobileOpen }">
      <div class="sidebar-brand">
        <div class="brand-icon">搭</div>
        <div class="brand-text">
          <strong>搭把手</strong>
          <small>管理后台</small>
        </div>
      </div>

      <nav class="sidebar-nav">
        <router-link
          v-for="item in menuItems"
          :key="item.path"
          :to="item.path"
          class="nav-item"
          :class="{ active: activePath === item.path }"
          @click="mobileOpen = false"
        >
          <el-icon class="nav-icon"><component :is="item.icon" /></el-icon>
          <span>{{ item.title }}</span>
        </router-link>
      </nav>

      <div class="sidebar-footer">
        <router-link to="/" class="back-link">
          <el-icon><Back /></el-icon>
          <span>返回前台</span>
        </router-link>
      </div>
    </aside>

    <!-- 主内容区 -->
    <div class="admin-main">
      <header class="admin-header">
        <div class="header-left">
          <button class="mobile-toggle" @click="mobileOpen = !mobileOpen">
            <el-icon><Fold /></el-icon>
          </button>
          <div class="header-title">
            <h1>{{ title }}</h1>
            <p v-if="subtitle" class="header-subtitle">{{ subtitle }}</p>
          </div>
        </div>
        <div class="header-right">
          <slot name="actions" />
          <el-dropdown trigger="click" class="admin-user-dropdown">
            <div class="admin-user">
              <el-avatar :size="32" :src="userStore.user?.avatar">
                {{ (userStore.user?.nickname || '管').charAt(0) }}
              </el-avatar>
              <span class="admin-username">{{ userStore.user?.nickname || '管理员' }}</span>
              <el-icon><ArrowDown /></el-icon>
            </div>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item @click="$router.push('/')">
                  <el-icon><HomeFilled /></el-icon> 返回前台
                </el-dropdown-item>
                <el-dropdown-item divided @click="handleLogout">
                  <span style="color: #ef4444">退出登录</span>
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </header>

      <section class="admin-content">
        <slot />
      </section>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'

defineProps<{
  title: string
  subtitle?: string
}>()

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const mobileOpen = ref(false)

const menuItems = [
  { path: '/admin', title: '运营总览', icon: 'DataBoard' },
  { path: '/admin/users', title: '用户管理', icon: 'User' },
  { path: '/admin/orders', title: '订单仲裁', icon: 'Tickets' },
  { path: '/admin/credit', title: '信用审核', icon: 'Warning' },
  { path: '/admin/campus-auths', title: '校园认证', icon: 'Checked' },
  { path: '/admin/system', title: '系统配置', icon: 'Setting' },
  { path: '/admin/stat', title: '数据统计', icon: 'DataAnalysis' },
]

const activePath = computed(() => {
  const matched = menuItems
    .filter((item) => route.path === item.path || (item.path !== '/admin' && route.path.startsWith(item.path)))
    .sort((a, b) => b.path.length - a.path.length)[0]
  return matched?.path || '/admin'
})

function handleLogout() {
  userStore.logout()
  router.push('/login')
}
</script>

<style scoped lang="scss">
.admin-shell {
  display: flex;
  min-height: 100vh;
  background: #f1f5f9;
}

/* ===== 侧边栏 ===== */
.admin-sidebar {
  position: fixed;
  top: 0;
  left: 0;
  bottom: 0;
  width: 240px;
  background: #1e293b;
  color: #e2e8f0;
  display: flex;
  flex-direction: column;
  z-index: 100;
  overflow-y: auto;
}

.sidebar-brand {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 20px 20px 24px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.08);
}

.brand-icon {
  width: 40px;
  height: 40px;
  border-radius: 10px;
  background: linear-gradient(135deg, #3b82f6, #1d4ed8);
  color: #fff;
  display: grid;
  place-items: center;
  font-weight: 800;
  font-size: 18px;
  flex-shrink: 0;
}

.brand-text {
  strong {
    display: block;
    font-size: 16px;
    color: #f1f5f9;
    line-height: 1.2;
  }
  small {
    display: block;
    font-size: 12px;
    color: #94a3b8;
    margin-top: 2px;
  }
}

.sidebar-nav {
  flex: 1;
  padding: 12px 10px;
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.nav-item {
  display: flex;
  align-items: center;
  gap: 10px;
  height: 42px;
  padding: 0 14px;
  border-radius: 8px;
  color: #94a3b8;
  font-size: 14px;
  font-weight: 500;
  transition: all 0.15s ease;
  text-decoration: none;

  .nav-icon {
    font-size: 18px;
    flex-shrink: 0;
  }

  &:hover {
    background: rgba(255, 255, 255, 0.06);
    color: #e2e8f0;
  }

  &.active {
    background: #3b82f6;
    color: #ffffff;
    font-weight: 600;

    .nav-badge {
      background: rgba(255, 255, 255, 0.25);
      color: #fff;
    }
  }
}

.sidebar-footer {
  padding: 12px 10px 16px;
  border-top: 1px solid rgba(255, 255, 255, 0.08);
}

.back-link {
  display: flex;
  align-items: center;
  gap: 8px;
  height: 38px;
  padding: 0 14px;
  border-radius: 8px;
  color: #64748b;
  font-size: 13px;
  text-decoration: none;
  transition: all 0.15s ease;

  &:hover {
    background: rgba(255, 255, 255, 0.06);
    color: #94a3b8;
  }
}

/* ===== 主内容区 ===== */
.admin-main {
  flex: 1;
  margin-left: 240px;
  min-width: 0;
  display: flex;
  flex-direction: column;
}

.admin-header {
  position: sticky;
  top: 0;
  z-index: 50;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  height: 64px;
  padding: 0 28px;
  background: #ffffff;
  border-bottom: 1px solid #e2e8f0;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.04);
}

.header-left {
  display: flex;
  align-items: center;
  gap: 14px;
  min-width: 0;
}

.mobile-toggle {
  display: none;
  appearance: none;
  border: 0;
  background: none;
  padding: 6px;
  cursor: pointer;
  color: #64748b;
  border-radius: 6px;

  &:hover {
    background: #f1f5f9;
  }
}

.header-title {
  min-width: 0;

  h1 {
    margin: 0;
    font-size: 18px;
    font-weight: 700;
    color: #0f172a;
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;
  }
}

.header-subtitle {
  margin: 2px 0 0;
  font-size: 12px;
  color: #94a3b8;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-shrink: 0;
}

.admin-user {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 4px 8px 4px 4px;
  border-radius: 8px;
  cursor: pointer;
  transition: background 0.15s ease;

  &:hover {
    background: #f1f5f9;
  }
}

.admin-username {
  font-size: 13px;
  font-weight: 600;
  color: #334155;
}

.admin-content {
  flex: 1;
  padding: 24px 28px 32px;
}

/* ===== 移动端遮罩 ===== */
.admin-overlay {
  display: none;
}

/* ===== 响应式 ===== */
@media (max-width: 900px) {
  .admin-sidebar {
    transform: translateX(-100%);
    transition: transform 0.25s ease;

    &.open {
      transform: translateX(0);
    }
  }

  .admin-overlay {
    display: block;
    position: fixed;
    inset: 0;
    background: rgba(0, 0, 0, 0.4);
    z-index: 90;
  }

  .admin-main {
    margin-left: 0;
  }

  .mobile-toggle {
    display: flex;
  }

  .admin-header {
    padding: 0 16px;
  }

  .admin-content {
    padding: 16px;
  }

  .admin-username {
    display: none;
  }
}
</style>
