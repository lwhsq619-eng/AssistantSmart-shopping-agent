<template>
  <div class="ai-page">
    <AiPageHeader
      title="智能客服"
      subtitle="支持订单状态、发货物流、退款退货、换货和优惠券咨询。涉及订单时只查询当前用户自己的数据。"
      tag="Customer Service"
    >
      <template #actions>
        <el-button type="warning" plain @click="manualService">转人工客服</el-button>
      </template>
    </AiPageHeader>

    <section class="ai-chat-layout">
      <AiChatBox
        :messages="messages"
        :loading="loading"
        :quick-questions="quickQuestions"
        placeholder="例如：我的订单怎么还没发货？订单号是202401010001"
        @send="send"
        @retry="retry"
        @clear="clear"
      />

      <aside class="ai-panel">
        <h3>常见问题</h3>
        <div class="faq-list">
          <button v-for="item in quickQuestions" :key="item" type="button" @click="send(item)">
            {{ item }}
          </button>
        </div>
        <div class="notice">
          <b>说明</b>
          <p>退款、退货、换货等操作需要以平台审核为准；AI 不会自动修改订单状态。</p>
        </div>
        <AiEmptyState v-if="!auth.isLogin" description="请登录后使用订单相关客服能力。" action-text="去登录" @action="$router.push('/login/user')" />
      </aside>
    </section>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { ElMessage } from 'element-plus'
import AiPageHeader from '../../components/ai/AiPageHeader.vue'
import AiChatBox from '../../components/ai/AiChatBox.vue'
import AiEmptyState from '../../components/ai/AiEmptyState.vue'
import { customerServiceChat } from '../../api/ai'
import { useAuthStore } from '../../stores/auth'

const auth = useAuthStore()
const storageKey = 'ai-customer-service'
const sessionId = ref(sessionStorage.getItem(`${storageKey}:id`) || `cs-${Date.now()}`)
const messages = ref(JSON.parse(sessionStorage.getItem(`${storageKey}:messages`) || '[]'))
const loading = ref(false)
const lastQuestion = ref('')

const quickQuestions = [
  '我的订单在哪里？',
  '为什么还没发货？',
  '如何申请退款？',
  '如何退货？',
  '有哪些优惠券？',
  '如何转人工客服？'
]

async function send(content) {
  if (!content || loading.value) return
  if (!auth.isLogin) {
    ElMessage.warning('请登录后使用智能客服')
    return
  }
  lastQuestion.value = content
  messages.value.push({ id: `u-${Date.now()}`, role: 'user', content })
  persist()
  loading.value = true
  try {
    const data = await customerServiceChat({
      userId: auth.user?.id,
      sessionId: sessionId.value,
      message: content
    })
    messages.value.push({ id: `a-${Date.now()}`, role: 'assistant', content: data.reply || data.message || '客服已返回结果。' })
    persist()
  } catch (error) {
    messages.value.push({ id: `e-${Date.now()}`, role: 'assistant', content: '客服服务暂时不可用，请稍后重试或转人工客服。', error: true })
    ElMessage.error(error.message || '智能客服请求失败')
    persist()
  } finally {
    loading.value = false
  }
}

function retry() {
  if (lastQuestion.value) send(lastQuestion.value)
}

function clear() {
  messages.value = []
  sessionId.value = `cs-${Date.now()}`
  persist()
}

function manualService() {
  ElMessage.info('已为你标记转人工客服，课程设计中该按钮作为人工客服入口占位。')
}

function persist() {
  sessionStorage.setItem(`${storageKey}:id`, sessionId.value)
  sessionStorage.setItem(`${storageKey}:messages`, JSON.stringify(messages.value))
}
</script>

<style scoped>
.ai-panel h3 {
  margin: 0 0 14px;
}

.faq-list {
  display: grid;
  gap: 10px;
  margin-bottom: 18px;
}

.faq-list button {
  padding: 10px 12px;
  border: 1px solid #dbeafe;
  border-radius: 10px;
  color: #1d4ed8;
  background: #eff6ff;
  text-align: left;
  cursor: pointer;
}

.notice {
  padding: 14px;
  border-radius: 12px;
  background: #fff7ed;
  color: #9a3412;
  line-height: 1.7;
}

.notice p {
  margin: 6px 0 0;
}
</style>
