<template>
  <div class="chat-view">
    <!-- 聊天头部 -->
    <div class="chat-header">
      <span class="chat-title">{{ targetUserName }}</span>
    </div>

    <!-- 消息列表 -->
    <div ref="msgListRef" class="chat-messages">
      <LoadingSpinner v-if="loading" text="加载消息..." />
      <template v-else>
        <div
          v-for="msg in messages"
          :key="msg.id"
          class="msg-bubble-wrap"
          :class="{ 'is-mine': msg.isMine }"
        >
          <el-avatar v-if="!msg.isMine" :size="32" :src="targetUserAvatar">
            {{ targetUserName?.charAt(0) }}
          </el-avatar>
          <div class="msg-bubble" :class="{ 'is-mine': msg.isMine }">
            <div class="msg-content">{{ msg.content }}</div>
            <div class="msg-time">{{ fromNow(msg.createTime) }}</div>
          </div>
          <el-avatar v-if="msg.isMine" :size="32" :src="myAvatar">
            {{ myName?.charAt(0) }}
          </el-avatar>
        </div>
        <EmptyState v-if="messages.length === 0" icon="💬" title="开始聊天吧" description="发送第一条消息打个招呼" />
      </template>
    </div>

    <!-- 输入框 -->
    <div class="chat-input">
      <el-input
        v-model="inputText"
        type="textarea"
        :rows="2"
        placeholder="输入消息..."
        resize="none"
        @keyup.enter.exact.prevent="handleSend"
      />
      <el-button type="primary" :disabled="!inputText.trim()" @click="handleSend">
        发送
      </el-button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, nextTick, onMounted, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { getChatHistory, createSession } from '@/api/message'
import { useUserStore } from '@/stores/user'
import { fromNow } from '@/utils/format'
import EmptyState from '@/components/common/EmptyState.vue'
import LoadingSpinner from '@/components/common/LoadingSpinner.vue'
import type { ChatMessageVo } from '@/types/api'

const props = defineProps<{
  targetUserId: number
  targetUserName?: string
  targetUserAvatar?: string
}>()

const userStore = useUserStore()
const myAvatar = ref(userStore.user?.avatar || '')
const myName = ref(userStore.user?.nickname || '')

const msgListRef = ref<HTMLElement>()
const loading = ref(false)
const messages = ref<Array<ChatMessageVo & { isMine: boolean }>>([])
const inputText = ref('')
const sessionId = ref<number | null>(null)

async function loadMessages() {
  loading.value = true
  try {
    // Try to create or get session
    const sessionResult = await createSession(props.targetUserId)
    sessionId.value = sessionResult
    const result = await getChatHistory(sessionResult, { pageNum: 1, pageSize: 50 })
    messages.value = result.list.map((m: ChatMessageVo) => ({
      ...m,
      isMine: m.senderId === userStore.user?.id,
    })).reverse()
    scrollToBottom()
  } catch {
    // handled
  } finally {
    loading.value = false
  }
}

async function handleSend() {
  const text = inputText.value.trim()
  if (!text) return

  try {
    // Optimistic push
    messages.value.push({
      id: Date.now(),
      senderId: userStore.user?.id || 0,
      senderNickname: myName.value,
      senderAvatar: myAvatar.value,
      content: text,
      msgType: 1,
      isRead: 0,
      createTime: new Date().toISOString(),
      isMine: true,
    } as ChatMessageVo & { isMine: boolean })
    inputText.value = ''
    scrollToBottom()
  } catch {
    ElMessage.error('发送失败')
  }
}

function scrollToBottom() {
  nextTick(() => {
    if (msgListRef.value) {
      msgListRef.value.scrollTop = msgListRef.value.scrollHeight
    }
  })
}

watch(() => props.targetUserId, loadMessages)
onMounted(loadMessages)
</script>

<style scoped lang="scss">
.chat-view {
  display: flex;
  flex-direction: column;
  height: 100%;
}

.chat-header {
  padding: 12px $spacing-md;
  border-bottom: 1px solid $color-border-light;

  .chat-title {
    font-weight: 600;
    font-size: $font-size-base;
  }
}

.chat-messages {
  flex: 1;
  overflow-y: auto;
  padding: $spacing-md;
}

.msg-bubble-wrap {
  display: flex;
  align-items: flex-end;
  gap: 8px;
  margin-bottom: 16px;

  &.is-mine {
    flex-direction: row-reverse;
  }
}

.msg-bubble {
  max-width: 60%;
  padding: 10px 14px;
  border-radius: 16px;
  background: $color-bg;
  border-bottom-left-radius: 4px;

  &.is-mine {
    background: #FFF8E1;
    border-bottom-right-radius: 4px;
    border-bottom-left-radius: 16px;
  }

  .msg-content {
    font-size: $font-size-base;
    word-break: break-word;
  }

  .msg-time {
    font-size: 10px;
    color: $color-text-placeholder;
    margin-top: 4px;
    text-align: right;
  }
}

.chat-input {
  display: flex;
  align-items: flex-end;
  gap: 8px;
  padding: 12px $spacing-md;
  border-top: 1px solid $color-border-light;
}
</style>
