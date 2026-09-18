<template>
  <article class="skill-card card-hover" @click="goDetail">
    <div class="card-media">
      <img
        v-if="skill.coverImage"
        :src="skill.coverImage"
        :alt="skill.title"
        class="cover-img"
      />
      <div v-else class="cover-placeholder" :class="placeholderClass">
        <el-icon><component :is="categoryIcon" /></el-icon>
      </div>

      <div class="media-tags">
        <span class="tag">{{ locationText }}</span>
        <span v-if="(skill.trustScore ?? 0) >= 4" class="tag tag-trust">金牌</span>
      </div>
    </div>

    <div class="card-body">
      <div class="title-row">
        <h3 class="card-title text-ellipsis-2">{{ skill.title }}</h3>
        <span v-if="skill.distance != null" class="distance">{{ formatDistance(skill.distance) }}</span>
      </div>

      <div class="meta-row">
        <span>{{ skill.tagName || skill.skillTagName || '技能服务' }}</span>
        <span v-if="skill.durationMinutes">{{ skill.durationMinutes }} 分钟</span>
      </div>

      <div class="footer-row">
        <div class="user-row">
          <el-avatar :size="24" :src="skill.userAvatar || skill.avatar">
            {{ displayName.charAt(0) }}
          </el-avatar>
          <span class="user-name text-ellipsis">{{ displayName }}</span>
        </div>

        <div class="price-row">
          <strong>{{ skill.pointPrice }}</strong>
          <span>积分</span>
        </div>
      </div>
    </div>
  </article>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import type { SkillShelf } from '@/types'
import { formatDistance } from '@/utils/format'

const props = defineProps<{
  skill: SkillShelf
}>()

const router = useRouter()

const displayName = computed(() => props.skill.userName || props.skill.nickname || '匿名同学')

const locationText = computed(() => {
  if (props.skill.locationType === 2) return '线下'
  if (props.skill.locationType === 1) return '线上'
  return '不限'
})

const categoryIcon = computed(() => {
  const name = props.skill.tagName || props.skill.skillTagName || ''
  if (name.includes('学')) return 'Reading'
  if (name.includes('修')) return 'Tools'
  if (name.includes('设计') || name.includes('美')) return 'Brush'
  if (name.includes('技术')) return 'Monitor'
  if (name.includes('运动')) return 'Trophy'
  return 'Briefcase'
})

const placeholderClass = computed(() => {
  const name = props.skill.tagName || props.skill.skillTagName || ''
  if (name.includes('学')) return 'bg-blue'
  if (name.includes('修')) return 'bg-orange'
  if (name.includes('设计') || name.includes('美')) return 'bg-purple'
  if (name.includes('技术')) return 'bg-teal'
  if (name.includes('运动')) return 'bg-green'
  return 'bg-gray'
})

function goDetail() {
  router.push(`/skill/${props.skill.id}`)
}
</script>

<style scoped lang="scss">
.skill-card {
  overflow: hidden;
  border: 1px solid $color-border;
  border-radius: $radius-md;
  background: #ffffff;
  cursor: pointer;
}

.card-media {
  position: relative;
  aspect-ratio: 16 / 10;
  background: $color-surface-muted;
  overflow: hidden;
}

.cover-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.cover-placeholder {
  display: grid;
  place-items: center;
  width: 100%;
  height: 100%;
  font-size: 42px;

  &.bg-blue { background: #dbeafe; color: #1d4ed8; }
  &.bg-orange { background: #ffedd5; color: #c2410c; }
  &.bg-purple { background: #ede9fe; color: #6d28d9; }
  &.bg-teal { background: #ccfbf1; color: #0f766e; }
  &.bg-green { background: #dcfce7; color: #15803d; }
  &.bg-gray { background: #f1f5f9; color: #64748b; }
}

.media-tags {
  position: absolute;
  top: 10px;
  left: 10px;
  display: flex;
  gap: 6px;
}

.tag {
  display: inline-flex;
  align-items: center;
  min-height: 24px;
  padding: 0 9px;
  border-radius: $radius-round;
  background: rgba(15, 23, 42, 0.74);
  color: #ffffff;
  font-size: 12px;
  font-weight: 750;
  backdrop-filter: blur(8px);
}

.tag-trust {
  background: rgba(245, 158, 11, 0.92);
}

.card-body {
  padding: 14px;
}

.title-row {
  display: flex;
  align-items: flex-start;
  gap: 10px;
}

.card-title {
  flex: 1;
  min-height: 42px;
  margin: 0;
  color: $color-text-primary;
  font-size: 15px;
  font-weight: 760;
  line-height: 1.4;
}

.distance {
  flex-shrink: 0;
  color: $color-text-placeholder;
  font-size: 12px;
  font-weight: 650;
}

.meta-row {
  display: flex;
  gap: 8px;
  margin-top: 10px;
  color: $color-text-secondary;
  font-size: 12px;

  span {
    min-width: 0;
    padding: 4px 8px;
    border-radius: $radius-round;
    background: $color-surface-muted;
  }
}

.footer-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  margin-top: 14px;
}

.user-row {
  display: flex;
  align-items: center;
  gap: 7px;
  min-width: 0;
}

.user-name {
  color: $color-text-secondary;
  font-size: 13px;
  font-weight: 650;
}

.price-row {
  display: flex;
  align-items: baseline;
  gap: 3px;
  flex-shrink: 0;
  color: $color-accent;

  strong {
    font-size: 22px;
    line-height: 1;
  }

  span {
    font-size: 12px;
    font-weight: 700;
  }
}
</style>
