<template>
  <div class="chat-view">
    <section class="chat-panel">
      <header class="chat-header">
        <el-avatar :size="44" :src="peerAvatar" class="peer-avatar">
          {{ peerName.charAt(0) }}
        </el-avatar>
        <div class="peer-info">
          <strong>{{ peerName }}</strong>
          <span>联系他，确认服务细节和时间地点</span>
        </div>
      </header>

      <main ref="msgListRef" class="chat-messages">
        <LoadingSpinner v-if="loading" text="加载消息..." />
        <template v-else>
          <div
            v-for="msg in messages"
            :key="msg.id"
            class="msg-row"
            :class="{ 'is-mine': msg.isMine }"
          >
            <el-avatar v-if="!msg.isMine" :size="36" :src="msg.senderAvatar || peerAvatar">
              {{ (msg.senderNickname || peerName).charAt(0) }}
            </el-avatar>
            <div class="msg-stack">
              <div class="msg-bubble" :class="{ 'is-mine': msg.isMine }">
                {{ msg.content }}
              </div>
              <div class="msg-time">{{ fromNow(msg.createTime) }}</div>
            </div>
            <el-avatar v-if="msg.isMine" :size="36" :src="myAvatar">
              {{ myName.charAt(0) }}
            </el-avatar>
          </div>

          <EmptyState
            v-if="messages.length === 0"
            icon="💬"
            title="开始聊天吧"
            description="发一条消息，和发布者确认服务安排"
          />
        </template>
      </main>

      <footer class="chat-input">
        <el-input
          v-model="inputText"
          type="textarea"
          :autosize="{ minRows: 2, maxRows: 4 }"
          placeholder="输入消息..."
          resize="none"
          @keyup.enter.exact.prevent="handleSend"
        />
        <el-button type="primary" :disabled="!canSend" :loading="sending" @click="handleSend">
          发送
        </el-button>
      </footer>
    </section>
  </div>
</template>

<script setup lang="ts">
import { computed, nextTick, onMounted, ref, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { getChatMessages, sendChatMessage } from '@/api/message'
import { useUserStore } from '@/stores/user'
import { fromNow } from '@/utils/format'
import EmptyState from '@/components/common/EmptyState.vue'
import LoadingSpinner from '@/components/common/LoadingSpinner.vue'
import type { ChatMessageVo } from '@/types/api'

const props = defineProps<{
  userId?: string | number
  targetUserId?: string | number
  targetUserName?: string
  targetUserAvatar?: string
}>()

const userStore = useUserStore()
const myAvatar = computed(() => userStore.user?.avatar || '')
const myName = computed(() => userStore.user?.nickname || '我')

const msgListRef = ref<HTMLElement>()
const loading = ref(false)
const sending = ref(false)
const messages = ref<ChatMessageVo[]>([])
const inputText = ref('')

const targetUserId = computed(() => Number(props.targetUserId ?? props.userId))
const canSend = computed(() => !!inputText.value.trim() && Number.isFinite(targetUserId.value) && !sending.value)
const peerMessage = computed(() => messages.value.find((msg) => !msg.isMine))
const peerName = computed(() => props.targetUserName || peerMessage.value?.senderNickname || '对方')
const peerAvatar = computed(() => props.targetUserAvatar || peerMessage.value?.senderAvatar || '')

async function loadMessages() {
  if (!Number.isFinite(targetUserId.value)) return

  loading.value = true
  try {
    const result = await getChatMessages(targetUserId.value, { page: 1, size: 50 })
    messages.value = result.list.map((msg) => ({
      ...msg,
      isMine: msg.senderId === userStore.user?.id,
    })).reverse()
    scrollToBottom()
  } catch {
    // request.ts already shows a unified error message.
  } finally {
    loading.value = false
  }
}

async function handleSend() {
  const text = inputText.value.trim()
  if (!text || !Number.isFinite(targetUserId.value)) return

  sending.value = true
  try {
    await sendChatMessage(targetUserId.value, text, 1)
    messages.value.push({
      id: Date.now(),
      senderId: userStore.user?.id!,
      senderNickname: userStore.user?.nickname || '',
      senderAvatar: userStore.user?.avatar || '',
      content: text,
      msgType: 1,
      isRead: 0,
      createTime: new Date().toISOString(),
      isMine: true,
    })
    inputText.value = ''
    scrollToBottom()
  } catch {
    ElMessage.error('发送失败')
  } finally {
    sending.value = false
  }
}

function scrollToBottom() {
  nextTick(() => {
    if (msgListRef.value) {
      msgListRef.value.scrollTop = msgListRef.value.scrollHeight
    }
  })
}

watch(targetUserId, loadMessages)
onMounted(loadMessages)
</script>

<style scoped lang="scss">
.chat-view {
  min-height: calc(100vh - 140px);
  padding: 24px;
  background:
    linear-gradient(135deg, rgba(255, 248, 225, 0.72), rgba(245, 247, 250, 0.82)),
    #f5f7fa;
}

.chat-panel {
  display: flex;
  flex-direction: column;
  max-width: 920px;
  height: calc(100vh - 188px);
  min-height: 560px;
  margin: 0 auto;
  overflow: hidden;
  background: #fff;
  border: 1px solid $color-border-light;
  border-radius: 8px;
  box-shadow: 0 16px 40px rgba(31, 35, 41, 0.08);
}

.chat-header {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 18px 22px;
  border-bottom: 1px solid $color-border-light;
  background: rgba(255, 255, 255, 0.92);
}

.peer-avatar {
  flex: 0 0 auto;
}

.peer-info {
  display: flex;
  min-width: 0;
  flex-direction: column;
  gap: 4px;

  strong {
    color: $color-text-primary;
    font-size: 17px;
    line-height: 1.25;
  }

  span {
    color: $color-text-secondary;
    font-size: 13px;
  }
}

.chat-messages {
  flex: 1;
  overflow-y: auto;
  padding: 22px;
  background: #f7f8fa;
}

.msg-row {
  display: flex;
  align-items: flex-end;
  gap: 10px;
  margin-bottom: 18px;

  &.is-mine {
    flex-direction: row-reverse;

    .msg-stack {
      align-items: flex-end;
    }
  }
}

.msg-stack {
  display: flex;
  max-width: min(68%, 560px);
  flex-direction: column;
  align-items: flex-start;
  gap: 6px;
}

.msg-bubble {
  padding: 11px 14px;
  border: 1px solid #edf0f4;
  border-radius: 8px 8px 8px 2px;
  background: #fff;
  color: $color-text-primary;
  font-size: 14px;
  line-height: 1.65;
  word-break: break-word;
  box-shadow: 0 6px 18px rgba(31, 35, 41, 0.05);

  &.is-mine {
    border-color: #ffd980;
    border-radius: 8px 8px 2px 8px;
    background: #fff3c4;
  }
}

.msg-time {
  color: $color-text-placeholder;
  font-size: 12px;
}

.chat-input {
  display: grid;
  grid-template-columns: 1fr 88px;
  gap: 12px;
  padding: 16px 18px;
  border-top: 1px solid $color-border-light;
  background: #fff;

  :deep(.el-textarea__inner) {
    min-height: 48px !important;
    border-radius: 8px;
    box-shadow: none;
  }

  .el-button {
    height: 48px;
    align-self: end;
    border-radius: 8px;
  }
}

@media (max-width: 768px) {
  .chat-view {
    min-height: calc(100vh - 96px);
    padding: 0;
  }

  .chat-panel {
    height: calc(100vh - 96px);
    min-height: 0;
    border-right: 0;
    border-left: 0;
    border-radius: 0;
  }

  .chat-header,
  .chat-messages,
  .chat-input {
    padding-right: 14px;
    padding-left: 14px;
  }

  .msg-stack {
    max-width: 76%;
  }

  .chat-input {
    grid-template-columns: 1fr 72px;
  }
}
</style>
