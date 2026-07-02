<template>
  <div class="login-page">
    <section class="login-visual">
      <router-link to="/" class="brand">
        <span class="brand-mark">搭</span>
        <span>搭把手</span>
      </router-link>

      <div class="visual-copy">
        <span class="eyebrow">校园互助服务台</span>
        <h1>登录后，继续管理你的技能、订单和积分</h1>
        <p>把可信的同学互助沉淀成可追踪、可评价、可结算的服务流程。</p>
      </div>

      <div class="feature-list">
        <div class="feature-item">
          <el-icon><Connection /></el-icon>
          <span>技能与求助智能匹配</span>
        </div>
        <div class="feature-item">
          <el-icon><Wallet /></el-icon>
          <span>积分担保与流水记录</span>
        </div>
        <div class="feature-item">
          <el-icon><Medal /></el-icon>
          <span>信任分沉淀靠谱关系</span>
        </div>
      </div>
    </section>

    <section class="login-card">
      <div class="login-header">
        <h2>欢迎回来</h2>
        <p>使用账号登录搭把手</p>
      </div>

      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-position="top"
        size="large"
        @submit.prevent="handleLogin"
      >
        <el-form-item label="用户名" prop="username">
          <el-input v-model="form.username" placeholder="请输入用户名或手机号" prefix-icon="User" />
        </el-form-item>

        <el-form-item label="密码" prop="password">
          <el-input
            v-model="form.password"
            type="password"
            placeholder="请输入密码"
            prefix-icon="Lock"
            show-password
            @keyup.enter="handleLogin"
          />
        </el-form-item>

        <el-form-item>
          <el-button
            type="primary"
            native-type="submit"
            :loading="loading"
            class="login-btn"
          >
            {{ loading ? '登录中...' : '登录' }}
          </el-button>
        </el-form-item>
      </el-form>

      <div class="login-footer">
        <span>还没有账号？</span>
        <router-link to="/register">立即注册</router-link>
      </div>
    </section>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

const formRef = ref()
const loading = ref(false)

const form = reactive({
  username: '',
  password: '',
})

const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
}

async function handleLogin() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return

  loading.value = true
  try {
    await userStore.login(form.username, form.password)
    ElMessage.success('登录成功，欢迎回来')
    const redirect = (route.query.redirect as string) || '/'
    router.push(redirect)
  } catch {
    // Request interceptor displays the error message.
  } finally {
    loading.value = false
  }
}
</script>

<style scoped lang="scss">
.login-page {
  min-height: 100vh;
  display: grid;
  grid-template-columns: minmax(0, 1.05fr) minmax(360px, 0.95fr);
  background:
    radial-gradient(circle at 16% 8%, rgba(35, 100, 232, 0.16), transparent 28rem),
    linear-gradient(135deg, #f8fbff 0%, #eef4ff 45%, #ffffff 100%);
}

.login-visual {
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  padding: 48px;
}

.brand {
  display: inline-flex;
  align-items: center;
  gap: 10px;
  width: fit-content;
  color: $color-text-primary;
  font-size: 18px;
  font-weight: 850;
}

.brand-mark {
  display: grid;
  place-items: center;
  width: 36px;
  height: 36px;
  border-radius: 10px;
  color: #ffffff;
  background: linear-gradient(135deg, $color-primary, $color-primary-dark);
  box-shadow: 0 12px 28px rgba(35, 100, 232, 0.22);
}

.visual-copy {
  max-width: 650px;

  .eyebrow {
    display: inline-flex;
    margin-bottom: 16px;
    padding: 6px 10px;
    border-radius: $radius-round;
    background: rgba(35, 100, 232, 0.1);
    color: $color-primary;
    font-size: 12px;
    font-weight: 850;
  }

  h1 {
    margin: 0;
    color: $color-text-primary;
    font-size: clamp(38px, 5vw, 64px);
    line-height: 1.02;
    letter-spacing: 0;
    font-weight: 880;
  }

  p {
    max-width: 520px;
    margin-top: 18px;
    color: $color-text-secondary;
    font-size: 16px;
  }
}

.feature-list {
  display: grid;
  gap: 12px;
  max-width: 520px;
}

.feature-item {
  display: flex;
  align-items: center;
  gap: 10px;
  width: fit-content;
  min-height: 42px;
  padding: 0 14px;
  border: 1px solid rgba(203, 213, 225, 0.8);
  border-radius: $radius-round;
  background: rgba(255, 255, 255, 0.72);
  color: $color-text-regular;
  font-weight: 750;

  .el-icon {
    color: $color-primary;
  }
}

.login-card {
  align-self: center;
  justify-self: center;
  width: min(100% - 48px, 440px);
  padding: 36px;
  border: 1px solid rgba(203, 213, 225, 0.78);
  border-radius: $radius-xl;
  background: rgba(255, 255, 255, 0.9);
  box-shadow: $shadow-xl;
  backdrop-filter: blur(18px);
}

.login-header {
  margin-bottom: 28px;

  h2 {
    margin: 0;
    color: $color-text-primary;
    font-size: 28px;
    font-weight: 850;
  }

  p {
    margin-top: 6px;
    color: $color-text-secondary;
  }
}

.login-btn {
  width: 100%;
  height: 44px;
}

.login-footer {
  display: flex;
  justify-content: center;
  gap: 6px;
  margin-top: 12px;
  color: $color-text-secondary;
  font-size: 14px;

  a {
    color: $color-primary;
    font-weight: 750;
  }
}

@media (max-width: 860px) {
  .login-page {
    grid-template-columns: 1fr;
  }

  .login-visual {
    padding: 28px 24px 0;
  }

  .visual-copy {
    margin-top: 44px;

    h1 {
      font-size: 38px;
    }
  }

  .feature-list {
    display: none;
  }

  .login-card {
    width: min(100% - 32px, 440px);
    margin: 28px 0 36px;
    padding: 28px;
  }
}
</style>
