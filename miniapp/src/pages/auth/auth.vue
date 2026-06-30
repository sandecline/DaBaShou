<template>
  <view class="page-auth">
    <view class="logo-section">
      <image class="logo" src="/static/images/logo.png" mode="aspectFit" />
      <text class="slogan">校园技能共享互助平台</text>
    </view>

    <view class="login-section">
      <!-- 微信登录按钮（仅小程序） -->
      <!-- #ifdef MP-WEIXIN -->
      <button
        class="btn-wechat"
        type="primary"
        open-type="getUserInfo"
        @getuserinfo="onWechatLogin"
      >
        <text class="btn-icon">💬</text>
        微信一键登录
      </button>
      <!-- #endif -->

      <view class="divider">
        <text class="divider-text">或</text>
      </view>

      <u--form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-width="0"
      >
        <u--input
          v-model="form.username"
          placeholder="请输入用户名"
          border="surround"
          clearable
          prefixIcon="account"
        />
        <view style="height: 24rpx" />
        <u--input
          v-model="form.password"
          placeholder="请输入密码"
          border="surround"
          clearable
          password
          prefixIcon="lock"
        />
        <view style="height: 48rpx" />
        <u-button type="primary" :loading="loading" @tap="onLogin">
          登录
        </u-button>
      </u--form>

      <view class="register-link">
        <text>还没有账号？</text>
        <text class="link" @tap="goRegister">立即注册</text>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useUserStore } from '@/store/user'

const userStore = useUserStore()
const formRef = ref(null)
const loading = ref(false)

const form = reactive({
  username: '',
  password: '',
})

const rules = {
  username: [{ required: true, message: '请输入用户名' }],
  password: [{ required: true, message: '请输入密码' }],
}

async function onLogin() {
  try {
    loading.value = true
    await userStore.loginWithPassword(form.username, form.password)
    uni.switchTab({ url: '/pages/index/index' })
  } catch (err) {
    console.error('login failed', err)
  } finally {
    loading.value = false
  }
}

async function onWechatLogin(e) {
  if (!e.detail.userInfo) {
    uni.showToast({ title: '已拒绝授权', icon: 'none' })
    return
  }
  try {
    loading.value = true
    await userStore.loginWithWechat()
    uni.switchTab({ url: '/pages/index/index' })
  } catch (err) {
    console.error('wechat login failed', err)
  } finally {
    loading.value = false
  }
}

function goRegister() {
  uni.navigateTo({ url: '/pages/auth/register' })
}
</script>

<style lang="scss" scoped>
.page-auth {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  background: #fff;
}

.logo-section {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding-top: 120rpx;

  .logo {
    width: 160rpx;
    height: 160rpx;
  }

  .slogan {
    margin-top: 24rpx;
    font-size: 28rpx;
    color: #999;
  }
}

.login-section {
  padding: 0 48rpx 100rpx;
}

.btn-wechat {
  width: 100%;
  height: 88rpx;
  line-height: 88rpx;
  background: #07c160;
  border-radius: 44rpx;
  font-size: 32rpx;
  color: #fff;

  .btn-icon {
    margin-right: 8rpx;
  }
}

.divider {
  display: flex;
  align-items: center;
  margin: 48rpx 0;

  &::before,
  &::after {
    content: '';
    flex: 1;
    height: 1px;
    background: #eee;
  }

  .divider-text {
    padding: 0 24rpx;
    color: #999;
    font-size: 26rpx;
  }
}

.register-link {
  text-align: center;
  margin-top: 48rpx;
  font-size: 26rpx;
  color: #999;

  .link {
    color: #2979ff;
    margin-left: 8rpx;
  }
}
</style>
