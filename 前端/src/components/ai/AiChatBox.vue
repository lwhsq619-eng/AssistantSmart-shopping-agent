<template>
  <section class="ai-card chat-box">
    <div ref="listRef" class="ai-message-list">
      <AiEmptyState
        v-if="!messages.length"
        description="暂无聊天记录，可以从下方示例问题开始。"
      />
      <div
        v-for="item in visibleMessages"
        :key="item.id"
        :class="['ai-message', item.role === 'user' ? 'ai-message-user' : 'ai-message-assistant']"
      >
        <div class="ai-message-bubble">
          <div class="ai-markdown" v-html="renderMarkdown(item.content)" />
          <div v-if="item.error" class="retry-row">
            <el-button size="small" type="danger" plain @click="$emit('retry', item)">重新发送</el-button>
          </div>
        </div>
      </div>
      <div v-if="loading" class="ai-message ai-message-assistant">
        <div class="ai-message-bubble thinking">
          <el-icon class="is-loading"><Loading /></el-icon>
          AI 正在分析...
        </div>
      </div>
    </div>

    <div v-if="quickQuestions.length" class="quick-list">
      <el-button
        v-for="question in quickQuestions"
        :key="question"
        size="small"
        round
        plain
        @click="sendQuick(question)"
      >
        {{ question }}
      </el-button>
    </div>

    <div class="ai-input-area">
      <el-input
        v-model="text"
        type="textarea"
        :rows="3"
        resize="none"
        :placeholder="placeholder"
        :disabled="loading"
        @keydown.enter.exact.prevent="send"
      />
      <div class="button-stack">
        <el-button type="primary" :loading="loading" @click="send">发送</el-button>
        <el-button plain :disabled="loading || !messages.length" @click="$emit('clear')">清空</el-button>
      </div>
    </div>
    <p class="hint">Enter 发送，Shift + Enter 换行。</p>
  </section>
</template>

<script setup>
import { computed, nextTick, ref, watch } from 'vue'
import AiEmptyState from './AiEmptyState.vue'

const props = defineProps({
  messages: { type: Array, default: () => [] },
  loading: { type: Boolean, default: false },
  placeholder: { type: String, default: '请输入你的问题或需求' },
  quickQuestions: { type: Array, default: () => [] }
})

const emit = defineEmits(['send', 'retry', 'clear'])
const text = ref('')
const listRef = ref(null)
const visibleMessages = computed(() => props.messages.slice(-100))

function send() {
  const value = text.value.trim()
  if (!value || props.loading) return
  text.value = ''
  emit('send', value)
}

function sendQuick(question) {
  if (props.loading) return
  emit('send', question)
}

function renderMarkdown(value) {
  return escapeHtml(value || '')
    .replace(/^### (.*)$/gm, '<h3>$1</h3>')
    .replace(/^## (.*)$/gm, '<h2>$1</h2>')
    .replace(/^# (.*)$/gm, '<h1>$1</h1>')
    .replace(/\*\*(.*?)\*\*/g, '<strong>$1</strong>')
    .replace(/```([\s\S]*?)```/g, '<pre><code>$1</code></pre>')
    .replace(/\n/g, '<br>')
}

function escapeHtml(value) {
  return String(value).replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;')
}

watch(
  () => [props.messages.length, props.loading],
  async () => {
    await nextTick()
    if (listRef.value) listRef.value.scrollTop = listRef.value.scrollHeight
  }
)
</script>

<style scoped>
.chat-box {
  display: grid;
  gap: 12px;
}

.quick-list {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.button-stack {
  display: grid;
  gap: 8px;
  align-content: start;
  min-width: 96px;
}

.thinking {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  color: #2563eb;
}

.retry-row {
  margin-top: 10px;
}

.hint {
  margin: 0;
  color: #94a3b8;
  font-size: 12px;
}

@media (max-width: 768px) {
  .button-stack {
    grid-template-columns: 1fr 1fr;
  }
}
</style>
