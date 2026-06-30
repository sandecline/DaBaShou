<template>
  <div class="message-page">
    <div class="page-container">
      <div class="message-layout">
        <!-- 左侧会话列表 -->
        <div class="conversations-panel">
          <h3>消息</h3>
          <LoadingSpinner v-if="loading" text="加载中..." />
          <template v-else>
            <div
              v-for="conv in conversations"
              :key="conv.id"
              class="conv-item"
              :class="{ active: activeConv === conv.otherUserId }"
              @click="selectConv(conv.otherUserId)"
            >
              <el-badge :value="conv.unreadCount" :hidden="conv.unreadCount === 0" :max="99">
                <el-avatar :size="44" :src="conv.otherAvatar">
                  {{ (conv.otherNickname || '?').charAt(0) }}
                </el-avatar>
              </el-badge>
              <div class="conv-info">
                <div class="conv-header">
                  <span class="conv-name">{{ conv.otherNickname }}</span>
                  <span class="conv-time">{{ fromNow(conv.lastTime) }}</span>
                </div>
                <p class="conv-last-msg text-ellipsis">{{ conv.lastMessage }}</p>
              </div>
            </div>
            <EmptyState v-if="conversations.length === 0" icon="💬" title="暂无消息" description="去技能广场找同学聊聊吧" />
          </template>
        </div>

        <!-- 右侧聊天窗口 -->
        <div class="chat-panel">
          <div v-if="!activeConv" class="chat-placeholder flex-center">
            <div>
              <span style="font-size: 48px">💬</span>
              <p>选择一个对话开始聊天</p>
            </div>
          </div>
          <ChatView v-else :target-user-id="activeConv" />
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { getConversations } from '@/api/message'
import { fromNow } from '@/utils/format'
import ChatView from './chat.vue'
import EmptyState from '@/components/common/EmptyState.vue'
import LoadingSpinner from '@/components/common/LoadingSpinner.vue'
import type { Conversation } from '@/types'

const loading = ref(true)
const conversations = ref<Conversation[]>([])
const activeConv = ref<number | null>(null)

function selectConv(targetUserId: number) {
  activeConv.value = targetUserId
}

onMounted(async () => {
  try {
    const result = await getConversations({ page: 1, size: 50 })
    conversations.value = result.records
    if (result.records.length > 0) {
      activeConv.value = result.records[0].otherUserId
    }
  } catch {
    // handled
  } finally {
    loading.value = false
  }
})
</script>

<style scoped lang="scss">
.message-layout {
  display: grid;
  grid-template-columns: 320px 1fr;
  height: calc(100vh - #{$header-offset} - 80px);
  background: #ffffff;
  border-radius: $radius-lg;
  overflow: hidden;
  border: 1px solid $color-border;
}

.conversations-panel {
  border-right: 1px solid $color-border;
  display: flex;
  flex-direction: column;
  overflow: hidden;

  h3 {
    padding: $spacing-md;
    margin: 0;
    font-size: $font-size-lg;
    border-bottom: 1px solid $color-border-light;
  }
}

.conv-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px $spacing-md;
  cursor: pointer;
  transition: background 0.15s;

  &:hover { background: $color-bg; }
  &.active { background: #FFF8E1; }

  .conv-info {
    flex: 1;
    min-width: 0;
  }

  .conv-header {
    display: flex;
    justify-content: space-between;
    align-items: center;

    .conv-name {
      font-weight: 600;
      font-size: $font-size-base;
    }

    .conv-time {
      font-size: 11px;
      color: $color-text-placeholder;
    }
  }

  .conv-last-msg {
    font-size: $font-size-xs;
    color: $color-text-secondary;
    margin-top: 2px;
  }
}

.chat-panel {
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.chat-placeholder {
  text-align: center;
  color: $color-text-secondary;
  flex: 1;

  p {
    margin-top: 12px;
  }
}

@media (max-width: 768px) {
  .message-layout {
    grid-template-columns: 1fr;
    height: calc(100vh - #{$header-offset} - 40px);
  }

  .conversations-panel {
    display: none;
  }
}
</style>
