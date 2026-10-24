<template>
  <article class="ai-product-card">
    <el-image class="cover" :src="image" fit="cover" lazy>
      <template #error>
        <div class="image-fallback">商品图</div>
      </template>
    </el-image>
    <div class="body">
      <div class="title-row">
        <h4>{{ name }}</h4>
        <el-tag v-if="stock <= 0" type="danger" size="small">缺货</el-tag>
      </div>
      <p class="reason">{{ reason }}</p>
      <div class="meta">
        <strong>￥{{ price }}</strong>
        <span>库存 {{ stockText }}</span>
        <span v-if="salesText">销量 {{ salesText }}</span>
      </div>
      <el-progress v-if="score !== null" :percentage="score" :stroke-width="6" :show-text="false" />
      <div v-if="score !== null" class="score">匹配分 {{ score }}</div>
      <div class="actions">
        <el-button size="small" type="primary" plain @click.stop="viewDetail">查看详情</el-button>
        <el-button size="small" type="warning" :disabled="stock <= 0" @click.stop="addCart">加入购物车</el-button>
      </div>
    </div>
  </article>
</template>

<script setup>
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { addCartApi } from '../../api/mall'
import { defaultProductImage } from '../../utils/images'

const props = defineProps({
  product: { type: Object, required: true }
})

const router = useRouter()
const productId = computed(() => props.product.productId || props.product.id)
const name = computed(() => props.product.productName || props.product.name || '商品')
const image = computed(() => props.product.imageUrl || props.product.productImage || props.product.image || defaultProductImage)
const price = computed(() => Number(props.product.price || props.product.discountPrice || props.product.unitPrice || 0).toFixed(2))
const stock = computed(() => Number(props.product.stock ?? 0))
const stockText = computed(() => props.product.stock ?? '-')
const salesText = computed(() => props.product.sales ?? props.product.salesCount ?? '')
const reason = computed(() => props.product.reason || props.product.matchReason || props.product.recommendReason || '基于当前需求推荐。')
const score = computed(() => {
  const raw = props.product.matchScore ?? props.product.score
  if (raw === undefined || raw === null || raw === '') return null
  return Math.max(0, Math.min(100, Number(raw)))
})

function viewDetail() {
  if (!productId.value) return
  router.push(`/user/product/${productId.value}`)
}

async function addCart() {
  if (!productId.value) return ElMessage.warning('商品编号为空，无法加入购物车')
  await addCartApi({ productId: productId.value, specInfo: '默认规格', quantity: 1 })
  ElMessage.success('已加入购物车')
}
</script>

<style scoped>
.ai-product-card {
  min-height: 100%;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  border: 1px solid #e5e7eb;
  border-radius: 12px;
  background: #fff;
  box-shadow: 0 4px 16px rgba(15, 23, 42, 0.06);
  transition: transform .18s ease, box-shadow .18s ease;
}

.ai-product-card:hover {
  transform: translateY(-3px);
  box-shadow: 0 12px 28px rgba(15, 23, 42, 0.1);
}

.cover,
.image-fallback {
  width: 100%;
  height: 176px;
  background: #f1f5f9;
}

.image-fallback {
  display: grid;
  place-items: center;
  color: #94a3b8;
}

.body {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 10px;
  padding: 14px;
}

.title-row {
  display: flex;
  gap: 8px;
  align-items: flex-start;
  justify-content: space-between;
}

h4 {
  margin: 0;
  color: #111827;
  line-height: 1.45;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.reason {
  min-height: 62px;
  margin: 0;
  color: #64748b;
  line-height: 1.55;
  display: -webkit-box;
  -webkit-line-clamp: 3;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.meta {
  display: flex;
  flex-wrap: wrap;
  gap: 8px 12px;
  align-items: center;
  color: #64748b;
  font-size: 13px;
}

.meta strong {
  color: #f97316;
  font-size: 20px;
}

.score {
  color: #2563eb;
  font-size: 13px;
}

.actions {
  margin-top: auto;
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 8px;
}
</style>
