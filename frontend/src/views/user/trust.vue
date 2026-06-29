<template>
  <div class="trust-page">
    <div class="page-container">
      <h2 class="page-title">信任分</h2>

      <LoadingSpinner v-if="loading" text="加载中..." />

      <template v-else>
        <!-- 分数卡片 -->
        <div class="trust-header gradient-primary">
          <div class="score-circle">
            <span class="score-value">{{ score.toFixed(1) }}</span>
            <span class="score-max">/ 5.0</span>
          </div>
          <div class="trust-level">
            <el-icon :size="24"><Medal /></el-icon>
            <span>{{ trustLevel.label }}</span>
          </div>
          <p class="trust-desc">
            {{ trustLevel.level === 'gold' ? '你是平台最值得信赖的用户！' : trustLevel.level === 'reliable' ? '继续保持，离金牌不远啦！' : '努力积累信任，成为靠谱伙伴！' }}
          </p>
        </div>

        <!-- 等级说明 -->
        <div class="level-cards">
          <div class="level-card" :class="{ active: trustLevel.level === 'newcomer' }">
            <div class="level-icon">🌱</div>
            <div class="level-name">新人</div>
            <div class="level-range">0 - 2.9分</div>
          </div>
          <div class="level-card" :class="{ active: trustLevel.level === 'reliable' }">
            <div class="level-icon">⭐</div>
            <div class="level-name">靠谱</div>
            <div class="level-range">3.0 - 3.9分</div>
          </div>
          <div class="level-card" :class="{ active: trustLevel.level === 'gold' }">
            <div class="level-icon">👑</div>
            <div class="level-name">金牌</div>
            <div class="level-range">4.0 - 5.0分</div>
          </div>
        </div>

        <!-- 变动记录 -->
        <div class="log-card">
          <h3>信任分变动记录</h3>
          <el-table :data="logs" stripe v-loading="logLoading" empty-text="暂无变动记录">
            <el-table-column prop="reason" label="变动原因" min-width="200" show-overflow-tooltip />
            <el-table-column label="变动" width="100" align="center">
              <template #default="{ row }">
                <span :class="row.changeAmount > 0 ? 'text-success' : 'text-danger'">
                  {{ row.changeAmount > 0 ? '+' : '' }}{{ row.changeAmount.toFixed(1) }}
                </span>
              </template>
            </el-table-column>
            <el-table-column label="变动前" width="80" align="center">
              <template #default="{ row }">{{ row.beforeScore.toFixed(1) }}</template>
            </el-table-column>
            <el-table-column label="变动后" width="80" align="center">
              <template #default="{ row }">{{ row.afterScore.toFixed(1) }}</template>
            </el-table-column>
            <el-table-column label="时间" width="170">
              <template #default="{ row }">{{ formatDateTime(row.createTime) }}</template>
            </el-table-column>
          </el-table>
        </div>
      </template>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useUserStore } from '@/stores/user'
import { getTrustLevel, formatDateTime } from '@/utils/format'
import EmptyState from '@/components/common/EmptyState.vue'
import LoadingSpinner from '@/components/common/LoadingSpinner.vue'
import type { TrustScoreLog } from '@/types'

const userStore = useUserStore()
const loading = ref(false)
const logLoading = ref(false)
const logs = ref<TrustScoreLog[]>([])

const score = ref(userStore.user?.trustScore || 5.0)
const trustLevel = ref(getTrustLevel(score.value))

onMounted(async () => {
  // Log data would come from API
})
</script>

<style scoped lang="scss">
.page-title {
  font-size: $font-size-xl;
  font-weight: 700;
  margin: 0 0 $spacing-md;
}

.trust-header {
  border-radius: $radius-lg;
  padding: $spacing-2xl;
  text-align: center;
  color: #ffffff;
  margin-bottom: $spacing-xl;

  .score-circle {
    display: inline-flex;
    align-items: baseline;
    gap: 4px;
    margin-bottom: 8px;

    .score-value {
      font-size: 56px;
      font-weight: 800;
      line-height: 1;
    }

    .score-max {
      font-size: $font-size-lg;
      opacity: 0.7;
    }
  }

  .trust-level {
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 6px;
    font-size: $font-size-xl;
    font-weight: 700;
    margin-bottom: 8px;
  }

  .trust-desc {
    opacity: 0.85;
    font-size: $font-size-sm;
  }
}

.level-cards {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: $spacing-md;
  margin-bottom: $spacing-xl;
}

.level-card {
  background: #ffffff;
  border: 2px solid $color-border;
  border-radius: $radius-md;
  padding: $spacing-lg;
  text-align: center;
  transition: all 0.2s;

  &.active {
    border-color: $color-primary;
    background: #FFF8E1;
  }

  .level-icon { font-size: 32px; margin-bottom: 4px; }
  .level-name { font-weight: 700; font-size: $font-size-base; }
  .level-range { font-size: $font-size-xs; color: $color-text-secondary; }
}

.log-card {
  background: #ffffff;
  border-radius: $radius-lg;
  padding: $spacing-xl;
  border: 1px solid $color-border;

  h3 {
    margin: 0 0 $spacing-md;
    font-size: $font-size-lg;
    font-weight: 600;
  }
}

.text-success { color: $color-success; font-weight: 600; }
.text-danger { color: $color-danger; font-weight: 600; }

@media (max-width: 768px) {
  .level-cards {
    grid-template-columns: 1fr;
  }
}
</style>
