<template>
  <div class="home-page">
    <!-- 顶部 Banner -->
    <section class="home-banner gradient-primary">
      <div class="banner-content">
        <div class="banner-text">
          <h2 class="banner-title">搭把手，让互助更简单</h2>
          <p class="banner-subtitle">校园技能互助，有求必应 🤝</p>
        </div>
        <div class="banner-illustration">
          <span class="illustration-icon">🤝</span>
        </div>
      </div>
    </section>

    <!-- 八大分类入口 -->
    <section class="category-section">
      <div class="category-grid">
        <div
          v-for="cat in categories"
          :key="cat.key"
          class="category-item"
          @click="$router.push(cat.route)"
        >
          <div class="category-icon" :class="cat.bgClass">
            <span>{{ cat.icon }}</span>
          </div>
          <span class="category-label">{{ cat.name }}</span>
        </div>
      </div>
    </section>

    <!-- 通知横幅 -->
    <div class="notice-bar" v-if="noticeText" @click="$router.push('/demand')">
      <span class="notice-icon">📢</span>
      <span class="notice-text text-ellipsis">{{ noticeText }}</span>
      <span class="notice-arrow">›</span>
    </div>

    <!-- 统计数字条 -->
    <section class="stats-bar">
      <div class="stat-item">
        <span class="stat-num">{{ stats[0].value }}</span>
        <span class="stat-desc">{{ stats[0].label }}</span>
      </div>
      <div class="stat-divider"></div>
      <div class="stat-item">
        <span class="stat-num">{{ stats[1].value }}</span>
        <span class="stat-desc">{{ stats[1].label }}</span>
      </div>
      <div class="stat-divider"></div>
      <div class="stat-item">
        <span class="stat-num">{{ stats[2].value }}</span>
        <span class="stat-desc">{{ stats[2].label }}</span>
      </div>
      <div class="stat-divider"></div>
      <div class="stat-item">
        <span class="stat-num">{{ stats[3].value }}</span>
        <span class="stat-desc">{{ stats[3].label }}</span>
      </div>
    </section>

    <div class="page-container">
      <!-- 热门技能 -->
      <section class="section">
        <div class="section-header">
          <h2 class="section-title">🔥 热门技能</h2>
          <router-link to="/skill" class="see-all">全部 →</router-link>
        </div>
        <LoadingSpinner v-if="skillLoading" text="加载中..." />
        <div v-else-if="hotSkills.length > 0" class="card-grid">
          <SkillCard v-for="skill in hotSkills" :key="skill.id" :skill="skill" />
        </div>
        <EmptyState v-else icon="🎒" title="还没有技能服务" action-text="发布第一个技能" @action="$router.push('/skill/publish')" />
      </section>

      <!-- 最新求助 -->
      <section class="section">
        <div class="section-header">
          <h2 class="section-title">🙏 最新求助</h2>
          <router-link to="/demand" class="see-all">全部 →</router-link>
        </div>
        <LoadingSpinner v-if="demandLoading" text="加载中..." />
        <div v-else-if="latestDemands.length > 0" class="card-grid">
          <DemandCard v-for="demand in latestDemands" :key="demand.id" :demand="demand" />
        </div>
        <EmptyState v-else icon="📭" title="暂无求助需求" action-text="发布第一个需求" @action="$router.push('/demand/publish')" />
      </section>

      <!-- 底部品牌语 -->
      <div class="home-footer">
        <p>— 技能换时间 · 时间换服务 —</p>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { searchShelves } from '@/api/shelf'
import { searchDemands } from '@/api/demand'
import { getPlatformOverview } from '@/api/stat'
import SkillCard from '@/components/common/SkillCard.vue'
import DemandCard from '@/components/common/DemandCard.vue'
import EmptyState from '@/components/common/EmptyState.vue'
import LoadingSpinner from '@/components/common/LoadingSpinner.vue'
import type { ShelfItemVo, DemandItemVo, PlatformOverviewVo } from '@/types/api'

const hotSkills = ref<ShelfItemVo[]>([])
const latestDemands = ref<DemandItemVo[]>([])
const skillLoading = ref(true)
const demandLoading = ref(true)
const noticeText = ref('')

const categories = [
  { key: 'tutor', name: '学业辅导', icon: '📚', route: '/skill?cat=tutor', bgClass: 'bg-blue' },
  { key: 'repair', name: '维修帮忙', icon: '🔧', route: '/skill?cat=repair', bgClass: 'bg-orange' },
  { key: 'design', name: '设计美工', icon: '🎨', route: '/skill?cat=design', bgClass: 'bg-purple' },
  { key: 'tech', name: '电脑数码', icon: '💻', route: '/skill?cat=tech', bgClass: 'bg-teal' },
  { key: 'delivery', name: '跑腿代取', icon: '🏃', route: '/skill?cat=delivery', bgClass: 'bg-green' },
  { key: 'photo', name: '摄影摄像', icon: '📷', route: '/skill?cat=photo', bgClass: 'bg-pink' },
  { key: 'move', name: '搬家帮手', icon: '📦', route: '/skill?cat=move', bgClass: 'bg-amber' },
  { key: 'other', name: '其他服务', icon: '💡', route: '/skill?cat=other', bgClass: 'bg-gray' },
]

const stats = ref([
  { label: '注册用户', value: '0' },
  { label: '技能服务', value: '0' },
  { label: '完成订单', value: '0' },
  { label: '完成率', value: '0%' },
])

onMounted(async () => {
  try {
    const [skillResult, demandResult, overviewResult] = await Promise.all([
      searchShelves({ pageNum: 1, pageSize: 6, sortBy: 'heat' }).catch(() => null),
      searchDemands({ pageNum: 1, pageSize: 4, status: 1, sortBy: 'time' }).catch(() => null),
      getPlatformOverview().catch(() => null),
    ])

    if (skillResult?.data) {
      hotSkills.value = skillResult.list
    }

    if (demandResult?.data) {
      latestDemands.value = demandResult.list
    }

    if (overviewResult?.data) {
      const o = overviewResult
      stats.value = [
        { label: '注册用户', value: String(o.totalUsers || 0) },
        { label: '技能服务', value: String(o.totalShelves || 0) },
        { label: '完成订单', value: String(o.totalOrders || 0) },
        { label: '今日新增', value: String(o.todayNewUsers || 0) },
      ]
    }

    // 模拟通知
    if (latestDemands.value.length > 0) {
      noticeText.value = `🙏 有 ${demandResult?.total || latestDemands.value.length} 个新求助等你来接！`
    }
  } finally {
    skillLoading.value = false
    demandLoading.value = false
  }
})
</script>

<style scoped lang="scss">
// ========== Banner ==========
.home-banner {
  padding: 16px $spacing-md;
  color: #FFFFFF;

  .banner-content {
    display: flex;
    align-items: center;
    justify-content: space-between;
  }

  .banner-text {
    .banner-title {
      font-size: 20px;
      font-weight: 800;
      margin: 0 0 4px;
      letter-spacing: 0.5px;
    }

    .banner-subtitle {
      font-size: 13px;
      opacity: 0.85;
      margin: 0;
    }
  }

  .banner-illustration {
    .illustration-icon {
      font-size: 48px;
      filter: drop-shadow(0 2px 4px rgba(0,0,0,0.15));
    }
  }
}

// ========== 分类图标网格 ==========
.category-section {
  background: #FFFFFF;
  padding: 16px $spacing-sm 12px;
}

.category-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 12px 4px;
  max-width: $max-width;
  margin: 0 auto;
}

.category-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 6px;
  cursor: pointer;
  transition: transform 0.15s;

  &:active {
    transform: scale(0.92);
  }
}

.category-icon {
  width: 52px;
  height: 52px;
  border-radius: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;

  &.bg-blue   { background: #E3F2FD; }
  &.bg-orange { background: #FFF3E0; }
  &.bg-purple { background: #F3E5F5; }
  &.bg-teal   { background: #E0F2F1; }
  &.bg-green  { background: #E8F5E9; }
  &.bg-pink   { background: #FCE4EC; }
  &.bg-amber  { background: #FFF8E1; }
  &.bg-gray   { background: #F5F5F5; }
}

.category-label {
  font-size: 11px;
  color: $color-text-regular;
  font-weight: 500;
}

// ========== 通知横幅 ==========
.notice-bar {
  display: flex;
  align-items: center;
  gap: 8px;
  margin: 8px $spacing-md;
  padding: 10px 14px;
  background: #FFFFFF;
  border-radius: $radius-sm;
  cursor: pointer;
  box-shadow: $shadow-sm;

  .notice-icon {
    font-size: 15px;
    flex-shrink: 0;
  }

  .notice-text {
    flex: 1;
    font-size: 12px;
    color: $color-text-regular;
  }

  .notice-arrow {
    font-size: 18px;
    color: $color-text-placeholder;
    flex-shrink: 0;
  }
}

// ========== 统计条 ==========
.stats-bar {
  display: flex;
  align-items: center;
  justify-content: space-around;
  margin: 12px $spacing-md;
  padding: 14px 8px;
  background: #FFFFFF;
  border-radius: $radius-sm;
  box-shadow: $shadow-sm;
}

.stat-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 2px;

  .stat-num {
    font-size: 18px;
    font-weight: 800;
    color: $color-primary-dark;
  }

  .stat-desc {
    font-size: 10px;
    color: $color-text-secondary;
  }
}

.stat-divider {
  width: 1px;
  height: 24px;
  background: $color-border;
}

// ========== 板块 ==========
.section {
  margin-bottom: $spacing-lg;
}

.section-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: $spacing-sm;

  .section-title {
    font-size: $font-size-lg;
    font-weight: 700;
    color: $color-text-primary;
    display: flex;
    align-items: center;
    gap: 6px;

    &::before {
      content: '';
      width: 3px;
      height: 16px;
      background: $color-primary;
      border-radius: 2px;
    }
  }

  .see-all {
    font-size: $font-size-sm;
    color: $color-text-secondary;
    text-decoration: none;

    &:hover {
      color: $color-primary-dark;
    }
  }
}

// ========== 底部品牌语 ==========
.home-footer {
  text-align: center;
  padding: $spacing-lg 0;
  color: $color-text-placeholder;
  font-size: $font-size-sm;
}

// ========== 响应式 ==========
@media (min-width: 769px) {
  .home-banner {
    .banner-content {
      max-width: $max-width;
      margin: 0 auto;
    }
  }

  .category-grid {
    grid-template-columns: repeat(8, 1fr);
    max-width: $max-width;
  }

  .stats-bar,
  .notice-bar {
    max-width: $max-width;
    margin: 12px auto;
  }
}

@media (max-width: 480px) {
  .category-icon {
    width: 44px;
    height: 44px;
    font-size: 20px;
  }

  .home-banner {
    padding: 12px $spacing-md;

    .banner-title {
      font-size: 17px;
    }
  }

  .stats-bar {
    margin: 8px 12px;
    padding: 12px 4px;
  }
}
</style>
