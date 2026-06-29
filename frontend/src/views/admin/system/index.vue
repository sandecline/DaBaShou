<template>
  <div class="admin-page">
    <div class="admin-layout">
      <Sidebar :menu-items="adminMenu" />
      <div class="admin-content">
        <div class="page-container">
          <h2>系统配置</h2>

          <div class="config-card">
            <el-form :model="config" label-position="left" label-width="180px">
              <h3>积分规则</h3>
              <el-form-item label="注册赠送积分">
                <el-input-number v-model="config.registerBonus" :min="0" :max="10000" />
              </el-form-item>
              <el-form-item label="好评加分">
                <el-input-number v-model="config.goodReviewBonus" :min="0" :max="100" />
              </el-form-item>
              <el-form-item label="签到活跃奖励">
                <el-input-number v-model="config.checkinBonus" :min="0" :max="100" />
              </el-form-item>

              <el-divider />
              <h3>信任分阈值</h3>
              <el-form-item label="新人上限">
                <el-input-number v-model="config.newcomerMax" :min="0" :max="5" :precision="1" :step="0.1" />
              </el-form-item>
              <el-form-item label="靠谱上限">
                <el-input-number v-model="config.reliableMax" :min="0" :max="5" :precision="1" :step="0.1" />
              </el-form-item>

              <el-divider />
              <h3>超时熔断</h3>
              <el-form-item label="待支付超时(分钟)">
                <el-input-number v-model="config.payTimeout" :min="1" :max="120" />
              </el-form-item>
              <el-form-item label="核销码有效期(分钟)">
                <el-input-number v-model="config.verifyCodeTimeout" :min="1" :max="120" />
              </el-form-item>
              <el-form-item label="服务确认超时(天)">
                <el-input-number v-model="config.confirmTimeout" :min="1" :max="30" />
              </el-form-item>

              <el-divider />
              <h3>退改扣分比例</h3>
              <el-form-item label="买家取消扣除比例(%)">
                <el-input-number v-model="config.buyerCancelPenalty" :min="0" :max="100" />
              </el-form-item>
              <el-form-item label="卖家取消扣除比例(%)">
                <el-input-number v-model="config.sellerCancelPenalty" :min="0" :max="100" />
              </el-form-item>

              <el-form-item>
                <el-button type="primary" :loading="saving" @click="saveConfig">
                  {{ saving ? '保存中...' : '保存配置' }}
                </el-button>
              </el-form-item>
            </el-form>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getSystemConfig, updateSystemConfig } from '@/api/admin'
import Sidebar from '@/components/layout/Sidebar.vue'

const adminMenu = [
  { path: '/admin/users', title: '用户管理', icon: 'User' },
  { path: '/admin/orders', title: '订单管理', icon: 'Document' },
  { path: '/admin/credit', title: '信用管理', icon: 'Warning' },
  { path: '/admin/system', title: '系统配置', icon: 'Setting' },
  { path: '/admin/stat', title: '数据统计', icon: 'DataAnalysis' },
]

const saving = ref(false)
const config = reactive({
  registerBonus: 100,
  goodReviewBonus: 10,
  checkinBonus: 5,
  newcomerMax: 2.9,
  reliableMax: 3.9,
  payTimeout: 15,
  verifyCodeTimeout: 30,
  confirmTimeout: 7,
  buyerCancelPenalty: 10,
  sellerCancelPenalty: 20,
})

async function loadConfig() {
  try {
    const result = await getSystemConfig()
    if (result) Object.assign(config, result)
  } catch { /* handled */ }
}

async function saveConfig() {
  saving.value = true
  try {
    await updateSystemConfig({ ...config })
    ElMessage.success('系统配置已保存')
  } catch { /* handled */ } finally { saving.value = false }
}

onMounted(loadConfig)
</script>

<style scoped lang="scss">
.admin-layout {
  display: flex;
  min-height: calc(100vh - #{$header-offset});
}

.admin-content {
  flex: 1;
  overflow-x: auto;
}

h2 {
  margin: 0 0 $spacing-md;
  font-size: $font-size-xl;
  font-weight: 700;
}

.config-card {
  max-width: 700px;
  background: #ffffff;
  border-radius: $radius-lg;
  padding: $spacing-xl;
  border: 1px solid $color-border;

  h3 {
    font-size: $font-size-base;
    font-weight: 600;
    margin: 0 0 $spacing-md;
    color: $color-primary;
  }
}
</style>
