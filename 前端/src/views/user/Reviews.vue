<template>
  <div class="page-card">
    <h2>我的评价</h2>
    <el-empty v-if="reviews.length === 0" description="暂无评价" />
    <div v-for="item in reviews" :key="item.id" class="review">
      <el-rate :model-value="item.rating" disabled />
      <p>{{ item.content }}</p>
      <span>{{ item.createTime }}</span>
    </div>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { getMyReviewsApi } from '../../api/mall'
const reviews = ref([])
onMounted(async () => { reviews.value = await getMyReviewsApi() })
</script>

<style scoped>
.review { padding: 14px 0; border-bottom: 1px solid #eef2f7; }
.review p { margin: 8px 0; }
.review span { color: #64748b; font-size: 13px; }
</style>
