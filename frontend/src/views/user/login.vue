<template>
  <div class="login-page flex-center">
    <div class="login-card">
      <div class="login-header">
        <span class="login-logo">🤝</span>
        <h1>登录搭把手</h1>
        <p>校园共享技能互助平台</p>
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
            round
          >
            {{ loading ? '登录中...' : '登录' }}
          </el-button>
        </el-form-item>
      </el-form>

      <div class="login-footer">
        <span>还没有账号？</span>
        <router-link to="/register">立即注册</router-link>
      </div>
    </div>
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
    ElMessage.success('登录成功！欢迎回来 🎉')
    const redirect = (route.query.redirect as string) || '/'
    router.push(redirect)
  } catch {
    // err handled by request interceptor
  } finally {
    loading.value = false
  }
}
</script>

<style scoped lang="scss">
.login-page {
  min-height: 100vh;
  background: linear-gradient(180deg, #FFF8E1 0%, #F5F5F5 60%);
  padding: $spacing-md;
}

.login-card {
  width: 100%;
  max-width: 400px;
  background: #FFFFFF;
  border-radius: $radius-xl;
  padding: $spacing-2xl;
  box-shadow: $shadow-xl;
}

.login-header {
  text-align: center;
  margin-bottom: $spacing-xl;

  .login-logo {
    font-size: 48px;
    display: block;
  }

  h1 {
    margin: 12px 0 4px;
    font-size: $font-size-2xl;
    font-weight: 700;
    color: $color-text-primary;
  }

  p {
    margin: 0;
    font-size: $font-size-sm;
    color: $color-text-secondary;
  }
}

.login-btn {
  width: 100%;
}

.login-footer {
  text-align: center;
  font-size: $font-size-sm;
  color: $color-text-secondary;

  a {
    color: $color-accent;
    font-weight: 500;
  }
}
</style>
