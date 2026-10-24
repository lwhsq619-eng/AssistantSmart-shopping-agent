<template>
  <div v-if="product" class="detail-page">
    <section class="detail-card">
      <div class="gallery">
        <el-image class="product-main-image main-image" :src="activeImage" :preview-src-list="imageList" fit="cover" />
        <div class="thumbs">
          <img
            v-for="img in imageList"
            :key="img"
            class="product-thumbnail"
            :class="{ active: activeImage === img }"
            :src="img"
            :alt="product.name"
            @error="imageError"
            @click="activeImage = img"
          />
        </div>
      </div>

      <div class="info">
        <el-tag v-if="product.isSpecial === 1" type="danger">今日特价</el-tag>
        <h1>{{ product.name }}</h1>
        <p>{{ product.description }}</p>
        <div class="brand">品牌：{{ product.brandName || brand?.name || '未设置' }}</div>
        <div class="rating">
          <el-rate :model-value="Number(product.rating || 5)" disabled />
          <span>{{ product.reviewCount || 0 }} 条评价 · 销量 {{ product.sales || 0 }}</span>
        </div>
        <div class="price-box">
          <span class="price">¥{{ money(product.discountPrice || product.price) }}</span>
          <span v-if="product.marketPrice" class="origin">¥{{ money(product.marketPrice) }}</span>
        </div>
        <div class="stock">库存：{{ product.stock }}</div>
        <div class="sku-box">
          <b>规格选择</b>
          <el-radio-group v-model="selectedSpec">
            <el-radio-button v-for="sku in skus" :key="sku.id" :label="sku.specInfo" />
          </el-radio-group>
        </div>
        <el-input-number v-model="quantity" :min="1" :max="Math.max(1, Number(product.stock || 1))" />
        <div class="actions">
          <el-button type="primary" size="large" @click="addCart">加入购物车</el-button>
          <el-button type="warning" size="large" @click="buyNow">立即购买</el-button>
        </div>
        <div class="ai-actions">
          <el-button plain @click="askGuide">AI 分析商品</el-button>
          <el-button plain @click="sceneMatch">AI 推荐搭配</el-button>
          <el-button plain @click="findSimilar">AI 找相似商品</el-button>
          <el-button plain @click="askService">AI 问客服</el-button>
        </div>
      </div>
    </section>

    <el-tabs class="tabs">
      <el-tab-pane label="商品详情">
        <p>{{ product.description }}</p>
        <p>商品特点：精选商品信息来自数据库，适合演示完整商城购买流程。</p>
      </el-tab-pane>
      <el-tab-pane label="规格参数"><pre>{{ product.specs }}</pre></el-tab-pane>
      <el-tab-pane label="品牌故事">
        <div v-if="brand" class="brand-story">
          <img :src="brand.imageUrl" :alt="brand.name" @error="imageError" />
          <div><h3>{{ brand.name }}</h3><p>{{ brand.description }}</p></div>
        </div>
        <el-empty v-else description="暂无品牌故事" />
      </el-tab-pane>
      <el-tab-pane label="用户评价">
        <el-empty v-if="reviews.length === 0" description="暂无评价" />
        <div v-for="review in reviews" :key="review.id" class="review">
          <el-avatar :src="review.avatar" />
          <div><b>{{ review.username }}</b><el-rate :model-value="review.rating" disabled size="small" /><p>{{ review.content }}</p></div>
        </div>
      </el-tab-pane>
    </el-tabs>

    <section>
      <h2>类似商品推荐</h2>
      <div class="product-grid">
        <ProductCard v-for="item in similar" :key="item.id" :product="item" @add-cart="addCartProduct" />
      </div>
    </section>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import ProductCard from '../../components/ProductCard.vue'
import { addCartApi, getProductDetailApi, getSimilarProductsApi } from '../../api/mall'
import { defaultProductImage, imageError } from '../../utils/images'

const route = useRoute()
const router = useRouter()
const product = ref(null)
const brand = ref(null)
const skus = ref([])
const reviews = ref([])
const images = ref([])
const similar = ref([])
const activeImage = ref('')
const selectedSpec = ref('')
const quantity = ref(1)
const imageList = computed(() => images.value.length ? images.value.map(i => i.imageUrl) : [product.value?.imageUrl || defaultProductImage])

function money(value) {
  return Number(value || 0).toFixed(2)
}

async function load() {
  const data = await getProductDetailApi(route.params.id)
  product.value = data.product
  brand.value = data.brand
  skus.value = data.skus || []
  reviews.value = data.reviews || []
  images.value = data.images || []
  selectedSpec.value = skus.value[0]?.specInfo || '默认规格'
  activeImage.value = imageList.value[0]
  similar.value = await getSimilarProductsApi(route.params.id)
}

async function addCart() {
  if (!selectedSpec.value) return ElMessage.warning('请先选择规格')
  await addCartApi({ productId: product.value.id, specInfo: selectedSpec.value, quantity: quantity.value })
  ElMessage.success('已加入购物车')
}

async function buyNow() {
  if (!selectedSpec.value) return ElMessage.warning('请先选择规格')
  router.push({ path: '/user/checkout', query: { productId: product.value.id, specInfo: selectedSpec.value, quantity: quantity.value } })
}

async function addCartProduct(item) {
  await addCartApi({ productId: item.id, specInfo: '默认规格', quantity: 1 })
  ElMessage.success('已加入购物车')
}

function askGuide() {
  router.push({ path: '/ai/guide', query: { q: `帮我分析商品：${product.value?.name || ''}，价格${product.value?.price || ''}元，适合什么人购买？` } })
}

function sceneMatch() {
  router.push({ path: '/ai/guide', query: { q: `围绕${product.value?.name || '这个商品'}推荐一套搭配组合` } })
}

function findSimilar() {
  router.push({ path: '/ai/search', query: { q: `找和${product.value?.name || '当前商品'}相似的商品` } })
}

function askService() {
  router.push({ path: '/ai/customer-service', query: { q: `我想咨询商品${product.value?.name || ''}的购买和售后问题` } })
}

onMounted(load)
</script>

<style scoped>
.detail-page {
  max-width: 1440px;
  margin: 0 auto;
  display: flex;
  flex-direction: column;
  gap: 22px;
}

.detail-card {
  display: grid;
  grid-template-columns: minmax(360px, 440px) minmax(0, 1fr);
  gap: 28px;
  padding: 24px;
  border-radius: 12px;
  background: #fff;
}

.product-main-image {
  width: 100%;
  aspect-ratio: 4 / 3;
  object-fit: cover;
  border-radius: 12px;
  background: #f5f7fa;
}

.main-image {
  display: block;
}

.thumbs {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  margin-top: 12px;
  overflow-x: auto;
}

.product-thumbnail {
  width: 72px;
  height: 72px;
  object-fit: cover;
  border-radius: 8px;
  cursor: pointer;
  background: #f5f7fa;
  border: 2px solid transparent;
  flex: 0 0 auto;
}

.product-thumbnail.active {
  border-color: #2563eb;
}

.info h1 {
  margin: 12px 0;
  font-size: 30px;
}

.info p,
.brand,
.stock {
  color: #64748b;
  line-height: 1.7;
}

.rating {
  display: flex;
  align-items: center;
  gap: 10px;
  margin: 12px 0;
}

.price-box {
  margin: 18px 0;
}

.price {
  color: #f59e0b;
  font-size: 32px;
  font-weight: 900;
}

.origin {
  margin-left: 12px;
  color: #94a3b8;
  text-decoration: line-through;
}

.sku-box {
  display: flex;
  flex-direction: column;
  gap: 10px;
  margin: 18px 0;
}

.actions {
  margin-top: 18px;
  display: flex;
  gap: 12px;
}

.ai-actions {
  margin-top: 14px;
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  padding: 14px;
  border-radius: 12px;
  background: #eff6ff;
  border: 1px solid #dbeafe;
}

.tabs {
  padding: 18px;
  border-radius: 12px;
  background: #fff;
}

.brand-story {
  display: flex;
  gap: 16px;
}

.brand-story img {
  width: 180px;
  height: 120px;
  object-fit: cover;
  border-radius: 10px;
}

.review {
  display: flex;
  gap: 12px;
  padding: 12px 0;
  border-bottom: 1px solid #eef2f7;
}

.product-grid {
  display: grid;
  grid-template-columns: repeat(5, minmax(0, 1fr));
  gap: 18px;
}

@media (max-width: 1440px) {
  .product-grid {
    grid-template-columns: repeat(4, minmax(0, 1fr));
  }
}

@media (max-width: 1024px) {
  .product-grid {
    grid-template-columns: repeat(3, minmax(0, 1fr));
  }
}

@media (max-width: 768px) {
  .detail-card {
    grid-template-columns: 1fr;
  }

  .product-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 640px) {
  .actions,
  .ai-actions {
    display: grid;
    grid-template-columns: 1fr;
  }

  .product-grid {
    grid-template-columns: 1fr;
  }
}
</style>
