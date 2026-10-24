<template>
  <div class="ai-page">
    <AiPageHeader
      title="商家 AI 工作台"
      subtitle="统一接入商品文案、营销素材、评论分析、订单分析和运营简报。所有内容仅作为运营建议，不自动覆盖业务数据。"
      tag="Merchant AI"
    >
      <template #actions>
        <el-button :loading="healthLoading" plain @click="loadHealth">检查 AI 状态</el-button>
      </template>
    </AiPageHeader>

    <section class="stats">
      <div class="ai-card stat-card"><span>AI 功能</span><strong>{{ health?.enabledAgents?.length || '暂无统计' }}</strong></div>
      <div class="ai-card stat-card"><span>DeepSeek</span><strong>{{ health?.deepseekAvailable ? '已连接' : '待检查' }}</strong></div>
      <div class="ai-card stat-card"><span>RAG 文档</span><strong>{{ health?.ragDocumentCount ?? '暂无数据' }}</strong></div>
      <div class="ai-card stat-card"><span>最近调用</span><strong>以接口返回为准</strong></div>
    </section>

    <el-tabs v-model="active" class="ai-panel">
      <el-tab-pane label="AI 工作台" name="chat">
        <AiChatBox
          :messages="messages"
          :loading="chatLoading"
          :quick-questions="['帮商品1001生成开学季营销文案', '分析商品1001的评论问题', '生成本周运营简报']"
          placeholder="例如：帮商品1001生成开学季营销文案"
          @send="sendWorkbench"
          @clear="messages = []"
        />
      </el-tab-pane>

      <el-tab-pane label="商品文案" name="copy">
        <el-form :model="copyForm" label-width="100px" class="ai-form">
          <el-form-item label="商品ID"><el-input v-model="copyForm.productId" /></el-form-item>
          <el-form-item label="风格"><el-input v-model="copyForm.style" /></el-form-item>
          <el-form-item><el-button type="primary" :loading="loading" @click="generateCopy">生成文案</el-button></el-form-item>
        </el-form>
      </el-tab-pane>

      <el-tab-pane label="营销素材" name="marketing">
        <el-form :model="marketingForm" label-width="100px" class="ai-form">
          <el-form-item label="商品ID"><el-input v-model="marketingForm.productId" /></el-form-item>
          <el-form-item label="活动名称"><el-input v-model="marketingForm.activityName" /></el-form-item>
          <el-form-item><el-button type="primary" :loading="loading" @click="generateMarketing">生成素材</el-button></el-form-item>
        </el-form>
      </el-tab-pane>

      <el-tab-pane label="评论分析" name="comment">
        <el-form :model="commentForm" label-width="100px" class="ai-form">
          <el-form-item label="商品ID"><el-input v-model="commentForm.productId" /></el-form-item>
          <el-form-item><el-button type="primary" :loading="loading" @click="analyzeComment">分析评论</el-button></el-form-item>
        </el-form>
      </el-tab-pane>

      <el-tab-pane label="订单分析" name="order">
        <el-button type="primary" :loading="loading" @click="analyzeOrder">生成近30天订单分析报告</el-button>
      </el-tab-pane>

      <el-tab-pane label="运营简报" name="briefing">
        <AiChatBox
          :messages="briefingMessages"
          :loading="chatLoading"
          :quick-questions="['生成本周运营简报', '总结最近订单和库存风险']"
          placeholder="例如：生成本周运营简报"
          @send="sendWorkbench"
          @clear="briefingMessages = []"
        />
      </el-tab-pane>
    </el-tabs>

    <AiReportViewer
      v-if="lastResult"
      title="AI 输出结果"
      :markdown="reportMarkdown"
      :metric-summary="metricSummary"
      :sections="sections"
    />
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref, watch } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import AiPageHeader from '../../components/ai/AiPageHeader.vue'
import AiChatBox from '../../components/ai/AiChatBox.vue'
import AiReportViewer from '../../components/ai/AiReportViewer.vue'
import { aiHealth, aiWorkbenchChat, commentAnalyze, copywritingGenerate, marketingGenerate, orderAnalysis } from '../../api/ai'
import { useAuthStore } from '../../stores/auth'

const route = useRoute()
const auth = useAuthStore()
const active = ref(route.query.tab || 'chat')
const loading = ref(false)
const chatLoading = ref(false)
const healthLoading = ref(false)
const health = ref(null)
const lastResult = ref(null)
const messages = ref([])
const briefingMessages = ref([])
const copyForm = reactive({ productId: '1001', style: '年轻活泼' })
const marketingForm = reactive({ productId: '1001', activityName: '开学季好物' })
const commentForm = reactive({ productId: '1001' })

const reportMarkdown = computed(() => lastResult.value?.reportMarkdown || lastResult.value?.data?.reportMarkdown || JSON.stringify(lastResult.value || '', null, 2))
const metricSummary = computed(() => lastResult.value?.metricSummary || lastResult.value?.data?.metricSummary || null)
const sections = computed(() => lastResult.value?.sections || lastResult.value?.data?.sections || [])

watch(() => route.query.tab, (tab) => {
  if (tab) active.value = tab
})

async function loadHealth() {
  healthLoading.value = true
  try {
    health.value = await aiHealth()
  } finally {
    healthLoading.value = false
  }
}

async function sendWorkbench(message) {
  const target = active.value === 'briefing' ? briefingMessages : messages
  target.value.push({ id: `u-${Date.now()}`, role: 'user', content: message })
  chatLoading.value = true
  try {
    const data = await aiWorkbenchChat({ userId: auth.user?.id || 1, role: 'MERCHANT', sessionId: `merchant-${active.value}`, message })
    lastResult.value = data
    target.value.push({ id: `a-${Date.now()}`, role: 'assistant', content: data.reply || data.message || 'AI 已返回结果。' })
  } catch (error) {
    target.value.push({ id: `e-${Date.now()}`, role: 'assistant', content: error.message || 'AI 任务失败', error: true })
  } finally {
    chatLoading.value = false
  }
}

async function generateCopy() {
  await run(async () => {
    lastResult.value = await copywritingGenerate({
      productIds: [Number(copyForm.productId)],
      style: copyForm.style,
      targetAudience: '商城用户',
      scene: '课程设计演示',
      generateTypes: ['TITLE', 'SELLING_POINTS', 'DETAIL', 'PROMOTION']
    })
  })
}

async function generateMarketing() {
  await run(async () => {
    lastResult.value = await marketingGenerate({
      productIds: [Number(marketingForm.productId)],
      activityName: marketingForm.activityName,
      targetAudience: '年轻消费者',
      style: '简洁专业',
      channels: ['COMMUNITY', 'PUSH', 'SLOGAN'],
      extraRequirement: '突出性价比'
    })
  })
}

async function analyzeComment() {
  await run(async () => {
    lastResult.value = await commentAnalyze({
      productId: Number(commentForm.productId),
      limit: 20,
      analysisTypes: ['SENTIMENT', 'REPLY', 'NEGATIVE_SUMMARY']
    })
  })
}

async function analyzeOrder() {
  await run(async () => {
    const end = new Date().toISOString().slice(0, 10)
    const start = new Date(Date.now() - 30 * 86400000).toISOString().slice(0, 10)
    lastResult.value = await orderAnalysis({
      startDate: start,
      endDate: end,
      analysisTypes: ['FULL_REPORT'],
      categoryId: null
    })
  })
}

async function run(task) {
  loading.value = true
  try {
    await task()
    ElMessage.success('AI 任务完成')
  } catch (error) {
    ElMessage.error(error.message || 'AI 任务失败')
  } finally {
    loading.value = false
  }
}

onMounted(loadHealth)
</script>

<style scoped>
.stats {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 16px;
}

.stat-card {
  display: grid;
  gap: 8px;
}

.stat-card span {
  color: #64748b;
}

.stat-card strong {
  color: #2563eb;
  font-size: 22px;
}

.ai-form {
  max-width: 620px;
}

@media (max-width: 900px) {
  .stats {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (max-width: 560px) {
  .stats {
    grid-template-columns: 1fr;
  }
}
</style>
