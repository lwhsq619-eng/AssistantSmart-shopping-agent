<template>
  <div class="page-card payment">
    <h2>模拟支付</h2>
    <el-result icon="info" title="订单待支付" :sub-title="order?.orderNo">
      <template #extra>
        <div class="amount">￥{{ Number(order?.payAmount || order?.totalAmount || 0).toFixed(2) }}</div>
        <p>支付倒计时：{{ seconds }} 秒</p>
        <el-radio-group v-model="method">
          <el-radio-button label="支付宝模拟支付" />
          <el-radio-button label="微信模拟支付" />
          <el-radio-button label="银行卡模拟支付" />
          <el-radio-button label="余额模拟支付" />
        </el-radio-group>
        <div class="actions">
          <el-button type="primary" @click="pay">确认支付</el-button>
          <el-button @click="$router.push('/user/orders')">取消支付</el-button>
        </div>
      </template>
    </el-result>
  </div>
</template>

<script setup>
import { onMounted, onUnmounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getPaymentApi, payOrderApi } from '../../api/mall'

const route = useRoute()
const router = useRouter()
const order = ref(null)
const method = ref('余额模拟支付')
const seconds = ref(900)
let timer

async function load() {
  const data = await getPaymentApi(route.params.orderNo)
  order.value = data.order
}
async function pay() {
  await payOrderApi(route.params.orderNo, method.value)
  ElMessage.success('模拟支付成功')
  router.replace('/user/orders')
}
onMounted(() => { load(); timer = setInterval(() => seconds.value--, 1000) })
onUnmounted(() => clearInterval(timer))
</script>

<style scoped>
.payment { min-height: 520px; display: grid; place-items: center; }
.amount { color: #f59e0b; font-size: 34px; font-weight: 900; margin-bottom: 14px; }
.actions { margin-top: 20px; display: flex; gap: 12px; justify-content: center; }
</style>
