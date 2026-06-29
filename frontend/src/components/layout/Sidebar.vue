<template>
  <aside class="app-sidebar">
    <el-menu
      :default-active="activeMenu"
      :router="true"
      :collapse="collapsed"
      class="sidebar-menu"
    >
      <template v-for="item in menuItems" :key="item.path">
        <el-sub-menu v-if="item.children" :index="item.path">
          <template #title>
            <el-icon><component :is="item.icon" /></el-icon>
            <span>{{ item.title }}</span>
          </template>
          <el-menu-item v-for="child in item.children" :key="child.path" :index="child.path">
            {{ child.title }}
          </el-menu-item>
        </el-sub-menu>
        <el-menu-item v-else :index="item.path">
          <el-icon><component :is="item.icon" /></el-icon>
          <span>{{ item.title }}</span>
        </el-menu-item>
      </template>
    </el-menu>

    <div class="sidebar-footer">
      <el-button text @click="collapsed = !collapsed">
        <el-icon><component :is="collapsed ? 'Expand' : 'Fold'" /></el-icon>
      </el-button>
    </div>
  </aside>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { useRoute } from 'vue-router'

const props = defineProps<{
  menuItems: Array<{
    path: string
    title: string
    icon?: string
    children?: Array<{ path: string; title: string }>
  }>
}>()

const route = useRoute()
const collapsed = ref(false)

const activeMenu = computed(() => route.path)
</script>

<style scoped lang="scss">
.app-sidebar {
  width: $sidebar-width;
  min-height: calc(100vh - #{$header-offset});
  background: #ffffff;
  border-right: 1px solid $color-border;
  display: flex;
  flex-direction: column;
  transition: width 0.3s;

  .sidebar-menu:not(.el-menu--collapse) {
    width: $sidebar-width;
  }
}

.sidebar-menu {
  flex: 1;
  border-right: none;

  .el-menu-item {
    &.is-active {
      background-color: #FFF8E1;
      color: $color-primary;
    }
  }
}

.sidebar-footer {
  padding: 12px;
  text-align: center;
  border-top: 1px solid $color-border-light;
}
</style>
