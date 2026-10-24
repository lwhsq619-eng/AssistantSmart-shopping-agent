<template>
  <section class="ai-chat-box">
    <div class="messages">
      <div v-for="item in messages" :key="item.id" :class="['message', item.role]">
        <div class="bubble">
          <div class="markdown" v-html="renderMarkdown(item.content)"></div>
          <div v-if="item.products?.length" class="product-grid">
            <AiProductCard v-for="product in item.products" :key="product.productId || product.id" :product="product" />
          </div>
        </div>
      </div>
      <el-empty v-if="!messages.length" description="输入需求，开始 AI 对话" />
    </div>
    <div class="input-bar">
      <el-input
        v-model="text"
        type="textarea"
        :rows="3"
        :placeholder="placeholder"
        resize="none"
        @keydown.enter.exact.prevent="send"
      />
      <el-button type="primary" :loading="loading" @click="send">发送</el-button>
    </div>
  </section>
</template>

<script setup>
import { ref } from 'vue'
import { ElMessage } from 'element-plus'
import AiProductCard from './AiProductCard.vue'

const props = defineProps({
  placeholder: { type: String, default: '请输入你的问题或需求' },
  sender: { type: Function, required: true }
})

const emit = defineEmits(['response'])
const text = ref('')
const loading = ref(false)
const sessionId = `ai-${Date.now()}-${Math.random().toString(16).slice(2)}`
const messages = ref([])

async function send() {
  const content = text.value.trim()
  if (!content || loading.value) return
  messages.value.push({ id: Date.now(), role: 'user', content })
  text.value = ''
  loading.value = true
  try {
    const data = await props.sender(content, sessionId)
    const reply = data.reply || data.answer || data.content || data.message || 'AI 已返回结构化结果。'
    const products = extractProducts(data)
    messages.value.push({ id: Date.now() + 1, role: 'assistant', content: reply, products })
    emit('response', data)
  } catch (error) {
    ElMessage.error(error.message || 'AI 请求失败')
    messages.value.push({ id: Date.now() + 1, role: 'assistant error', content: error.message || 'AI 请求失败' })
  } finally {
    loading.value = false
  }
}

function extractProducts(data) {
  return data.products || data.data?.products || data.data?.data?.products || data.data?.groups?.[0]?.products || []
}

function renderMarkdown(value) {
  return escapeHtml(value || '')
    .replace(/^### (.*)$/gm, '<h3>$1</h3>')
    .replace(/^## (.*)$/gm, '<h2>$1</h2>')
    .replace(/^# (.*)$/gm, '<h1>$1</h1>')
    .replace(/\*\*(.*?)\*\*/g, '<strong>$1</strong>')
    .replace(/\n/g, '<br>')
}

function escapeHtml(text) {
  return String(text)
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
}
</script>

<style scoped>
.ai-chat-box {
  display: grid;
  grid-template-rows: 1fr auto;
  min-height: 620px;
  background: #fff;
  border-radius: 16px;
  border: 1px solid #e5e7eb;
  overflow: hidden;
}

.messages {
  padding: 20px;
  overflow-y: auto;
  background: linear-gradient(180deg, #f8fbff, #fff);
}

.message {
  display: flex;
  margin-bottom: 16px;
}

.message.user {
  justify-content: flex-end;
}

.bubble {
  max-width: 76%;
  padding: 14px 16px;
  border-radius: 14px;
  background: #eef2ff;
  color: #1e293b;
  line-height: 1.7;
}

.message.user .bubble {
  background: #2563eb;
  color: #fff;
}

.message.error .bubble {
  background: #fef2f2;
  color: #b91c1c;
}

.product-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(260px, 1fr));
  gap: 12px;
  margin-top: 12px;
}

.input-bar {
  display: grid;
  grid-template-columns: 1fr 100px;
  gap: 12px;
  padding: 16px;
  border-top: 1px solid #e5e7eb;
  background: #fff;
}
</style>
