<template>
  <div class="profile-page">
    <div class="page-container">
      <h2 class="page-title">个人资料</h2>

      <div class="profile-card">
        <el-form
          ref="formRef"
          :model="form"
          :rules="rules"
          label-position="top"
          size="large"
        >
          <!-- 头像 -->
          <div class="avatar-section">
            <el-avatar :size="80" :src="form.avatar">
              {{ (form.nickname || 'U').charAt(0) }}
            </el-avatar>
            <el-upload
              action="/api/file/upload"
              :headers="uploadHeaders"
              :show-file-list="false"
              :on-success="handleAvatarSuccess"
              accept="image/*"
            >
              <el-button size="small" text type="primary">更换头像</el-button>
            </el-upload>
          </div>

          <el-row :gutter="16">
            <el-col :span="12">
              <el-form-item label="昵称" prop="nickname">
                <el-input v-model="form.nickname" placeholder="你的昵称" />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="手机号">
                <el-input v-model="form.phone" placeholder="手机号" />
              </el-form-item>
            </el-col>
          </el-row>

          <el-form-item label="邮箱">
            <el-input v-model="form.email" placeholder="邮箱" />
          </el-form-item>

          <el-form-item label="个人简介">
            <el-input
              v-model="form.bio"
              type="textarea"
              :rows="3"
              placeholder="介绍一下自己吧..."
              maxlength="200"
              show-word-limit
            />
          </el-form-item>

          <el-form-item label="所在校区">
            <el-input v-model="form.campus" placeholder="如：仙林校区" />
          </el-form-item>

          <el-form-item label="楼栋">
            <el-input v-model="form.building" placeholder="如：2号楼" />
          </el-form-item>

          <el-form-item>
            <el-button type="primary" :loading="saving" @click="handleSave">
              {{ saving ? '保存中...' : '保存修改' }}
            </el-button>
          </el-form-item>
        </el-form>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getProfile, updateProfile } from '@/api/auth'
import { getToken } from '@/utils/auth'
import { useUserStore } from '@/stores/user'

const userStore = useUserStore()
const formRef = ref()
const saving = ref(false)

const uploadHeaders = {
  Authorization: `Bearer ${getToken()}`,
}

const form = reactive({
  nickname: '',
  avatar: '',
  phone: '',
  email: '',
  bio: '',
  campus: '',
  building: '',
})

const rules = {
  nickname: [{ required: true, message: '请输入昵称', trigger: 'blur' }],
}

function handleAvatarSuccess(response: any) {
  if (response.data?.url) {
    form.avatar = response.data.url
  }
}

async function handleSave() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return

  saving.value = true
  try {
    await updateProfile(form)
    ElMessage.success('资料更新成功！')
    userStore.fetchProfile()
  } catch {
    // handled
  } finally {
    saving.value = false
  }
}

onMounted(async () => {
  try {
    const profile = await getProfile()
    Object.assign(form, {
      nickname: profile.nickname || '',
      avatar: profile.avatar || '',
      phone: profile.phone || '',
      email: profile.email || '',
      bio: profile.bio || '',
      campus: profile.campus || '',
      building: profile.building || '',
    })
  } catch {
    // handled
  }
})
</script>

<style scoped lang="scss">
.page-title {
  font-size: $font-size-xl;
  font-weight: 700;
  margin: 0 0 $spacing-md;
}

.profile-card {
  max-width: 640px;
  background: #ffffff;
  border-radius: $radius-lg;
  padding: $spacing-xl;
  box-shadow: $shadow-sm;
  border: 1px solid $color-border;
}

.avatar-section {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12px;
  margin-bottom: $spacing-xl;
}
</style>
