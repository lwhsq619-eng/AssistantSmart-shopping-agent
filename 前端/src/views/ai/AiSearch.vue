<template>
  <div class="ai-page">
    <AiPageHeader
      title="语义化商品搜索"
      subtitle="输入模糊描述，AI 会解析关键词、预算、场景和功能标签，再从数据库商品中检索真实结果。"
      tag="Semantic Search"
    />

    <section class="ai-card">
      <div class="search-row">
        <el-input
          v-model="keyword"
          size="large"
          clearable
          placeholder="例如：适合宿舍用的安静小风扇，价格别太贵"
          @keyup.enter="search(1)"
        >
          <template #append>
            <el-button :loading="loading" @click="search(1)">搜索</el-button>
          </template>
        </el-input>
        <el-select v-model="sortBy" size="large" @change="search(1)">
          <el-option label="综合相关度" value="RELEVANCE" />
          <el-option label="价格升序" value="PRICE_ASC" />
          <el-option label="价格降序" value="PRICE_DESC" />
          <el-option label="销量降序" value="SALES_DESC" />
        </el-select>
      </div>
      <div v-if="searched" class="tags">
        <el-tag v-if="result.normalizedKeyword">关键词：{{ result.normalizedKeyword }}</el-tag>
        <el-tag v-if="result.categoryName" type="success">分类：{{ result.categoryName }}</el-tag>
        <el-tag v-for="tag in resultTags" :key="tag" type="info">{{ tag }}</el-tag>
        <el-tag v-if="result.fallback || result.fallbackUsed" type="warning">热销兜底</el-tag>
      </div>
      <el-alert
        v-if="result.fallback || result.fallbackUsed"
        title="未找到完全匹配商品，当前展示相近热销商品。"
        type="warning"
        show-icon
        :closable="false"
      />
    </section>

    <AiLoading v-if="loading" text="AI 正在解析搜索意图并检索商品..." />
    <AiEmptyState v-else-if="searched && !products.length" description="暂无匹配商品，请尝试放宽预算或更换关键词。" />
    <section v-else-if="products.length" class="ai-product-grid">
      <AiProductCard v-for="product in pagedProducts" :key="product.productId || product.id" :product="product" />
    </section>
    <AiEmptyState v-else description="输入一句自然语言描述开始搜索。" />

    <div v-if="products.length" class="pager">
      <el-pagination
        layout="prev, pager, next"
        :current-page="page"
        :page-size="size"
        :total="products.length"
        @current-change="page = $event"
      />
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import AiPageHeader from '../../components/ai/AiPageHeader.vue'
import AiProductCard from '../../components/ai/AiProductCard.vue'
import AiEmptyState from '../../components/ai/AiEmptyState.vue'
import AiLoading from '../../components/ai/AiLoading.vue'
import { semanticSearch } from '../../api/ai'
import { useAuthStore } from '../../stores/auth'

const route = useRoute()
const auth = useAuthStore()
const keyword = ref('')
const sortBy = ref('RELEVANCE')
const loading = ref(false)
const searched = ref(false)
const page = ref(1)
const size = ref(12)
const products = ref([])
const result = reactive({})

const resultTags = computed(() => [
  ...(result.sceneTags || []),
  ...(result.functionTags || []),
  ...(result.tags || [])
].filter(Boolean))

const sortedProducts = computed(() => {
  const list = [...products.value]
  const score = (item) => Number(item.matchScore ?? item.score ?? 0)
  if (sortBy.value === 'PRICE_ASC') return list.sort((a, b) => Number(a.price || 0) - Number(b.price || 0) || score(b) - score(a))
  if (sortBy.value === 'PRICE_DESC') return list.sort((a, b) => Number(b.price || 0) - Number(a.price || 0) || score(b) - score(a))
  if (sortBy.value === 'SALES_DESC') return list.sort((a, b) => Number(b.sales || 0) - Number(a.sales || 0) || score(b) - score(a))
  return list.sort((a, b) => score(b) - score(a))
})

const pagedProducts = computed(() => sortedProducts.value.slice((page.value - 1) * size.value, page.value * size.value))

async function search(targetPage = page.value) {
  if (!keyword.value.trim()) return ElMessage.warning('请输入搜索内容')
  page.value = targetPage
  searched.value = true
  loading.value = true
  try {
    const data = await semanticSearch({
      userId: auth.user?.id || 1,
      keyword: keyword.value,
      page: 1,
      size: 50,
      sortBy: sortBy.value
    })
    Object.assign(result, data)
    products.value = (data.products || []).map((item) => ({ ...item, reason: item.matchReason }))
  } catch (error) {
    products.value = []
    ElMessage.error(error.message || '语义搜索失败')
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  const q = route.query.q || route.query.keyword
  if (q) {
    keyword.value = String(q)
    search(1)
  }
})
</script>

<style scoped>
.search-row {
  display: grid;
  grid-template-columns: 1fr 180px;
  gap: 12px;
}

.tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-top: 14px;
}

.pager {
  display: flex;
  justify-content: center;
}

@media (max-width: 768px) {
  .search-row {
    grid-template-columns: 1fr;
  }
}
</style>
