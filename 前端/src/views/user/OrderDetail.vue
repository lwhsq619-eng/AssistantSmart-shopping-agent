<template>
  <div v-if="order" class="page-card detail">
    <h2>订单详情</h2>
    <p>订单编号：{{ order.orderNo }}　状态：<el-tag>{{ order.status }}</el-tag></p>
    <p>收货地址：{{ order.shippingAddress }}</p>
    <el-table :data="items">
      <el-table-column label="商品">
        <template #default="{ row }"><div class="product-line"><img :src="row.productImage" /><span>{{ row.productName }}</span></div></template>
      </el-table-column>
      <el-table-column prop="specInfo" label="规格" />
      <el-table-column prop="quantity" label="数量" />
      <el-table-column label="操作">
        <template #default="{ row }">
          <el-button v-if="order.status === '已完成' && row.reviewed !== 1" type="success" @click="openReview(row)">评价</el-button>
          <el-tag v-else-if="row.reviewed === 1" type="success">已评价</el-tag>
        </template>
      </el-table-column>
    </el-table>
    <el-dialog v-model="dialog" title="商品评价" width="520px">
      <el-rate v-model="review.rating" />
      <el-input v-model="review.content" type="textarea" :rows="4" placeholder="请输入评价内容" />
      <el-checkbox v-model="review.anonymous" :true-label="1" :false-label="0">匿名评价</el-checkbox>
      <template #footer><el-button @click="dialog=false">取消</el-button><el-button type="primary" @click="submitReview">提交评价</el-button></template>
    </el-dialog>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { createReviewApi, getOrderDetailApi } from '../../api/mall'

const route = useRoute()
const order = ref(null)
const items = ref([])
const dialog = ref(false)
const review = reactive({ orderItemId: null, rating: 5, content: '', anonymous: 0 })
async function load() { const data = await getOrderDetailApi(route.params.id); order.value = data.order; items.value = data.items }
function openReview(row) { review.orderItemId = row.id; review.rating = 5; review.content = ''; dialog.value = true }
async function submitReview() { await createReviewApi(review); ElMessage.success('评价成功'); dialog.value = false; await load() }
onMounted(load)
</script>

<style scoped>
.detail { display: flex; flex-direction: column; gap: 14px; }
.product-line { display: flex; align-items: center; gap: 12px; }
.product-line img { width: 58px; height: 58px; object-fit: cover; border-radius: 8px; }
</style>
