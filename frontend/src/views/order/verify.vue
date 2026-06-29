<template>
  <div class="order-verify-page">
    <div class="page-container">
      <div class="verify-card">
        <!-- 如果是买家：输入核销码 -->
        <template v-if="isBuyer">
          <div class="verify-header">
            <el-icon :size="48" class="verify-icon"><Checked /></el-icon>
            <h2>确认核销</h2>
            <p>请向卖家获取核销码，输入后确认服务完成</p>
          </div>

          <VerifyCode
            mode="input"
            :error-msg="errorMsg"
            @complete="handleVerify"
          />
        </template>

        <!-- 如果是卖家：展示核销码 -->
        <template v-else>
          <div class="verify-header">
            <el-icon :size="48" class="verify-icon"><Present /></el-icon>
            <h2>核销码</h2>
            <p>请将核销码出示给买家，完成服务确认</p>
          </div>

          <VerifyCode
            v-if="order?.verifyCode"
            mode="display"
            :code="order!.verifyCode!"
            :expire-at="order!.verifyCodeExpire"
          />

          <div v-else class="verify-expired">
            <el-result icon="warning" title="核销码已过期" sub-title="核销码有效期为30分钟，已自动失效" />
          </div>
        </template>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getOrderDetail, verifyOrder, confirmComplete } from '@/api/order'
import { useUserStore } from '@/stores/user'
import VerifyCode from '@/components/common/VerifyCode.vue'
import type { Order } from '@/types'

const props = defineProps<{ id: string }>()
const router = useRouter()
const userStore = useUserStore()

const order = ref<Order | null>(null)
const isBuyer = ref(false)
const errorMsg = ref('')

async function fetchOrder() {
  try {
    order.value = await getOrderDetail(Number(props.id))
    isBuyer.value = userStore.user?.id === order.value.buyerId
  } catch {
    order.value = null
  }
}

async function handleVerify(code: string) {
  errorMsg.value = ''
  try {
    if (order.value?.status === 4) {
      // 待确认 -> 确认完成
      await confirmComplete(Number(props.id))
      ElMessage.success('确认完成！积分已结算 🎉')
    } else {
      await verifyOrder({ orderId: Number(props.id), verifyCode: code })
      ElMessage.success('核销成功！积分已结算 🎉')
    }
    router.push('/order')
  } catch (err: any) {
    errorMsg.value = err?.message || '核销失败，请检查核销码'
  }
}

onMounted(fetchOrder)
</script>

<style scoped lang="scss">
.verify-card {
  max-width: 480px;
  margin: 40px auto;
  background: #ffffff;
  border-radius: $radius-lg;
  padding: $spacing-2xl;
  box-shadow: $shadow-md;
  border: 1px solid $color-border;
}

.verify-header {
  text-align: center;
  margin-bottom: $spacing-xl;

  .verify-icon {
    color: $color-primary;
    margin-bottom: $spacing-md;
  }

  h2 {
    margin: 0 0 8px;
    font-size: $font-size-xl;
    font-weight: 700;
  }

  p {
    color: $color-text-secondary;
    font-size: $font-size-sm;
  }
}

.verify-expired {
  padding: $spacing-xl 0;
}
</style>
