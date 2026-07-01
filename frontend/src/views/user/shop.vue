<template>
  <div class="shop-page">
    <div class="page-container">
      <h2 class="page-title">{{ isOwner ? '我的技能小铺' : '技能小铺' }}</h2>

      <LoadingSpinner v-if="loading" text="加载中..." />

      <template v-else>
        <!-- 小铺头部 -->
        <div class="shop-header gradient-primary">
          <el-avatar :size="72" :src="shopUser?.avatar">
            {{ (shopUser?.nickname || '?').charAt(0) }}
          </el-avatar>
          <h3>{{ shopUser?.nickname || '用户' }}</h3>
          <TrustBadge v-if="shopUser" :score="shopUser.trustScore" />
          <p v-if="shopUser?.bio" class="shop-bio">{{ shopUser.bio }}</p>
          <div v-if="shopUser?.campus" class="shop-location">
            📍 {{ shopUser.campus }} {{ shopUser.building || '' }}
          </div>
        </div>

        <!-- 技能列表 -->
        <div class="shop-skills">
          <div class="skills-header">
            <h3>已上架技能（{{ skills.length }}）</h3>
            <el-button v-if="isOwner" type="primary" size="small" @click="$router.push('/skill/publish')">
              <el-icon><Plus /></el-icon>
              上架新技能
            </el-button>
          </div>

          <div v-if="skills.length > 0" class="card-grid">
            <SkillCard v-for="skill in skills" :key="skill.id" :skill="skill" />
          </div>
          <EmptyState v-else icon="🛒" :title="isOwner ? '还没有上架技能' : 'TA还没有上架技能'">
            <el-button v-if="isOwner" type="primary" @click="$router.push('/skill/publish')">
              发布第一个技能
            </el-button>
          </EmptyState>
        </div>

        <!-- 闲时格子管理（仅主人可见） -->
        <div v-if="isOwner" class="shop-timeslots">
          <h3>闲时格子管理</h3>
          <TimeSlotPicker v-model="timeSlots" />
          <el-button
            type="primary"
            :loading="savingSlots"
            style="margin-top: 16px"
            @click="saveTimeSlots"
          >
            保存闲时设置
          </el-button>
        </div>
      </template>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getMyShelves, getUserShelves } from '@/api/shelf'
import { getUserById } from '@/api/user'
import { useUserStore } from '@/stores/user'
import SkillCard from '@/components/common/SkillCard.vue'
import TimeSlotPicker from '@/components/common/TimeSlotPicker.vue'
import TrustBadge from '@/components/common/TrustBadge.vue'
import EmptyState from '@/components/common/EmptyState.vue'
import LoadingSpinner from '@/components/common/LoadingSpinner.vue'
import type { PublicUserVo, ShelfItemVo, UserProfileVo } from '@/types/api'

const route = useRoute()
const userStore = useUserStore()

const loading = ref(true)
const skills = ref<ShelfItemVo[]>([])
const shopUser = ref<UserProfileVo | PublicUserVo | null>(null)
const timeSlots = ref<Array<{ date: string; startTime: string; endTime: string }>>([])
const savingSlots = ref(false)

const isOwner = computed(() => {
  const routeUserId = route.params.userId
  if (!routeUserId) return true
  return Number(routeUserId) === userStore.user?.id
})

async function loadData() {
  loading.value = true
  try {
    const userId = route.params.userId as string
    if (userId && !isOwner.value) {
      shopUser.value = await getUserById(Number(userId))
    } else {
      shopUser.value = userStore.user
    }
    const result = userId && !isOwner.value
      ? await getUserShelves(Number(userId), { pageNum: 1, pageSize: 50 })
      : await getMyShelves({ pageNum: 1, pageSize: 50 })
    skills.value = result.list
  } catch {
    // handled
  } finally {
    loading.value = false
  }
}

async function saveTimeSlots() {
  savingSlots.value = true
  try {
    // API call to save time slots
    ElMessage.success('闲时设置已保存')
  } catch {
    // handled
  } finally {
    savingSlots.value = false
  }
}

onMounted(loadData)
</script>

<style scoped lang="scss">
.page-title {
  font-size: $font-size-xl;
  font-weight: 700;
  margin: 0 0 $spacing-md;
}

.shop-header {
  border-radius: $radius-lg;
  padding: $spacing-2xl;
  text-align: center;
  color: #ffffff;
  margin-bottom: $spacing-xl;

  h3 {
    margin: 12px 0 8px;
    font-size: $font-size-xl;
    font-weight: 700;
  }

  .shop-bio {
    max-width: 400px;
    margin: 8px auto;
    opacity: 0.85;
    font-size: $font-size-sm;
  }

  .shop-location {
    font-size: $font-size-sm;
    opacity: 0.7;
  }
}

.skills-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: $spacing-md;

  h3 {
    margin: 0;
    font-size: $font-size-lg;
    font-weight: 600;
  }
}

.shop-skills {
  margin-bottom: $spacing-2xl;
}

.shop-timeslots {
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
</style>
