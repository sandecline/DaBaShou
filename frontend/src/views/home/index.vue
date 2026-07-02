<template>
  <div class="home-page">
    <section class="hero">
      <div class="hero-copy">
        <span class="eyebrow">校园互助服务台</span>
        <h1>把身边同学的技能，变成马上可用的帮助</h1>
        <p>找辅导、约维修、接设计、发求助。用积分结算，用信用记录沉淀靠谱关系。</p>
        <div class="hero-actions">
          <el-button type="primary" size="large" @click="$router.push('/skill')">找技能</el-button>
          <el-button size="large" @click="$router.push('/demand/publish')">发布求助</el-button>
        </div>
      </div>

      <div class="hero-panel">
        <div class="panel-header">
          <span>今日看板</span>
          <el-tag type="success" effect="light">实时</el-tag>
        </div>
        <div class="panel-grid">
          <div v-for="item in stats" :key="item.label" class="panel-stat">
            <strong>{{ item.value }}</strong>
            <span>{{ item.label }}</span>
          </div>
        </div>
        <div class="panel-task">
          <div>
            <span class="task-label">推荐流程</span>
            <strong>发布需求后，系统会优先匹配高信任分同学</strong>
          </div>
          <el-icon><Connection /></el-icon>
        </div>
      </div>
    </section>

    <section class="quick-section">
      <button
        v-for="cat in categories"
        :key="cat.key"
        class="quick-item"
        type="button"
        @click="$router.push(cat.route)"
      >
        <span class="quick-icon" :class="cat.bgClass">
          <el-icon><component :is="cat.icon" /></el-icon>
        </span>
        <span>{{ cat.name }}</span>
      </button>
    </section>

    <div v-if="noticeText" class="notice-bar" @click="$router.push('/demand')">
      <el-icon><Bell /></el-icon>
      <span class="notice-text text-ellipsis">{{ noticeText }}</span>
      <el-icon><ArrowRight /></el-icon>
    </div>

    <div class="page-container">
      <section class="section">
        <div class="section-header">
          <div>
            <h2 class="section-title">热门技能</h2>
            <p>同学们最近更常预约的服务</p>
          </div>
          <router-link to="/skill" class="see-all">全部技能</router-link>
        </div>
        <LoadingSpinner v-if="skillLoading" text="加载中..." />
        <div v-else-if="hotSkills.length > 0" class="card-grid">
          <SkillCard v-for="skill in hotSkills" :key="skill.id" :skill="skill" />
        </div>
        <EmptyState
          v-else
          icon="技能"
          title="还没有技能服务"
          action-text="发布第一个技能"
          @action="$router.push('/skill/publish')"
        />
      </section>

      <section class="section">
        <div class="section-header">
          <div>
            <h2 class="section-title">最新求助</h2>
            <p>正在等待响应的校园需求</p>
          </div>
          <router-link to="/demand" class="see-all">全部求助</router-link>
        </div>
        <LoadingSpinner v-if="demandLoading" text="加载中..." />
        <div v-else-if="latestDemands.length > 0" class="card-grid">
          <DemandCard v-for="demand in latestDemands" :key="demand.id" :demand="demand" />
        </div>
        <EmptyState
          v-else
          icon="求助"
          title="暂无求助需求"
          action-text="发布第一个需求"
          @action="$router.push('/demand/publish')"
        />
      </section>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { searchShelves } from '@/api/shelf'
import { searchDemands } from '@/api/demand'
import { getPersonalOverview } from '@/api/stat'
import { getCategoryTree } from '@/api/skill'
import { getUserInfo, isLoggedIn } from '@/utils/auth'
import SkillCard from '@/components/common/SkillCard.vue'
import DemandCard from '@/components/common/DemandCard.vue'
import EmptyState from '@/components/common/EmptyState.vue'
import LoadingSpinner from '@/components/common/LoadingSpinner.vue'
import type { OverviewStat, SkillCategory, ShelfItemVo, DemandItemVo } from '@/types/api'

const hotSkills = ref<ShelfItemVo[]>([])
const latestDemands = ref<DemandItemVo[]>([])
const skillLoading = ref(true)
const demandLoading = ref(true)
const noticeText = ref('')

const fallbackCategories = [
  { key: 'study', name: '学业辅导', icon: 'Reading', route: '/skill?keyword=辅导', bgClass: 'bg-blue' },
  { key: 'repair', name: '维修帮忙', icon: 'Tools', route: '/skill?keyword=维修', bgClass: 'bg-orange' },
  { key: 'design', name: '设计美工', icon: 'Brush', route: '/skill?keyword=设计', bgClass: 'bg-purple' },
  { key: 'tech', name: '技术支持', icon: 'Monitor', route: '/skill?keyword=技术', bgClass: 'bg-teal' },
  { key: 'sport', name: '运动陪练', icon: 'Trophy', route: '/skill?keyword=运动', bgClass: 'bg-green' },
  { key: 'life', name: '生活服务', icon: 'House', route: '/skill?keyword=生活', bgClass: 'bg-amber' },
]

const categories = ref<Array<{ key: string; name: string; icon: string; route: string; bgClass: string }>>(fallbackCategories)

const stats = ref([
  { label: '技能服务', value: '0' },
  { label: '完成订单', value: '0' },
  { label: '好评率', value: '0%' },
])

function pageItems<T>(result: { list?: T[]; records?: T[] } | null | undefined): T[] {
  return result?.list || result?.records || []
}

function isNotMine(item: { userId?: number }): boolean {
  const userId = getUserInfo()?.id
  return !userId || item.userId !== userId
}

function categoryIcon(category: SkillCategory): string {
  const name = category.name
  if (name.includes('学')) return 'Reading'
  if (name.includes('修')) return 'Tools'
  if (name.includes('设计') || name.includes('美')) return 'Brush'
  if (name.includes('技术')) return 'Monitor'
  if (name.includes('运动')) return 'Trophy'
  if (name.includes('音乐') || name.includes('艺术')) return 'Headset'
  return 'House'
}

function categoryClass(index: number): string {
  return ['bg-blue', 'bg-orange', 'bg-purple', 'bg-teal', 'bg-green', 'bg-pink', 'bg-amber'][index % 7]
}

async function loadCategories() {
  try {
    const cats = await getCategoryTree()
    const activeCats = cats.filter(c => c.status !== 0).slice(0, 8)
    if (activeCats.length) {
      categories.value = activeCats.map((c, index) => ({
        key: String(c.id),
        name: c.name,
        icon: categoryIcon(c),
        route: `/skill?categoryId=${c.id}`,
        bgClass: categoryClass(index),
      }))
    }
  } catch {
    categories.value = fallbackCategories
  }
}

function applyOverview(overview: OverviewStat | null) {
  if (!overview) return
  stats.value = [
    { label: '技能服务', value: String(overview.totalSkills ?? overview.skillCount ?? overview.publishedSkills ?? 0) },
    { label: '完成订单', value: String(overview.completedOrders ?? 0) },
    { label: '好评率', value: `${Math.round((overview.orderCompletionRate ?? 0) * 100)}%` },
  ]
}

onMounted(async () => {
  loadCategories()
  try {
    const [skillResult, demandResult, overview] = await Promise.all([
      searchShelves({ pageNum: 1, pageSize: 8, sortBy: 'heat' }).catch(() => ({ records: [] as ShelfItemVo[], total: 0 })),
      searchDemands({ pageNum: 1, pageSize: 8 }).catch(() => ({ records: [] as DemandItemVo[], total: 0 })),
      isLoggedIn() ? getPersonalOverview().catch(() => null as OverviewStat | null) : Promise.resolve(null),
    ])

    hotSkills.value = pageItems<ShelfItemVo>(skillResult).filter(isNotMine)
    latestDemands.value = pageItems<DemandItemVo>(demandResult).filter(isNotMine)
    applyOverview(overview)

    if (latestDemands.value.length > 0) {
      const urgent = latestDemands.value.find(d => d.isUrgent)
      noticeText.value = urgent
        ? `急单求助：${urgent.title}，悬赏 ${urgent.pointReward} 积分`
        : `有 ${demandResult.total || latestDemands.value.length} 个新求助正在等待响应`
    }
  } finally {
    skillLoading.value = false
    demandLoading.value = false
  }
})
</script>

<style scoped lang="scss">
.home-page {
  padding-top: $spacing-lg;
}

.hero {
  display: grid;
  grid-template-columns: minmax(0, 1.25fr) minmax(320px, 0.75fr);
  gap: $spacing-xl;
  align-items: stretch;
  width: min(100% - 32px, $max-width);
  margin: 0 auto $spacing-lg;
  padding: $spacing-xl;
  border: 1px solid rgba(203, 213, 225, 0.75);
  border-radius: $radius-xl;
  background:
    linear-gradient(135deg, rgba(35, 100, 232, 0.1), rgba(16, 185, 129, 0.08)),
    #ffffff;
  box-shadow: $shadow-md;
}

.hero-copy {
  display: flex;
  flex-direction: column;
  justify-content: center;
  min-height: 270px;
}

.eyebrow {
  width: fit-content;
  margin-bottom: 14px;
  padding: 6px 10px;
  border: 1px solid rgba(35, 100, 232, 0.18);
  border-radius: $radius-round;
  background: rgba(35, 100, 232, 0.08);
  color: $color-primary;
  font-size: 12px;
  font-weight: 800;
}

h1 {
  max-width: 760px;
  color: $color-text-primary;
  font-size: clamp(34px, 5vw, 58px);
  line-height: 1.04;
  letter-spacing: 0;
  font-weight: 850;
}

.hero-copy p {
  max-width: 640px;
  margin-top: 18px;
  color: $color-text-secondary;
  font-size: 16px;
}

.hero-actions {
  display: flex;
  gap: 12px;
  margin-top: 28px;
}

.hero-panel {
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  min-height: 270px;
  padding: $spacing-lg;
  border: 1px solid $color-border;
  border-radius: $radius-lg;
  background: rgba(255, 255, 255, 0.86);
}

.panel-header,
.panel-task {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.panel-header {
  color: $color-text-primary;
  font-weight: 800;
}

.panel-grid {
  display: grid;
  gap: 12px;
  margin: $spacing-lg 0;
}

.panel-stat {
  display: flex;
  align-items: baseline;
  justify-content: space-between;
  padding: 13px 0;
  border-bottom: 1px solid $color-border-light;

  strong {
    color: $color-primary;
    font-size: 28px;
    line-height: 1;
  }

  span {
    color: $color-text-secondary;
    font-weight: 650;
  }
}

.panel-task {
  gap: 14px;
  padding: 14px;
  border-radius: $radius-md;
  background: $color-surface-muted;

  .task-label {
    display: block;
    margin-bottom: 4px;
    color: $color-text-placeholder;
    font-size: 12px;
    font-weight: 750;
  }

  strong {
    color: $color-text-regular;
    font-size: 14px;
  }

  .el-icon {
    flex-shrink: 0;
    color: $color-success;
    font-size: 28px;
  }
}

.quick-section {
  display: grid;
  grid-template-columns: repeat(6, 1fr);
  gap: 12px;
  width: min(100% - 32px, $max-width);
  margin: 0 auto $spacing-md;
}

.quick-item {
  appearance: none;
  border: 1px solid $color-border;
  border-radius: $radius-md;
  background: #ffffff;
  color: $color-text-regular;
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 10px;
  min-height: 76px;
  padding: 12px;
  text-align: left;
  font-weight: 750;
  transition: transform 0.16s ease, box-shadow 0.16s ease, border-color 0.16s ease;

  &:hover {
    transform: translateY(-2px);
    border-color: rgba(35, 100, 232, 0.28);
    box-shadow: $shadow-md;
  }
}

.quick-icon {
  display: grid;
  place-items: center;
  width: 38px;
  height: 38px;
  border-radius: $radius-md;
  font-size: 19px;

  &.bg-blue { background: #dbeafe; color: #1d4ed8; }
  &.bg-orange { background: #ffedd5; color: #c2410c; }
  &.bg-purple { background: #ede9fe; color: #6d28d9; }
  &.bg-teal { background: #ccfbf1; color: #0f766e; }
  &.bg-green { background: #dcfce7; color: #15803d; }
  &.bg-pink { background: #fce7f3; color: #be185d; }
  &.bg-amber { background: #fef3c7; color: #b45309; }
}

.notice-bar {
  display: flex;
  align-items: center;
  gap: 10px;
  width: min(100% - 32px, $max-width);
  margin: $spacing-md auto 0;
  padding: 12px 14px;
  border: 1px solid rgba(249, 115, 22, 0.18);
  border-radius: $radius-md;
  background: #fff7ed;
  color: #9a3412;
  cursor: pointer;

  .notice-text {
    flex: 1;
    font-weight: 650;
  }
}

.section {
  margin-bottom: $spacing-2xl;
}

.section-header {
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  gap: $spacing-md;
  margin-bottom: $spacing-md;

  p {
    margin-top: 6px;
    color: $color-text-secondary;
  }
}

.see-all {
  color: $color-primary;
  font-weight: 750;
}

@media (max-width: 980px) {
  .hero {
    grid-template-columns: 1fr;
  }

  .quick-section {
    grid-template-columns: repeat(3, 1fr);
  }
}

@media (max-width: 640px) {
  .home-page {
    padding-top: 12px;
  }

  .hero,
  .quick-section,
  .notice-bar {
    width: min(100% - 24px, $max-width);
  }

  .hero {
    padding: $spacing-lg;
  }

  .hero-copy {
    min-height: auto;
  }

  h1 {
    font-size: 34px;
  }

  .hero-actions {
    flex-direction: column;
  }

  .quick-section {
    grid-template-columns: 1fr 1fr;
  }

  .quick-item {
    min-height: 68px;
  }
}
</style>
