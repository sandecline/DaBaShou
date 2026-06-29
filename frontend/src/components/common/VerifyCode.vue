<template>
  <div class="verify-code">
    <div v-if="mode === 'display'" class="code-display">
      <div class="code-bg">
        <span
          v-for="(ch, idx) in code.split('')"
          :key="idx"
          class="code-char"
          :style="{ transform: `rotate(${(idx - 2) * 3}deg)`, color: codeColors[idx] }"
        >{{ ch }}</span>
      </div>
      <div v-if="expireAt" class="code-countdown">
        <el-icon><Timer /></el-icon>
        <span :class="{ 'is-expiring': countdown <= 60 }">
          {{ formatCountdown(countdown) }}
        </span>
      </div>
      <p class="code-tip">请向服务方出示此码，完成核销</p>
    </div>

    <div v-else class="code-input">
      <p class="input-label">请输入6位核销码</p>
      <div class="code-input-boxes">
        <input
          v-for="(_, idx) in 6"
          :key="idx"
          :ref="(el) => { if (el) inputRefs[idx] = el as HTMLInputElement }"
          v-model="codeDigits[idx]"
          type="text"
          maxlength="1"
          inputmode="numeric"
          pattern="[0-9]"
          class="code-digit-input"
          :class="{ 'is-filled': codeDigits[idx] }"
          @input="handleDigitInput(idx)"
          @keydown.backspace="handleBackspace(idx)"
          @paste="handlePaste"
        />
      </div>
      <p v-if="errorMsg" class="error-msg">{{ errorMsg }}</p>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted, watch } from 'vue'
import { formatCountdown } from '@/utils/format'

const props = withDefaults(defineProps<{
  mode?: 'display' | 'input'
  code?: string
  expireAt?: string | null
  errorMsg?: string
}>(), {
  mode: 'display',
  code: '',
  expireAt: null,
  errorMsg: '',
})

const emit = defineEmits<{
  complete: [code: string]
}>()

// Display mode — code colors
const codeColors = ['#FFC300', '#26C6B8', '#FF9800', '#FF5252', '#42A5F5', '#FF8F00']

// Countdown timer
const countdown = ref(0)
let timer: ReturnType<typeof setInterval> | null = null

function calcCountdown() {
  if (!props.expireAt) return 0
  const diff = new Date(props.expireAt).getTime() - Date.now()
  return Math.max(0, Math.floor(diff / 1000))
}

onMounted(() => {
  if (props.mode === 'display' && props.expireAt) {
    countdown.value = calcCountdown()
    timer = setInterval(() => {
      countdown.value = calcCountdown()
    }, 1000)
  }
})

onUnmounted(() => {
  if (timer) clearInterval(timer)
})

// Input mode
const codeDigits = ref<string[]>(Array(6).fill(''))
const inputRefs = ref<HTMLInputElement[]>([])

function handleDigitInput(idx: number) {
  const val = codeDigits.value[idx]
  if (val && !/^\d$/.test(val)) {
    codeDigits.value[idx] = ''
    return
  }
  // Auto-focus next
  if (val && idx < 5) {
    inputRefs.value[idx + 1]?.focus()
  }
  // Check completion
  if (codeDigits.value.every((d) => d && /^\d$/.test(d))) {
    emit('complete', codeDigits.value.join(''))
  }
}

function handleBackspace(idx: number) {
  if (!codeDigits.value[idx] && idx > 0) {
    inputRefs.value[idx - 1]?.focus()
  }
}

function handlePaste(e: ClipboardEvent) {
  e.preventDefault()
  const text = e.clipboardData?.getData('text')?.replace(/\D/g, '').slice(0, 6)
  if (text) {
    for (let i = 0; i < 6; i++) {
      codeDigits.value[i] = text[i] || ''
    }
    if (text.length === 6) {
      emit('complete', text)
    }
  }
}
</script>

<style scoped lang="scss">
.verify-code {
  padding: $spacing-md;
}

// Display mode
.code-display {
  text-align: center;
}

.code-bg {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  padding: $spacing-xl;
  background: linear-gradient(135deg, #f5f7ff 0%, #ecfdf5 25%, #fffbeb 50%, #fef2f2 75%, #f0f9ff 100%);
  background-size: 200% 200%;
  animation: gradient-shift 3s ease-in-out infinite;
  border-radius: $radius-lg;
  margin-bottom: $spacing-md;
}

@keyframes gradient-shift {
  0%, 100% { background-position: 0% 50%; }
  50% { background-position: 100% 50%; }
}

.code-char {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 44px;
  height: 56px;
  font-size: 28px;
  font-weight: 700;
  font-family: 'SF Mono', 'Cascadia Code', monospace;
  background: rgba(255, 255, 255, 0.8);
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.06);
}

.code-countdown {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  font-size: $font-size-lg;
  font-weight: 700;

  .is-expiring {
    color: $color-danger;
    animation: pulse 1s infinite;
  }
}

@keyframes pulse {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.5; }
}

.code-tip {
  margin-top: 8px;
  font-size: $font-size-xs;
  color: $color-text-secondary;
}

// Input mode
.code-input {
  text-align: center;
}

.input-label {
  font-size: $font-size-base;
  color: $color-text-regular;
  margin-bottom: $spacing-md;
}

.code-input-boxes {
  display: flex;
  justify-content: center;
  gap: 10px;
}

.code-digit-input {
  width: 48px;
  height: 56px;
  text-align: center;
  font-size: 24px;
  font-weight: 700;
  font-family: 'SF Mono', 'Cascadia Code', monospace;
  border: 2px solid $color-border;
  border-radius: 8px;
  outline: none;
  transition: border-color 0.15s;

  &:focus {
    border-color: $color-primary;
    box-shadow: 0 0 0 3px rgba(255, 195, 0, 0.15);
  }

  &.is-filled {
    border-color: $color-primary;
    background: #FFF8E1;
  }
}

.error-msg {
  color: $color-danger;
  font-size: $font-size-xs;
  margin-top: 8px;
}
</style>
