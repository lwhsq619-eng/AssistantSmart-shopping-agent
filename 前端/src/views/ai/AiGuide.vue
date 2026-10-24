<template>
  <div class="ai-page">
    <AiPageHeader
      title="AI 智能导购"
      subtitle="用自然语言描述预算、用途和偏好，AI 会调用商品数据库生成推荐，不编造不存在的商品。"
      tag="Shopping Guide"
    />

    <section class="ai-chat-layout">
      <AiChatBox
        :messages="messages"
        :loading="loading"
        :quick-questions="quickQuestions"
        placeholder="例如：推荐500元以内适合跑步的无线耳机"
        @send="send"
        @retry="retry"
        @clear="clear"
      />

      <aside class="ai-panel">
        <div class="side-head">
          <h3>推荐商品</h3>
          <span>{{ products.length }} 件</span>
        </div>
        <AiLoading v-if="loading && !products.length" text="正在查询商品并生成推荐..." />
        <AiEmptyState v-else-if="!products.length" description="暂无推荐商品，先输入你的购买需求。" />
        <div v-else class="ai-product-grid side-grid">
          <AiProductCard v-for="product in products" :key="product.productId || product.id" :product="product" />
        </div>
      </aside>
    </section>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import AiPageHeader from '../../components/ai/AiPageHeader.vue'
import AiChatBox from '../../components/ai/AiChatBox.vue'
import AiProductCard from '../../components/ai/AiProductCard.vue'
import AiEmptyState from '../../components/ai/AiEmptyState.vue'
import AiLoading from '../../components/ai/AiLoading.vue'
import { shoppingGuideChat } from '../../api/ai'
import { useAuthStore } from '../../stores/auth'

const route = useRoute()
const auth = useAuthStore()
const storageKey = 'ai-guide-session'
const sessionId = ref(sessionStorage.getItem(`${storageKey}:id`) || `guide-${Date.now()}`)
const messages = ref(JSON.parse(sessionStorage.getItem(`${storageKey}:messages`) || '[]'))
const products = ref(JSON.parse(sessionStorage.getItem(`${storageKey}:products`) || '[]'))
const loading = ref(false)
const lastQuestion = ref('')

const quickQuestions = [
  '推荐500元以内耳机',
  '适合宿舍的好物',
  '送朋友的生日礼物',
  '性价比高的办公用品'
]

async function send(content) {
  if (!content || loading.value) return
  lastQuestion.value = content
  messages.value.push({ id: `u-${Date.now()}`, role: 'user', content })
  persist()
  loading.value = true
  try {
    const data = await shoppingGuideChat({
      userId: auth.user?.id || 1,
      sessionId: sessionId.value,
      message: content
    })
    const reply = data.reply || data.content || data.message || 'AI 已返回结果。'
    products.value = normalizeProducts(data.products || data.data?.products || [])
    messages.value.push({ id: `a-${Date.now()}`, role: 'assistant', content: reply })
    persist()
  } catch (error) {
    const fallback = 'AI 服务暂时不可用，请稍后重试；你也可以换一个更明确的商品关键词。'
    messages.value.push({ id: `e-${Date.now()}`, role: 'assistant', content: fallback, error: true })
    ElMessage.error(error.message || 'AI 导购请求失败')
    persist()
  } finally {
    loading.value = false
  }
}

function normalizeProducts(list) {
  return list.map((item) => ({
    ...item,
    reason: item.reason || item.matchReason || item.recommendReason
  }))
}

function retry() {
  if (lastQuestion.value) send(lastQuestion.value)
}

function clear() {
  messages.value = []
  products.value = []
  sessionId.value = `guide-${Date.now()}`
  sessionStorage.removeItem(`${storageKey}:messages`)
  sessionStorage.removeItem(`${storageKey}:products`)
  sessionStorage.setItem(`${storageKey}:id`, sessionId.value)
}

function persist() {
  sessionStorage.setItem(`${storageKey}:id`, sessionId.value)
  sessionStorage.setItem(`${storageKey}:messages`, JSON.stringify(messages.value))
  sessionStorage.setItem(`${storageKey}:products`, JSON.stringify(products.value))
}

onMounted(() => {
  sessionStorage.setItem(`${storageKey}:id`, sessionId.value)
  const q = route.query.q || route.query.message
  if (q && !messages.value.length) send(String(q))
})
</script>

<style scoped>
.side-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 16px;
}

.side-head h3 {
  margin: 0;
}

.side-head span {
  color: #64748b;
}

.side-grid {
  grid-template-columns: 1fr;
}
</style>
