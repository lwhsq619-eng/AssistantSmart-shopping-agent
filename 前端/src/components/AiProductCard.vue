<template>
  <article class="ai-product-card">
    <el-image class="cover" :src="image" fit="cover" lazy>
      <template #error>
        <div class="image-fallback">商品图</div>
      </template>
    </el-image>
    <div class="body">
      <h4>{{ product.productName || product.name || '商品' }}</h4>
      <p class="reason">{{ product.reason || product.matchReason || product.match_reason || '基于当前需求推荐。' }}</p>
      <div class="meta">
        <strong>￥{{ price }}</strong>
        <span>库存 {{ product.stock ?? '-' }}</span>
      </div>
      <div class="actions">
        <el-button size="small" type="primary" plain @click.stop="viewDetail">查看详情</el-button>
        <el-button size="small" type="warning" @click.stop="addCart">加入购物车</el-button>
      </div>
    </div>
  </article>
</template>

<script setup>
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { addCartApi } from '../api/mall'
import { defaultProductImage } from '../utils/images'

const props = defineProps({
  product: {
    type: Object,
    required: true
  }
})

const router = useRouter()
const image = computed(() => props.product.imageUrl || props.product.productImage || props.product.image || defaultProductImage)
const price = computed(() => props.product.price || props.product.discountPrice || props.product.unitPrice || '0.00')
const productId = computed(() => props.product.productId || props.product.id)

function viewDetail() {
  if (!productId.value) return
  router.push(`/user/product/${productId.value}`)
}

async function addCart() {
  if (!productId.value) {
    ElMessage.warning('商品编号为空，无法加入购物车')
    return
  }
  await addCartApi({ productId: productId.value, specInfo: '默认规格', quantity: 1 })
  ElMessage.success('已加入购物车')
}
</script>

<style scoped>
.ai-product-card {
  display: grid;
  grid-template-columns: 92px 1fr;
  gap: 14px;
  padding: 12px;
  background: #fff;
  border: 1px solid #e5e7eb;
  border-radius: 12px;
  box-shadow: 0 10px 24px rgba(15, 23, 42, 0.06);
}

.cover,
.image-fallback {
  width: 92px;
  height: 92px;
  border-radius: 10px;
  background: #f1f5f9;
}

.image-fallback {
  display: flex;
  align-items: center;
  justify-content: center;
  color: #94a3b8;
  font-size: 13px;
}

h4 {
  margin: 0 0 6px;
  color: #111827;
}

.reason {
  margin: 0 0 8px;
  color: #64748b;
  line-height: 1.5;
}

.meta {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
  color: #64748b;
}

.meta strong {
  color: #f97316;
  font-size: 18px;
}

.actions {
  display: flex;
  gap: 8px;
}
</style>
