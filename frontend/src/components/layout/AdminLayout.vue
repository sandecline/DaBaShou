<template>
  <div class="admin-shell">
    <aside class="admin-rail">
      <router-link to="/admin" class="admin-brand">
        <span class="brand-block">管</span>
        <span>
          <strong>运营控制台</strong>
          <small>搭把手</small>
        </span>
      </router-link>

      <nav class="admin-nav" aria-label="管理后台导航">
        <router-link
          v-for="item in menuItems"
          :key="item.path"
          :to="item.path"
          class="admin-nav-item"
          :class="{ active: activePath === item.path }"
        >
          <el-icon><component :is="item.icon" /></el-icon>
          <span>{{ item.title }}</span>
        </router-link>
      </nav>
    </aside>

    <main class="admin-main">
      <header class="admin-topbar">
        <div>
          <p class="admin-kicker">{{ eyebrow }}</p>
          <h1>{{ title }}</h1>
          <p v-if="subtitle" class="admin-subtitle">{{ subtitle }}</p>
        </div>
        <div class="admin-actions">
          <slot name="actions" />
        </div>
      </header>

      <section class="admin-body">
        <slot />
      </section>
    </main>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRoute } from 'vue-router'

defineProps<{
  title: string
  subtitle?: string
  eyebrow?: string
}>()

const route = useRoute()
const menuItems = [
  { path: '/admin', title: '总览', icon: 'DataBoard' },
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
</script>

<style scoped lang="scss">
.admin-shell {
  display: grid;
  grid-template-columns: 236px minmax(0, 1fr);
  min-height: calc(100vh - #{$header-offset});
  background:
    linear-gradient(90deg, rgba(20, 184, 166, 0.06), transparent 360px),
    #f6f8f7;
}

.admin-rail {
  position: sticky;
  top: $header-offset;
  height: calc(100vh - #{$header-offset});
  border-right: 1px solid #dfe7e3;
  background: #fbfcfb;
  padding: 18px 14px;
}

.admin-brand {
  display: flex;
  align-items: center;
  gap: 10px;
  color: #17211f;
  margin-bottom: 18px;

  .brand-block {
    display: grid;
    place-items: center;
    width: 38px;
    height: 38px;
    border-radius: 8px;
    background: #123c36;
    color: #ffffff;
    font-weight: 800;
  }

  strong,
  small {
    display: block;
  }

  strong {
    font-size: 15px;
    line-height: 1.2;
  }

  small {
    margin-top: 2px;
    color: #6c7d77;
    font-size: 12px;
  }
}

.admin-nav {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.admin-nav-item {
  display: flex;
  align-items: center;
  gap: 10px;
  height: 40px;
  border-radius: 8px;
  padding: 0 11px;
  color: #4d625c;
  font-size: 14px;
  font-weight: 650;

  &:hover,
  &.active {
    background: #e4f3ef;
    color: #0f766e;
  }

  &.active {
    box-shadow: inset 3px 0 0 #0f766e;
  }
}

.admin-main {
  min-width: 0;
}

.admin-topbar {
  display: flex;
  justify-content: space-between;
  gap: 18px;
  padding: 26px 30px 18px;
  border-bottom: 1px solid #dfe7e3;
  background: rgba(246, 248, 247, 0.86);

  h1 {
    margin: 0;
    color: #17211f;
    font-size: 26px;
    font-weight: 800;
    letter-spacing: 0;
  }
}

.admin-kicker {
  margin: 0 0 5px;
  color: #0f766e;
  font-size: 12px;
  font-weight: 800;
}

.admin-subtitle {
  margin: 7px 0 0;
  color: #667a74;
  font-size: 14px;
}

.admin-actions {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-shrink: 0;
}

.admin-body {
  padding: 24px 30px 34px;
}

@media (max-width: 900px) {
  .admin-shell {
    grid-template-columns: 1fr;
  }

  .admin-rail {
    position: static;
    height: auto;
    border-right: 0;
    border-bottom: 1px solid #dfe7e3;
  }

  .admin-nav {
    flex-direction: row;
    overflow-x: auto;
  }

  .admin-nav-item {
    flex: 0 0 auto;
  }

  .admin-topbar,
  .admin-body {
    padding-right: 16px;
    padding-left: 16px;
  }
}
</style>
