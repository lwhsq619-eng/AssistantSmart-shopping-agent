<template>
  <div class="orders-page">
    <el-tabs v-model="status" @tab-change="load">
      <el-tab-pane v-for="s in statuses" :key="s" :label="s" :name="s" />
    </el-tabs>
    <el-empty v-if="orders.length === 0" description="暂无订单" />
    <article v-for="entry in orders" :key="entry.order.id" class="order-card">
      <header>
        <b>订单编号：{{ entry.order.orderNo }}</b>
        <el-tag>{{ entry.order.status }}</el-tag>
      </header>
      <div v-for="item in entry.items" :key="item.id" class="order-item">
        <img :src="item.productImage" />
        <div><b>{{ item.productName }}</b><p>{{ item.specInfo }} × {{ item.quantity }}</p></div>
        <span>￥{{ Number(item.subtotal).toFixed(2) }}</span>
      </div>
      <footer>
        <b>合计 ￥{{ Number(entry.order.payAmount || entry.order.totalAmount).toFixed(2) }}</b>
        <el-button v-if="entry.order.status === '待支付'" type="warning" @click="$router.push(`/user/payment/${entry.order.orderNo}`)">立即支付</el-button>
        <el-button v-if="entry.order.status === '待支付'" @click="cancel(entry.order.id)">取消订单</el-button>
        <el-button v-if="['待发货','待收货'].includes(entry.order.status)" type="primary" @click="confirm(entry.order.id)">确认收货</el-button>
        <el-button v-if="entry.order.status === '已完成'" type="success" @click="$router.push(`/user/order/${entry.order.id}`)">去评价</el-button>
        <el-button @click="$router.push(`/user/order/${entry.order.id}`)">查看详情</el-button>
      </footer>
    </article>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { cancelOrderApi, confirmReceiptApi, getMyOrdersApi } from '../../api/mall'

const statuses = ['全部订单', '待支付', '待发货', '待收货', '已完成', '已取消']
const status = ref('全部订单')
const orders = ref([])

async function load() {
  orders.value = await getMyOrdersApi({ status: status.value })
}
async function cancel(id) { await cancelOrderApi(id); ElMessage.success('订单已取消'); await load() }
async function confirm(id) { await confirmReceiptApi(id); ElMessage.success('已确认收货'); await load() }
onMounted(load)
</script>

<style scoped>
.orders-page { display: flex; flex-direction: column; gap: 14px; }
.order-card { padding: 18px; border-radius: 12px; background: #fff; box-shadow: 0 12px 28px rgba(15,23,42,.06); }
.order-card header, .order-card footer { display: flex; justify-content: space-between; align-items: center; gap: 10px; }
.order-item { display: grid; grid-template-columns: 72px 1fr 120px; gap: 14px; align-items: center; padding: 14px 0; border-top: 1px solid #eef2f7; }
.order-item img { width: 72px; height: 72px; border-radius: 8px; object-fit: cover; }
.order-item p { margin: 6px 0 0; color: #64748b; }
footer b { margin-right: auto; color: #f59e0b; }
</style>
