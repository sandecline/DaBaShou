<template>
  <div class="register-page flex-center">
    <div class="register-card">
      <div class="register-header">
        <span class="register-logo">🤝</span>
        <h1>注册搭把手</h1>
        <p>加入校园互助大家庭</p>
      </div>

      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-position="top"
        size="large"
        @submit.prevent="handleRegister"
      >
        <el-form-item label="用户名" prop="username">
          <el-input v-model="form.username" placeholder="4-20位字母或数字" prefix-icon="User" />
        </el-form-item>

        <el-form-item label="昵称" prop="nickname">
          <el-input v-model="form.nickname" placeholder="给自己取个昵称吧" prefix-icon="Edit" />
        </el-form-item>

        <el-form-item label="密码" prop="password">
          <el-input
            v-model="form.password"
            type="password"
            placeholder="至少6位密码"
            prefix-icon="Lock"
            show-password
          />
        </el-form-item>

        <el-form-item label="确认密码" prop="confirmPassword">
          <el-input
            v-model="form.confirmPassword"
            type="password"
            placeholder="再次输入密码"
            prefix-icon="Lock"
            show-password
          />
        </el-form-item>

        <el-form-item label="手机号（选填）" prop="phone">
          <el-input v-model="form.phone" placeholder="方便接收通知" prefix-icon="Phone" />
        </el-form-item>

        <el-form-item>
          <el-button
            type="primary"
            native-type="submit"
            :loading="loading"
            class="register-btn"
            round
          >
            {{ loading ? '注册中...' : '注册' }}
          </el-button>
        </el-form-item>
      </el-form>

      <div class="register-footer">
        <span>已有账号？</span>
        <router-link to="/login">立即登录</router-link>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { register } from '@/api/user'

const router = useRouter()
const formRef = ref()
const loading = ref(false)

const form = reactive({
  username: '',
  nickname: '',
  password: '',
  confirmPassword: '',
  phone: '',
})

const validateConfirmPassword = (_rule: any, value: string, callback: any) => {
  if (value !== form.password) {
    callback(new Error('两次输入的密码不一致'))
  } else {
    callback()
  }
}

const rules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 4, max: 20, message: '用户名4-20位', trigger: 'blur' },
    { pattern: /^[a-zA-Z0-9_]+$/, message: '用户名仅支持字母、数字和下划线', trigger: 'blur' },
  ],
  nickname: [{ required: true, message: '请输入昵称', trigger: 'blur' }],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码至少6位', trigger: 'blur' },
  ],
  confirmPassword: [
    { required: true, message: '请确认密码', trigger: 'blur' },
    { validator: validateConfirmPassword, trigger: 'blur' },
  ],
  phone: [
    { pattern: /^1[3-9]\d{9}$/, message: '手机号格式不正确', trigger: 'blur' },
  ],
}

async function handleRegister() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return

  loading.value = true
  try {
    await register({
      username: form.username,
      password: form.password,
      nickname: form.nickname,
      phone: form.phone || undefined,
    })
    ElMessage.success('注册成功！欢迎加入搭把手 🎉')
    router.push('/login')
  } catch {
    // err handled by request interceptor
  } finally {
    loading.value = false
  }
}
</script>

<style scoped lang="scss">
.register-page {
  min-height: 100vh;
  background: linear-gradient(180deg, #FFF8E1 0%, #F5F5F5 60%);
  padding: $spacing-md;
}

.register-card {
  width: 100%;
  max-width: 420px;
  background: #FFFFFF;
  border-radius: $radius-xl;
  padding: $spacing-xl $spacing-2xl;
  box-shadow: $shadow-xl;
  margin: 20px 0;
}

.register-header {
  text-align: center;
  margin-bottom: $spacing-lg;

  .register-logo {
    font-size: 40px;
    display: block;
  }

  h1 {
    margin: 8px 0 4px;
    font-size: $font-size-xl;
    font-weight: 700;
    color: $color-text-primary;
  }

  p {
    margin: 0;
    font-size: $font-size-sm;
    color: $color-text-secondary;
  }
}

.register-btn {
  width: 100%;
}

.register-footer {
  text-align: center;
  font-size: $font-size-sm;
  color: $color-text-secondary;

  a {
    color: $color-accent;
    font-weight: 500;
  }
}
</style>
