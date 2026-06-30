<template>
  <div class="skill-card card-hover" @click="goDetail">
    <!-- 技能图片区域 -->
    <div class="card-image">
      <div class="cover-placeholder" :class="placeholderClass">
        <span class="placeholder-emoji">{{ categoryEmoji }}</span>
      </div>

      <!-- 标签叠加层 -->
      <div class="image-tags">
        <span v-if="skill.locationType === 2" class="tag tag-offline">线下</span>
        <span v-else-if="skill.locationType === 1" class="tag tag-online">线上</span>
        <span class="tag tag-trust" v-if="(skill.trustScore ?? 0) >= 4">金牌</span>
      </div>
    </div>

    <!-- 卡片信息 -->
    <div class="card-body">
      <!-- 标题 -->
      <h3 class="card-title text-ellipsis-2">{{ skill.title }}</h3>

      <!-- 底部：价格 + 用户 -->
      <div class="card-footer">
        <div class="price-row">
          <span class="price-value">{{ skill.pointPrice }}</span>
          <span class="price-unit">积分</span>
          <span v-if="skill.durationMinutes" class="duration">/ {{ skill.durationMinutes }}分钟</span>
        </div>

        <div class="user-row">
          <el-avatar :size="18" :src="skill.avatar" class="user-avatar">
            {{ (skill.nickname || '?').charAt(0) }}
          </el-avatar>
          <span class="user-name text-ellipsis">{{ skill.nickname || '匿名' }}</span>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import type { ShelfItemVo } from '@/types/api'

const props = defineProps<{
  skill: ShelfItemVo
}>()

const router = useRouter()

const categoryEmojiMap: Record<string, string> = {
  '学业辅导': '📚', '维修帮忙': '🔧', '设计美工': '🎨',
  '技术支持': '💻', '运动陪练': '🎾', '音乐艺术': '🎵',
  '生活服务': '🌿', '其他': '✅',
}

const categoryEmoji = computed(() => {
  const tagName = props.skill.skillTagName ?? ''
  return categoryEmojiMap[tagName] || '🎒'
})

const placeholderClass = computed(() => {
  const map: Record<string, string> = {
    '学业辅导': 'bg-blue', '维修帮忙': 'bg-orange', '设计美工': 'bg-purple',
    '技术支持': 'bg-teal', '运动陪练': 'bg-green', '音乐艺术': 'bg-pink',
    '生活服务': 'bg-amber',
  }
  const tagName = props.skill.skillTagName ?? ''
  return map[tagName] || 'bg-gray'
})

function goDetail() {
  router.push(`/skill/${props.skill.id}`)
}
</script>

<style scoped lang="scss">
.skill-card {
  background: #FFFFFF;
  border-radius: $radius-md;
  overflow: hidden;
  cursor: pointer;
  box-shadow: $shadow-sm;
}

// ========== 图片区域 ==========
.card-image {
  position: relative;
  width: 100%;
  padding-top: 75%; // 4:3 比例
  background: $color-bg;
  overflow: hidden;

  .cover-placeholder {
    position: absolute;
    top: 0;
    left: 0;
    width: 100%;
    height: 100%;
    display: flex;
    align-items: center;
    justify-content: center;

    .placeholder-emoji {
      font-size: 36px;
      opacity: 0.8;
    }

    &.bg-blue   { background: #E3F2FD; }
    &.bg-orange { background: #FFF3E0; }
    &.bg-purple { background: #F3E5F5; }
    &.bg-teal   { background: #E0F2F1; }
    &.bg-green  { background: #E8F5E9; }
    &.bg-pink   { background: #FCE4EC; }
    &.bg-amber  { background: #FFF8E1; }
    &.bg-gray   { background: #F5F5F5; }
  }
}

.image-tags {
  position: absolute;
  top: 6px;
  left: 6px;
  display: flex;
  gap: 4px;

  .tag {
    padding: 1px 6px;
    border-radius: 4px;
    font-size: 10px;
    font-weight: 600;

    &-offline {
      background: rgba(255, 107, 0, 0.85);
      color: #FFFFFF;
    }

    &-online {
      background: rgba(66, 165, 245, 0.85);
      color: #FFFFFF;
    }

    &-trust {
      background: rgba(255, 195, 0, 0.9);
      color: #333333;
    }
  }
}

// ========== 信息区域 ==========
.card-body {
  padding: 8px 10px 10px;
  position: relative;
}

.card-title {
  font-size: 13px;
  font-weight: 600;
  color: $color-text-primary;
  line-height: 1.4;
  margin: 0 0 8px;
  min-height: 36px;
}

.card-footer {
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
}

.price-row {
  display: flex;
  align-items: baseline;
  gap: 1px;

  .price-value {
    font-size: 17px;
    font-weight: 800;
    color: #FF6B00;
    line-height: 1;
  }

  .price-unit {
    font-size: 10px;
    color: $color-text-secondary;
    margin-left: 1px;
  }

  .duration {
    font-size: 10px;
    color: $color-text-placeholder;
    margin-left: 2px;
  }
}

.user-row {
  display: flex;
  align-items: center;
  gap: 4px;
  max-width: 80px;

  .user-avatar {
    flex-shrink: 0;
  }

  .user-name {
    font-size: 10px;
    color: $color-text-secondary;
    max-width: 50px;
  }
}
</style>
