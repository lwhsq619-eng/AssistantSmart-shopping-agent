<template>
  <div class="list-page">
    <section class="list-banner">
      <h1>{{ title }}</h1>
      <p>商品数据来自 MySQL，支持分类、品牌与关键词模糊查询。</p>
    </section>

    <div class="filters">
      <el-input v-model="query.keyword" placeholder="搜索商品名称" clearable @keyup.enter="search" />
      <el-select v-model="query.categoryId" placeholder="全部分类" clearable>
        <el-option v-for="c in categories" :key="c.id" :label="c.name" :value="c.id" />
      </el-select>
      <el-button type="primary" @click="search">搜索</el-button>
    </div>

    <el-skeleton v-if="loading" :rows="6" animated />
    <el-empty v-else-if="products.length === 0" description="暂无搜索结果" />
    <div v-else class="product-grid">
      <ProductCard v-for="item in products" :key="item.id" :product="item" @add-cart="addCart" />
    </div>

    <el-pagination
      class="pagination"
      background
      layout="prev, pager, next, total"
      :total="total"
      :page-size="query.size"
      v-model:current-page="query.current"
      @current-change="load"
    />
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import ProductCard from '../../components/ProductCard.vue'
import { addCartApi, getCategoriesApi, searchProductsApi } from '../../api/mall'

const route = useRoute()
const router = useRouter()
const loading = ref(false)
const categories = ref([])
const products = ref([])
const total = ref(0)
const query = reactive({
  current: 1,
  size: 20,
  keyword: route.query.keyword || '',
  categoryId: route.query.categoryId ? Number(route.query.categoryId) : undefined,
  brandId: route.query.brandId ? Number(route.query.brandId) : undefined
})

const title = computed(() => route.path.includes('category') ? '商品分类' : '搜索结果')

async function load() {
  loading.value = true
  try {
    const data = await searchProductsApi(query)
    products.value = data.records || []
    total.value = data.total || 0
  } finally {
    loading.value = false
  }
}

function search() {
  const nextQuery = { keyword: query.keyword || undefined, categoryId: query.categoryId || undefined }
  router.replace({ path: route.path, query: nextQuery })
  query.current = 1
  window.scrollTo({ top: 0, behavior: 'smooth' })
  load()
}

async function addCart(product) {
  await addCartApi({ productId: product.id, specInfo: '默认规格', quantity: 1 })
  ElMessage.success('已加入购物车')
}

watch(() => route.query, () => {
  query.keyword = route.query.keyword || ''
  query.categoryId = route.query.categoryId ? Number(route.query.categoryId) : undefined
  query.brandId = route.query.brandId ? Number(route.query.brandId) : undefined
  query.current = 1
  window.scrollTo({ top: 0, behavior: 'smooth' })
  load()
})

onMounted(async () => {
  categories.value = await getCategoriesApi()
  await load()
})
</script>

<style scoped>
.list-page {
  display: flex;
  flex-direction: column;
  gap: 18px;
}

.list-banner {
  border-radius: 12px;
  padding: 32px;
  color: #fff;
  background:
    linear-gradient(90deg, rgba(15, 23, 42, 0.74), rgba(37, 99, 235, 0.22)),
    url('https://images.unsplash.com/photo-1555529669-e69e7aa0ba9a?auto=format&fit=crop&w=1600&q=80') center/cover;
}

.list-banner h1 {
  margin: 0 0 8px;
}

.list-banner p {
  margin: 0;
  color: rgba(255, 255, 255, 0.86);
}

.filters {
  display: grid;
  grid-template-columns: 1fr 220px 100px;
  gap: 12px;
  padding: 14px;
  border-radius: 12px;
  background: #fff;
}

.product-grid {
  display: grid;
  grid-template-columns: repeat(5, minmax(0, 1fr));
  gap: 18px;
}

.pagination {
  align-self: center;
  margin-top: 8px;
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
  .filters {
    grid-template-columns: 1fr;
  }

  .product-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 576px) {
  .product-grid {
    grid-template-columns: 1fr;
  }
}
</style>
