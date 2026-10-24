<template>
  <article class="mall-product-card product-card" @click="$router.push(`/user/product/${product.id}`)">
    <div class="image-box">
      <img
        class="product-card-image"
        :src="product.imageUrl || defaultProductImage"
        :alt="product.name"
        loading="lazy"
        @error="imageError"
      />
      <el-tag v-if="product.isSpecial === 1" class="tag" type="danger">特价</el-tag>
      <el-tag v-else-if="product.isNew === 1" class="tag" type="success">新品</el-tag>
    </div>
    <div class="body product-card-body">
      <h3 class="product-card-title">{{ product.name }}</h3>
      <div class="meta">
        <el-rate :model-value="Number(product.rating || 5)" disabled size="small" />
        <span>销量 {{ product.sales || 0 }}</span>
      </div>
      <p class="desc product-card-description">{{ product.description }}</p>
      <div class="price-row product-card-footer product-card-actions">
        <div>
          <span class="price">¥{{ money(product.discountPrice || product.price) }}</span>
          <span v-if="product.marketPrice" class="origin">¥{{ money(product.marketPrice) }}</span>
        </div>
        <el-button type="primary" size="small" @click.stop="$emit('add-cart', product)">加入购物车</el-button>
      </div>
    </div>
  </article>
</template>

<script setup>
import { defaultProductImage, imageError } from '../utils/images'

defineProps({
  product: {
    type: Object,
    required: true
  }
})

defineEmits(['add-cart'])

function money(value) {
  return Number(value || 0).toFixed(2)
}
</script>

<style scoped>
.mall-product-card {
  overflow: hidden;
  border-radius: 12px;
  background: #fff;
  box-shadow: 0 12px 28px rgba(15, 23, 42, 0.08);
  cursor: pointer;
  transition: transform 0.2s ease, box-shadow 0.2s ease;
}

.product-card {
  display: flex;
  flex-direction: column;
  height: 100%;
}

.mall-product-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 20px 40px rgba(15, 23, 42, 0.12);
}

.image-box {
  position: relative;
  overflow: hidden;
  background: #f5f7fa;
}

.product-card-image {
  width: 100%;
  aspect-ratio: 4 / 3;
  object-fit: cover;
  display: block;
  transition: transform 0.25s ease;
}

.mall-product-card:hover img {
  transform: scale(1.04);
}

.tag {
  position: absolute;
  left: 10px;
  top: 10px;
}

.body {
  padding: 14px;
}

.product-card-body {
  display: flex;
  flex: 1;
  flex-direction: column;
}

.product-card-title {
  display: -webkit-box;
  min-height: 44px;
  margin: 0 0 8px;
  overflow: hidden;
  -webkit-box-orient: vertical;
  -webkit-line-clamp: 2;
  font-size: 16px;
  line-height: 1.4;
}

.meta {
  display: flex;
  align-items: center;
  gap: 8px;
  color: #64748b;
  font-size: 12px;
}

.product-card-description {
  display: -webkit-box;
  min-height: 42px;
  margin: 8px 0 12px;
  color: #64748b;
  overflow: hidden;
  -webkit-box-orient: vertical;
  -webkit-line-clamp: 2;
  line-height: 1.45;
}

.price-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 8px;
}

.product-card-actions {
  margin-top: auto;
}

.price {
  color: #f59e0b;
  font-size: 20px;
  font-weight: 900;
}

.origin {
  margin-left: 6px;
  color: #94a3b8;
  text-decoration: line-through;
  font-size: 12px;
}

@media (prefers-reduced-motion: reduce) {
  .mall-product-card,
  .image-box img {
    transition: none;
  }
  .mall-product-card:hover,
  .mall-product-card:hover img {
    transform: none;
  }
}
</style>
